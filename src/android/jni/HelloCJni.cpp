//
//  HelloCJni
//
#include <string.h>
#include <hello.h>
#include <HelloCJni.h>
#include "tgcalls/VideoCaptureInterface.h"
#include "legacy/InstanceImplLegacy.h"
#include "InstanceImpl.h"

// Platform-specific C implementation to get current CPU architecture
JNIEXPORT jstring JNICALL Java_com_example_HelloCJni_getArch(JNIEnv *env, jclass thiz)
{
#if defined(__arm__)
#if defined(__ARM_ARCH_7A__)
#if defined(__ARM_NEON__)
#if defined(__ARM_PCS_VFP)
#define ABI "armeabi-v7a/NEON (hard-float)"
#else
#define ABI "armeabi-v7a/NEON"
#endif
#else
#if defined(__ARM_PCS_VFP)
#define ABI "armeabi-v7a (hard-float)"
#else
#define ABI "armeabi-v7a"
#endif
#endif
#else
#define ABI "armeabi"
#endif
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__x86_64__)
#define ABI "x86_64"
#elif defined(__mips64) /* mips64el-* toolchain defines __mips__ too */
#define ABI "mips64"
#elif defined(__mips__)
#define ABI "mips"
#elif defined(__aarch64__)
#define ABI "arm64-v8a"
#else
#define ABI "unknown"
#endif
  return (*env)->NewStringUTF(env, ABI);
}

// Android JNI wrapper for cross-platform C implementation
JNIEXPORT jstring JNICALL Java_com_example_HelloCJni_hello(JNIEnv *env, jclass thiz, jstring j_input)
{
  // Call the cross-platform shared C function
  char *c_input = strdup((*env)->GetStringUTFChars(env, j_input, 0));
  char *output = c_hello(c_input);
  return (*env)->NewStringUTF(env, output);
}

// Android JNI wrapper for cross-platform C library
JNIEXPORT jstring JNICALL Java_com_example_HelloCJni_calculate(JNIEnv *env, jclass thiz, jint j_x, jint j_y)
{
  // Call the cross-platform shared C function
  int x = (int)j_x;
  int y = (int)j_y;
  int result = calculate(x, y);
  return result;
}

// Android JNI wrapper for cross-platform C library
JNIEXPORT jstring JNICALL Java_com_example_HelloCJni_crash(JNIEnv *env, jclass thiz)
{
  // Call the cross-platform shared C function
  int result = crash();
  return result;
}

struct InstanceHolder
{
  std::unique_ptr<Instance> nativeInstance;
  std::unique_ptr<GroupInstanceImpl> groupNativeInstance;
  std::shared_ptr<tgcalls::VideoCaptureInterface> _videoCapture;
  std::shared_ptr<PlatformContext> _platformContext;
};

jlong getInstanceHolderId(JNIEnv *env, jobject obj)
{
  return env->GetLongField(obj, env->GetFieldID(NativeInstanceClass, "nativePtr", "J"));
}

jobject asJavaTrafficStats(JNIEnv *env, const TrafficStats &trafficStats)
{
  jmethodID initMethodId = env->GetMethodID(TrafficStatsClass, "<init>", "(JJJJ)V");
  return env->NewObject(TrafficStatsClass, initMethodId, (jlong)trafficStats.bytesSentWifi, (jlong)trafficStats.bytesReceivedWifi, (jlong)trafficStats.bytesSentMobile, (jlong)trafficStats.bytesReceivedMobile);
}

JNIEXPORT jobject JNICALL Java_com_example_HelloCJni_getTrafficStats(JNIEnv *env, jobject obj)
{
  InstanceHolder *instance = getInstanceHolder(env, obj);
  if (instance->nativeInstance == nullptr)
  {
    return nullptr;
  }
  return asJavaTrafficStats(env, instance->nativeInstance->getTrafficStats());
}