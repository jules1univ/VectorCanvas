package com.jules1univ.vectorcanvas;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class NativeLibLoader {

    private static volatile boolean loaded = false;

    private NativeLibLoader() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        if (tryLoadFromResource()) {
            loaded = true;
            return;
        }

        if (tryLoadFromFilesystem()) {
            loaded = true;
            return;
        }

        throw new RuntimeException("Failed to load native library for " + os() + "-" + arch());
    }

    private static boolean tryLoadFromResource() {
        String path = resourcePath();
        try (InputStream in = NativeLibLoader.class.getResourceAsStream(path)) {
            if (in == null)
                return false;

            String[] parts = splitFilename(path);
            Path tmp = Files.createTempFile(parts[0], parts[1]);
            tmp.toFile().deleteOnExit();

            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            System.load(tmp.toAbsolutePath().toString());
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private static boolean tryLoadFromFilesystem() {
        Path classRoot = classOutputDir();
        if (classRoot == null)
            return false;

        String relative = "lib/native/" + os() + "-" + arch() + "/" + libFilename();

        Path dir = classRoot;
        for (int i = 0; i < 6; i++) {
            Path candidate = dir.resolve(relative);
            if (Files.exists(candidate)) {
                try {
                    System.load(candidate.toAbsolutePath().toString());
                    return true;
                } catch (UnsatisfiedLinkError e) {
                    return false;
                }
            }
            dir = dir.getParent();
            if (dir == null) {
                break;
            }
        }

        return false;
    }

    private static Path classOutputDir() {
        try {
            String className = NativeLibLoader.class.getName().replace('.', '/') + ".class";
            java.net.URL url = NativeLibLoader.class.getClassLoader().getResource(className);
            if (url == null)
                return null;

            String urlStr = url.toString();
            if (urlStr.startsWith("jar:"))
                return null;

            Path classFile = Paths.get(url.toURI());
            Path root = classFile;

            String[] segments = className.split("/");
            for (int i = 0; i < segments.length; i++) {
                root = root.getParent();
            }
            return root;

        } catch (Exception e) {
            return null;
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

    private static String[] splitFilename(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);
        int dot = filename.lastIndexOf('.');
        if (dot == -1)
            return new String[] { filename, "" };
        return new String[] { filename.substring(0, dot), filename.substring(dot) };
    }

    private static String os() {
        String name = System.getProperty("os.name").toLowerCase();
        if (name.contains("win"))
            return "windows";
        if (name.contains("mac") || name.contains("darwin"))
            return "macos";
        return "linux";
    }

    private static String arch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return "aarch64";
        }
        return "x86_64";
    }
}