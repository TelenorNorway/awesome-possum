#include <jni.h>
#include <string>

extern "C"
jstring
Java_no_telenor_awesomepossum_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++ y'all!!";
    return env->NewStringUTF(hello.c_str());
}
