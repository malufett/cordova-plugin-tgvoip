APP_ABI := all
#In general, you can only use a static variant of the C++ runtime if you have one and only one shared library in your application.
APP_STL := c++_static
APP_CPPFLAGS += -std=c++14
NDK_TOOLCHAIN_VERSION := clang
APP_PLATFORM := android-23
APP_OPTIM := debug