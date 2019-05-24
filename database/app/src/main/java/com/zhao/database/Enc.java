package com.zhao.database;

import com.uopen.cryptionkit.utils.Utils;

import java.security.SecureRandom;

import com.uopen.cryptionkit.EncryptionManager;
import com.uopen.cryptionkit.core.AbstractCoder;
import com.uopen.cryptionkit.utils.Utils;

public class Enc {

    /** 随机生成对称加密SM4公钥*/
    public static String getKey() {
        byte[] key_byte = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key_byte);
        String key = Utils.byteToHex(key_byte);
        return key;
    }

    public static int getRandom() {
        SecureRandom ra = new SecureRandom();
        return ra.nextInt(1000000);
    }

}
