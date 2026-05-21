package com.example.DevNotes.utils;

public class FileSizeUtil {

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB"};

    public static String humanReadable(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        double size = bytes / Math.pow(1024, unitIndex);

        return String.format("%.2f %s", size, UNITS[unitIndex]);
    }
}