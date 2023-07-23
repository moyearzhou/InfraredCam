package com.moyear;

public class BasicConfig {

    /*    public static final int yuvImgWidth = 256;
    public static final int yuvImgHeight = 392;
//    public static final int yuvImgHeight = 192;*/

    public static  int yuvImgWidth = 192;
    public static  int yuvImgHeight = 256;


//    public static final int yuvImgHeight = 256;

    //    public static final int videoCodingType = 6;


    /**
     * 码流数据编解码类型，一共有一下几种类型:
     * 1-热成像裸数据; 2-全屏测温数据; 3-实时裸数据; 4-热图数据; 5-热成像实时流; 6-YUV实时数 7-PS封装MJPEG实时流;
     * 8-全屏测温数据+YUV实时流; 9-YUV+裸数据; 10-仅YUV不含测温头; 11-测温头+YUV+裸数据
     */
    public static  int videoCodingType = 8;

    //    public static final int framerate = 50;
    public static final int framerate = 25;
}
