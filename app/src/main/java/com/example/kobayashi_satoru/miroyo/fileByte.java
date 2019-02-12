package com.example.kobayashi_satoru.miroyo;

public class fileByte {
    public static String toStringMegaByte(int fileByte) {
        double megaByte = (double)fileByte / 1048576;
        return String.format("%.1f", megaByte) + " MB";
    }
}
