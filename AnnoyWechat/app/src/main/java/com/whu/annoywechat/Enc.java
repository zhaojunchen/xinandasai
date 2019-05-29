package com.whu.annoywechat;
import com.uopen.cryptionkit.utils.Utils;

import java.security.SecureRandom;


public class Enc {

    /**
     * 随机生成对称加密SM4公钥
     */
    public static String getKey() {
        byte[] key_byte = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key_byte);
        String key = Utils.byteToHex(key_byte);
        return key;
    }

    /**
     * 随机数测试函数
     * */

    public static int getRandom() {
        SecureRandom ra = new SecureRandom();
        return ra.nextInt(1000000);
    }

}

