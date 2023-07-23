package com.moyear.utils;

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/7/19 21:00
 * @Description :
 */
public class ConvertUtils {


    /**
     * 将byte数组数据转换成float
     *
     * @param arr
     * @return
     */
    public static float bytes2Float(byte[] arr) {
        int accum = 0;
        accum = accum | (arr[0] & 0xff) << 0;
        accum = accum | (arr[1] & 0xff) << 8;
        accum = accum | (arr[2] & 0xff) << 16;
        accum = accum | (arr[3] & 0xff) << 24;
        return Float.intBitsToFloat(accum);
    }


}
