package com.ct.module.installer;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class DownloadExecutor {
    private static final int DOWNLOAD_THREADS = 8;
    private static final int DOWNLOAD_ATTEMPTS = 4;

    private DownloadExecutor() {
    }

    public static void downloadAll(List<DownloadRequest> downloads, String label,
            Consumer<String> status) throws IOException, InterruptedException {
        if (downloads.isEmpty()) {
            return;
        }
        status.accept("Verifying " + downloads.size() + " " + label + " files...");
        ExecutorService executor = Executors.newFixedThreadPool(DOWNLOAD_THREADS);
        ExecutorCompletionService<Void> completion = new ExecutorCompletionService<>(executor);
        try {
            for (DownloadRequest download : downloads) {
                completion.submit(() -> {
                    if (download.sha1() != null && download.size() >= 0) {
                        downloadVerified(download.url(), download.destination(), download.sha1(),
                                download.size());
                    } else {
                        downloadLegacyLibrary(download.url(), download.destination(), download.sha1());
                    }
                    return null;
                });
            }
            for (int completed = 1; completed <= downloads.size(); completed++) {
                try {
                    completion.take().get();
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    if (cause instanceof IOException ioException) {
                        throw ioException;
                    }
                    throw new IOException("A " + label + " file could not be installed.", cause);
                }
                if (completed == downloads.size() || completed % 250 == 0) {
                    status.accept("Verified " + completed + " of " + downloads.size() + " " + label
                            + " files...");
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    public static void downloadVerified(String address, Path destination, String expectedSha1,
            long expectedSize) throws IOException, InterruptedException {
        if (Files.isRegularFile(destination) && Files.size(destination) == expectedSize
                && Sha1.matches(destination, expectedSha1)) {
            return;
        }
        IOException failure = null;
        for (int attempt = 1; attempt <= DOWNLOAD_ATTEMPTS; attempt++) {
            try {
                fetchFile(address, destination, expectedSha1, expectedSize);
                return;
            } catch (RetryableDownloadException exception) {
                failure = exception;
                Thread.sleep(Duration.ofMillis(400L * attempt * attempt));
            }
        }
        throw new IOException("Could not download " + destination.getFileName() + " after "
                + DOWNLOAD_ATTEMPTS + " attempts.", failure);
    }

    private static void downloadLegacyLibrary(String address, Path destination, String expectedSha1)
            throws IOException, InterruptedException {
        if (Files.isRegularFile(destination) && Files.size(destination) > 0
                && (expectedSha1 == null || Sha1.matches(destination, expectedSha1))) {
            return;
        }
        Files.createDirectories(destination.getParent());
        Path temporary = destination.resolveSibling(destination.getFileName() + ".part");
        HttpRequest request = HttpApi.request(address)
                .timeout(Duration.ofMinutes(20))
                .GET()
                .build();
        try {
            HttpResponse<Path> response = HttpApi.client().send(request,
                    HttpResponse.BodyHandlers.ofFile(temporary, StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            if (response.statusCode() != 200 || Files.size(temporary) == 0) {
                throw new IOException("A legacy game library could not be downloaded.");
            }
            if (expectedSha1 != null && !Sha1.matches(temporary, expectedSha1)) {
                throw new IOException("A legacy game library failed SHA-1 verification.");
            }
            moveIntoPlace(temporary, destination);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void fetchFile(String address, Path destination, String expectedSha1,
            long expectedSize) throws IOException, InterruptedException {
        Files.createDirectories(destination.getParent());
        Path temporary = destination.resolveSibling(destination.getFileName() + ".part");
        HttpRequest request = HttpApi.request(address)
                .timeout(Duration.ofMinutes(20))
                .GET()
                .build();
        try {
            HttpResponse<Path> response;
            try {
                response = HttpApi.client().send(request, HttpResponse.BodyHandlers.ofFile(temporary,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE));
            } catch (IOException exception) {
                throw new RetryableDownloadException("The download connection failed.", exception);
            }
            int statusCode = response.statusCode();
            if (statusCode != 200) {
                if (statusCode >= 500 || statusCode == 429) {
                    throw new RetryableDownloadException(
                            "The download server returned HTTP " + statusCode + ".", null);
                }
                throw new IOException("The download failed with HTTP " + statusCode + " for " + address);
            }
            if (Files.size(temporary) != expectedSize) {
                throw new RetryableDownloadException("The downloaded file was incomplete.", null);
            }
            if (!Sha1.matches(temporary, expectedSha1)) {
                throw new IOException("Downloaded file failed SHA-1 verification: "
                        + destination.getFileName());
            }
            moveIntoPlace(temporary, destination);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void moveIntoPlace(Path temporary, Path destination) throws IOException {
        try {
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static final class RetryableDownloadException extends IOException {
        RetryableDownloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
