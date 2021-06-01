cordova-plugin-tgvoip
======================

A simple example of a Cordova plugin that uses pure C code.

It illustrates how to use platform-specific (either Android or iOS) C code and how to share C code cross-platform (between Android and iOS).

For Android it utilizes the Android NDK to compile architecture-specific libraries and a JNI wrapper to expose the C functions to the Java plugin API.

For iOS it uses the pure C source code in place alongside the Objective-C plugin wrapper, as well as an example cross-platform library compiled as a static library for iOS.

Template from: (https://github.com/clement360/Cordova-Hello-JNI-Plugin)

