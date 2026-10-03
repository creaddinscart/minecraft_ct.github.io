package com.ct.module.ctloader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public final class CTLoaderBootstrap {
    private static final String MAIN_CLASS_FLAG = "--ct-main-class";
    private static final String GAME_DIRECTORY_FLAG = "--ct-game-directory";
    private static URLClassLoaderHolder activeMods;

    private CTLoaderBootstrap() {
    }

    public static void main(String[] args) throws Throwable {
        if (args.length < 5 || !MAIN_CLASS_FLAG.equals(args[0])
                || !GAME_DIRECTORY_FLAG.equals(args[2]) || !"--".equals(args[4])) {
            throw new IOException("Invalid CT Loader launch arguments.");
        }

        String gameMainClass = args[1];
        Path gameDirectory = Path.of(args[3]);
        activeMods = new URLClassLoaderHolder(CTModLoader.load(gameDirectory, System.out::println));
        Thread.currentThread().setContextClassLoader(activeMods.classLoader());

        try {
            Class<?> gameMain = Class.forName(gameMainClass, true, ClassLoader.getSystemClassLoader());
            gameMain.getMethod("main", String[].class).invoke(null,
                    (Object) Arrays.copyOfRange(args, 5, args.length));
        } catch (java.lang.reflect.InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private record URLClassLoaderHolder(java.net.URLClassLoader classLoader) {
    }
}
