package com.uplix.hackathon.Util;

import java.util.Set;

public class NoiseFilter {

    // Extensions that create noise
    private static final Set<String> NOISY_EXTENSIONS = Set.of(
            // Compiled / binaries
            ".class", ".o", ".obj", ".exe", ".dll", ".so", ".dylib", ".pyc",
            // Archives / large data
            ".zip", ".tar", ".gz", ".rar", ".7z", ".bak",
            ".db", ".sqlite",
            // Logs / temp
            ".log", ".tmp", ".swp",
            // Media
            ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico",
            ".mp4", ".mov", ".avi", ".mkv",
            ".mp3", ".wav",
            // Fonts
            ".ttf", ".woff", ".woff2"
    );

    // Folders to ignore entirely
    private static final Set<String> NOISY_FOLDERS = Set.of(
            ".git", ".idea", ".vscode", ".settings",
            "node_modules", "bin", "build", "out", "dist", "target",
            "__pycache__", "logs", "tmp", "coverage", ".cache", "doc", ".github"
    );

    // Files by exact name to ignore
    private static final Set<String> NOISY_FILES = Set.of(
            ".DS_Store", "Thumbs.db", "LICENSE", "COPYING", "NOTICE",
            ".gitignore", ".gitattributes",".settings", ".project", ".classpath ", "README.md", "README.txt", "CHANGELOG", "Changelog", "changelog"
    );

    /**
     * Check if a given file path is "noise" (unnecessary for scoring).
     *
     * @param path file path from GitHub tree (e.g., "src/Main.java")
     * @return true if noisy and should be ignored
     */
    public static boolean isNoise(String path) {
        String lower = path.toLowerCase();

        // Check for noisy folders in path
        for (String folder : NOISY_FOLDERS) {
            if (lower.contains("/" + folder.toLowerCase() + "/")
                    || lower.startsWith(folder.toLowerCase() + "/")) {
                return true;
            }
        }

        // Check for exact noisy filenames
        String fileName = lower.substring(lower.lastIndexOf('/') + 1);
        if (NOISY_FILES.contains(fileName) || NOISY_FOLDERS.contains(fileName)) {
            return true;
        }

        // Check for noisy extensions
        for (String ext : NOISY_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }
}
