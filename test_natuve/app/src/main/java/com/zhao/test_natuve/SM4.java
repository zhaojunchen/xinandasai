package com.zhao.test_natuve;

public class SM4 {
    /** 保存c++对象的地址*/
    long nativeSM4;

    /** 构造函数*/
    public SM4(String key) {
        nativeSM4 = createNativeObject(key);
    }

    public void encrypt() {


    }

    public void decrypt() {

    }


    static {
        System.loadLibrary("native-lib");
    }

    public native long createNativeObject(String key);
//    public native void encrypt(String output, String input, int length);
//    public native void decrypt();
//    public native
}
