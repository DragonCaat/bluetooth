package com.vise.bledemo.common;

/**
 * @author : Darcy
 * @Date ${Date}
 * @Description java 中各种类型的转化
 */
public class StringUtil {

    /**
     * bit(位) 转化为 byte(字节)
     *
     * @param bit 要转化的bit
     */
    public static byte bitToByte(String bit) {
        int re, len;
        if (null == bit) {
            return 0;
        }
        len = bit.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {// 8 bit处理
            if (bit.charAt(0) == '0') {// 正数
                re = Integer.parseInt(bit, 2);
            } else {// 负数
                re = Integer.parseInt(bit, 2) - 256;
            }
        } else {//4 bit处理
            re = Integer.parseInt(bit, 2);
        }
        return (byte) re;
    }

}
