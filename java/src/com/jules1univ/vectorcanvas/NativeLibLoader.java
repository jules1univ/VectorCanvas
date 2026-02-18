package com.jules1univ.vectorcanvas;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class NativeLibLoader {

    private static volatile boolean loaded = false;

    private NativeLibLoader() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        String resourcePath = resourcePath();

        try (InputStream in = NativeLibLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException(
                        "Native library not found in JAR at: " + resourcePath);
            }

            String[] parts = splitFilename(resourcePath);
            Path tmp = Files.createTempFile(parts[0], parts[1]);
            tmp.toFile().deleteOnExit();

            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            System.load(tmp.toAbsolutePath().toString());
            loaded = true;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library from JAR: " + e.getMessage(), e);
        }
    }


    private static String resourcePath() {
        return "/native/" + os() + "-" + arch() + "/" + libFilename();
    }

    private static String libFilename() {
        switch (os()) {
            case "windows":
                return "vectorcanvas.dll";
            case "macos":
                return "libvectorcanvas.dylib";
            default:
                return "libvectorcanvas.so";
        }
    }

    private static String[] splitFilename(String resourcePath) {
        String filename = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        int dot = filename.lastIndexOf('.');
        if (dot == -1) {
            return new String[] { filename, "" };
        }
        return new String[] { filename.substring(0, dot), filename.substring(dot) };
    }

    private static String os() {
        String name = System.getProperty("os.name").toLowerCase();
        if (name.contains("win")) {
            return "windows";
        }
        if (name.contains("mac") || name.contains("darwin")) {
            return "macos";
        }
        return "linux";
    }

    private static String arch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("aarch64") || arch.contains("arm64"))
            return "aarch64";
        return "x86_64";
    }
}