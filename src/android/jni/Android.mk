# Android Makefile

APP_PLATFORM := android-16
NDK_TOOLCHAIN_VERSION := clang
APP_STL := c++_static


#APP_PLATFORM := android-21
APP_ABI := armeabi-v7a arm64-v8a x86 x86_64

PATH_SEP := /

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

#traverse all the directory and subdirectory
define walk
  $(wildcard $(1)) $(foreach e, $(wildcard $(1)$(PATH_SEP)*), $(call walk, $(e)))
endef

SRC_LIST :=
INCLUDE_LIST :=


################################
# prepare shared lib

LOCAL_MODULE := tgvoip

# JNI interface files
INCLUDE_LIST += $(LOCAL_PATH)
SRC_LIST += $(wildcard $(LOCAL_PATH)/*.cpp)

# Cross-platform common files
# INCLUDE_LIST += $(LOCAL_PATH)/../../common/
# ifeq ($(OS),Windows_NT)
# 	INCLUDE_LIST += ${shell dir $(LOCAL_PATH)\..\..\common\ /ad /b /s}
# else
# 	INCLUDE_LIST += ${shell find $(LOCAL_PATH)/../../common/ -type d}
# endif
# SRC_LIST += $(filter %.c, $(call walk, $(LOCAL_PATH)/../../common))

# INCLUDE_LIST += $(LOCAL_PATH)/
# ifeq ($(OS),Windows_NT)
# 	INCLUDE_LIST += ${shell dir $(LOCAL_PATH)\ /ad /b /s}
# else
# 	INCLUDE_LIST += ${shell find $(LOCAL_PATH)/ -type d}
# endif
SRC_LIST += $(filter %.c, $(call walk, $(LOCAL_PATH)))


$(info LOCAL_PATH:$(LOCAL_PATH))
$(info SRC_LIST:$(SRC_LIST))
$(info INCLUDE_LIST:$(INCLUDE_LIST))

LOCAL_C_INCLUDES := $(INCLUDE_LIST)
LOCAL_SRC_FILES := $(SRC_LIST:$(LOCAL_PATH)/%=%)

# LOCAL_CFLAGS += -std=c99
LOCAL_CPPFLAGS += -Wall -std=c++14
LOCAL_CPPFLAGS := -fblocks
LOCAL_CPPFLAGS := -v
# TARGET_PLATFORM := android-27
TARGET_PLATFORM := android-16
LOCAL_DISABLE_FATAL_LINKER_WARNINGS := true
# LOCAL_LDLIBS += -Wl,--no-warn-shared-textrel
# LOCAL_LDFLAGS += -fuse-ld=gold

include $(BUILD_SHARED_LIBRARY)

################################
