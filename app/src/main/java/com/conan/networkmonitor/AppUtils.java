package com.conan.networkmonitor;

import java.text.DecimalFormat;

public class AppUtils {

    public static String getDataSize(long size) {
        DecimalFormat formater = new DecimalFormat("####.00");
        long b = 1024L;
        long kb = 1024*1024L;
        long mb = 1024*1024*1024L;
        long gb = 1024*1024*1024*1024L;
        if (size < b) {
            return size + "B";
        }
        else if (size < kb) {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + "KB";
        } else if (size < mb) {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + "MB";
        } else if (size < gb) {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + "GB";
        } else {
            return "";
        }
    }

    public static String getKb(long size) {
        DecimalFormat formater = new DecimalFormat("####.00");
        long b = 1024L;
        if (size < b) {
            return size + "B";
        }
        float kbsize = size / 1024f;
        return formater.format(kbsize) + "KB";
    }


}