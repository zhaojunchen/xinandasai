package com.zhao.a_native;

import java.security.SecureRandom;

public class SM4 {
    static{
        /** 加载原生sm4库*/
        System.loadLibrary("native-lib");
//        System.loadLibrary("sm4");
//        System.loadLibrary("sm4_cl");
    }

    /** native原生层 sm4加密算法
     * 输入:有待加密的字符串
     * 输入:加密的随机key字符串
     * 输出:加密后的字符串
     * */
    public native static byte[] encryption(byte[] input,byte [] key);
    /** native原生层 sm4解密算法
     * 输入:有待加密的字符串
     * 输入:加密的随机key字符串
     * 输出:加密后的字符串
     * */
    public native static byte[] decryption(byte[] input,byte [] key);
    /** 安全随机数生成器
     * 输出:16bit byte串
     * 作为对称加密的公钥*/
    public static byte[] getKey() {
        byte[] key = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return key;
    }

}
