// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.


// This file is autogenerated by
//     base/android/jni_generator/jni_generator.py
// For
//     org/webrtc/RtpSender

#ifndef org_webrtc_RtpSender_JNI
#define org_webrtc_RtpSender_JNI

#include <jni.h>

#include "webrtc/sdk/android/src/jni/jni_generator_helper.h"


// Step 1: Forward declarations.

JNI_REGISTRATION_EXPORT extern const char kClassPath_org_webrtc_RtpSender[];
const char kClassPath_org_webrtc_RtpSender[] = "org/webrtc/RtpSender";
// Leaking this jclass as we cannot use LazyInstance from some threads.
JNI_REGISTRATION_EXPORT std::atomic<jclass> g_org_webrtc_RtpSender_clazz(nullptr);
#ifndef org_webrtc_RtpSender_clazz_defined
#define org_webrtc_RtpSender_clazz_defined
inline jclass org_webrtc_RtpSender_clazz(JNIEnv* env) {
  return base::android::LazyGetClass(env, kClassPath_org_webrtc_RtpSender,
      &g_org_webrtc_RtpSender_clazz);
}
#endif


// Step 2: Constants (optional).


// Step 3: Method stubs.
namespace  webrtc {
namespace jni {

static jboolean JNI_RtpSender_SetTrack(JNIEnv* env, jlong rtpSender,
    jlong nativeTrack);

JNI_GENERATOR_EXPORT jboolean Java_org_webrtc_RtpSender_nativeSetTrack(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender,
    jlong nativeTrack) {
  return JNI_RtpSender_SetTrack(env, rtpSender, nativeTrack);
}

static jlong JNI_RtpSender_GetTrack(JNIEnv* env, jlong rtpSender);

JNI_GENERATOR_EXPORT jlong Java_org_webrtc_RtpSender_nativeGetTrack(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender) {
  return JNI_RtpSender_GetTrack(env, rtpSender);
}

static void JNI_RtpSender_SetStreams(JNIEnv* env, jlong rtpSender,
    const base::android::JavaParamRef<jobject>& streamIds);

JNI_GENERATOR_EXPORT void Java_org_webrtc_RtpSender_nativeSetStreams(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender,
    jobject streamIds) {
  return JNI_RtpSender_SetStreams(env, rtpSender, base::android::JavaParamRef<jobject>(env,
      streamIds));
}

static base::android::ScopedJavaLocalRef<jobject> JNI_RtpSender_GetStreams(JNIEnv* env, jlong
    rtpSender);

JNI_GENERATOR_EXPORT jobject Java_org_webrtc_RtpSender_nativeGetStreams(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender) {
  return JNI_RtpSender_GetStreams(env, rtpSender).Release();
}

static jlong JNI_RtpSender_GetDtmfSender(JNIEnv* env, jlong rtpSender);

JNI_GENERATOR_EXPORT jlong Java_org_webrtc_RtpSender_nativeGetDtmfSender(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender) {
  return JNI_RtpSender_GetDtmfSender(env, rtpSender);
}

static jboolean JNI_RtpSender_SetParameters(JNIEnv* env, jlong rtpSender,
    const base::android::JavaParamRef<jobject>& parameters);

JNI_GENERATOR_EXPORT jboolean Java_org_webrtc_RtpSender_nativeSetParameters(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender,
    jobject parameters) {
  return JNI_RtpSender_SetParameters(env, rtpSender, base::android::JavaParamRef<jobject>(env,
      parameters));
}

static base::android::ScopedJavaLocalRef<jobject> JNI_RtpSender_GetParameters(JNIEnv* env, jlong
    rtpSender);

JNI_GENERATOR_EXPORT jobject Java_org_webrtc_RtpSender_nativeGetParameters(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender) {
  return JNI_RtpSender_GetParameters(env, rtpSender).Release();
}

static base::android::ScopedJavaLocalRef<jstring> JNI_RtpSender_GetId(JNIEnv* env, jlong rtpSender);

JNI_GENERATOR_EXPORT jstring Java_org_webrtc_RtpSender_nativeGetId(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender) {
  return JNI_RtpSender_GetId(env, rtpSender).Release();
}

static void JNI_RtpSender_SetFrameEncryptor(JNIEnv* env, jlong rtpSender,
    jlong nativeFrameEncryptor);

JNI_GENERATOR_EXPORT void Java_org_webrtc_RtpSender_nativeSetFrameEncryptor(
    JNIEnv* env,
    jclass jcaller,
    jlong rtpSender,
    jlong nativeFrameEncryptor) {
  return JNI_RtpSender_SetFrameEncryptor(env, rtpSender, nativeFrameEncryptor);
}


static std::atomic<jmethodID> g_org_webrtc_RtpSender_Constructor(nullptr);
static base::android::ScopedJavaLocalRef<jobject> Java_RtpSender_Constructor(JNIEnv* env, jlong
    nativeRtpSender) {
  jclass clazz = org_webrtc_RtpSender_clazz(env);
  CHECK_CLAZZ(env, clazz,
      org_webrtc_RtpSender_clazz(env), NULL);

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "<init>",
          "(J)V",
          &g_org_webrtc_RtpSender_Constructor);

  jobject ret =
      env->NewObject(clazz,
          call_context.base.method_id, nativeRtpSender);
  return base::android::ScopedJavaLocalRef<jobject>(env, ret);
}

}  // namespace jni
}  // namespace  webrtc

// Step 4: Generated test functions (optional).


#endif  // org_webrtc_RtpSender_JNI
