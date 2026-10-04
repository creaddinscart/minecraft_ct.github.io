package com.ct.module.installer;

import java.nio.file.Path;

public record DownloadRequest(String url, Path destination, String sha1, long size,
        boolean classpathEntry) {
    public DownloadRequest(String url, Path destination, String sha1, long size) {
        this(url, destination, sha1, size, true);
    }
}
