#include <jni.h>
#include <string>
#include "sm4.h"
#include "sm4_cl.h"
#include <android/log.h>

SM4 MYSM4;

int checkUtfString(const char *bytes);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_zhao_a_1native_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_zhao_a_1native_SM4_encryption(JNIEnv *env, jclass type, jstring input_, jbyteArray key_) {
//    /** 类型转换 java字符串转化为c++ const char* */
//    const char *input = env->GetStringUTFChars(input_, 0);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "转化的input输入为%s", input);
//    /** 类型转换 java byte[]转化为c++ char* 本身为jbyte*类型 */
//    jbyte *key = env->GetByteArrayElements(key_, NULL);
//    unsigned char *mykey = (unsigned char *) key;
//
//
//    /** 计算输入字符字节长度*/
//    int length = strlen(input);
//    /** 动态分配内存*/
//    char *Value = new char[length];
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "%d", length);
//
//    /** 设置秘钥*/
//    MYSM4.setKey(mykey);
//    /** 解密*/
//    for (int i = 0; i < 16; i++) {
//        __android_log_print(ANDROID_LOG_INFO, "JNI", "cao%d", MYSM4.m_key[i]);
//    }
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "\n秘钥%s", MYSM4.m_key);
//    MYSM4.encrypt((unsigned char *) Value, (unsigned char *) input, length);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "加密后的字符%s", Value);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "加密后的字符长度%d", strlen(Value));
//
//    std::string returnValue(Value, 0, strlen(Value));
//    /**释放内存空间 */
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "是这儿么%d", length);
//
//    delete[] Value;
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "是这儿么%d", length);
//    checkUtfString(input);
//    env->ReleaseStringUTFChars(input_,input);
//    env->ReleaseByteArrayElements(key_, key, 0);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "是这儿么%d", length);
////    __android_log_print(ANDROID_LOG_INFO, "JNI", "返回值%c", returnValue);
//
//    return env->NewStringUTF(returnValue.c_str());
//}
//
//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_zhao_a_1native_SM4_decryption(JNIEnv *env, jclass type, jstring input_, jbyteArray key_) {
//    /** 类型转换 java字符串转化为c++ const char* */
//    const char *input = env->GetStringUTFChars(input_, 0);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "转化的input输入为%s", input);
//    /** 类型转换 java byte[]转化为c++ char* 本身为jbyte*类型 */
//    jbyte *key = env->GetByteArrayElements(key_, NULL);
//    unsigned char *mykey = (unsigned char *) key;
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "%s", mykey);
//
//    /** 计算输入字符字节长度*/
//    int length = strlen(input);
//    /** 动态分配内存*/
//    char *Value = new char[length];
//
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "%d", length);
//
//    /** 设置秘钥*/
//    MYSM4.setKey(mykey);
//    __android_log_print(ANDROID_LOG_INFO, "JNI秘钥:", "%s", MYSM4.m_key);
//    /** 解密*/
//    MYSM4.decrypt((unsigned char *) Value, (unsigned char *) input, length);
//
//    std::string returnValue(Value, 0, strlen(Value));
//    /**释放内存空间 */
//
//    delete[] Value;
////    env->ReleaseStringUTFChars(input_, input);
////    env->ReleaseByteArrayElements(key_, key, 0);
//    return env->NewStringUTF(returnValue.c_str());
//}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_zhao_a_1native_SM4_encryption(JNIEnv *env, jclass type, jbyteArray input_,
                                       jbyteArray key_) {
    jbyte *input_java = env->GetByteArrayElements(input_, NULL);
    jbyte *key_java = env->GetByteArrayElements(key_, NULL);

    // TODO
    /** 类型转换 java字符串转化为c++ const char* */
    char * input = (char*)input_java;
    char *key = (char *) key_java;

    /** 计算输入字符字节长度*/
    int length = strlen(input);
    /** 动态分配内存*/
//    jbyteArray Value =env->NewByteArray(length+1);
    char *Value = new char[length];
    __android_log_print(ANDROID_LOG_INFO, "JNI", "%d", length);

    /** 设置秘钥*/
    MYSM4.setKey((unsigned char*)key);
    /** 解密*/
    for (int i = 0; i < 16; i++) {
        __android_log_print(ANDROID_LOG_INFO, "JNI", "cao%d", MYSM4.m_key[i]);
    }
    __android_log_print(ANDROID_LOG_INFO, "JNI", "\n秘钥%s", MYSM4.m_key);
    MYSM4.encrypt((unsigned char *) Value, (unsigned char *) input, length);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "加密后的字符%s", Value);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "加密后的字符长度%d", strlen(Value));
//
//    std::string returnValue(Value, 0, strlen(Value));
//    /**释放内存空间 */
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "是这儿么%d", length);
//

    jbyte *jb = (jbyte *) Value;

    jbyteArray jarray = env->NewByteArray(length);
    env->SetByteArrayRegin(jarray,0,length,by);

    env->ReleaseByteArrayElements(input_, input_java, 0);
    env->ReleaseByteArrayElements(key_, key_java, 0);

}extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_zhao_a_1native_SM4_decryption(JNIEnv *env, jclass type, jbyteArray input_,
                                       jbyteArray key_) {
    jbyte *input_java = env->GetByteArrayElements(input_, NULL);
    jbyte *key_java = env->GetByteArrayElements(key_, NULL);

    // TODO
    /** 类型转换 java字符串转化为c++ const char* */
    char * input = (char*)input_java;
    char *key = (char *) key_java;

    /** 计算输入字符字节长度*/
    int length = strlen(input);
    /** 动态分配内存*/
//    jbyteArray Value =env->NewByteArray(length+1);
    char *Value = new char[length];
    __android_log_print(ANDROID_LOG_INFO, "JNI", "%d", length);

    /** 设置秘钥*/
    MYSM4.setKey((unsigned char*)key);
    /** 解密*/
    for (int i = 0; i < 16; i++) {
        __android_log_print(ANDROID_LOG_INFO, "JNI", "cao%d", MYSM4.m_key[i]);
    }
    __android_log_print(ANDROID_LOG_INFO, "JNI", "\n秘钥%s", MYSM4.m_key);
    MYSM4.decrypt((unsigned char *) Value, (unsigned char *) input, length);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "加密后的字符%s", Value);
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "加密后的字符长度%d", strlen(Value));

//    std::string returnValue(Value, 0, strlen(Value));
//    /**释放内存空间 */
//    __android_log_print(ANDROID_LOG_INFO, "JNI", "是这儿么%d", length);
//


    env->ReleaseByteArrayElements(input_, input_java, 0);
    env->ReleaseByteArrayElements(key_, key_java, 0);
    return Value;

}