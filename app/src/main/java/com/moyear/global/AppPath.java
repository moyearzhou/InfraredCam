package com.moyear.global;

import java.io.File;

public class AppPath {

    public static String WORK_SPACE = "/mnt/sdcard/ThermalCam/";

    public static String DIR_PHOTO = WORK_SPACE + "photos/";

    public static String DIR_VIDEOS = WORK_SPACE + "videos/";
    public static String DIR_RAW = WORK_SPACE + "raw/";

    public static String DIR_LOG = WORK_SPACE + "logs/";

    public static File getRawDir() {
        File rawDir = new File(DIR_RAW);

        if (!rawDir.exists()) {
            rawDir.mkdirs();
        }
        return rawDir;
    }

    public static File getPhotoDir() {
        File photoDir = new File(DIR_PHOTO);

        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }
        return photoDir;
    }

    public static File getVideoDir() {
        File videoDir = new File(DIR_VIDEOS);

        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }
        return videoDir;
    }

}
