LOCAL_PATH := $(call my-dir)
STL_PATH = $(NDK_ROOT)/sources/cxx-stl/gnu-libstdc++/4.6/libs/armeabi-v7a

include $(CLEAR_VARS)
LOCAL_MODULE:= libswresample-0
LOCAL_SRC_FILES:= lib/libswresample-0.so
LOCAL_PRELINK_MODULE := true
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavcodec-55
LOCAL_SRC_FILES:= lib/libavcodec-55.so
LOCAL_PRELINK_MODULE := true
include $(PREBUILT_SHARED_LIBRARY)
 
include $(CLEAR_VARS)
LOCAL_MODULE:= libavformat-55
LOCAL_SRC_FILES:= lib/libavformat-55.so
LOCAL_PRELINK_MODULE := true
include $(PREBUILT_SHARED_LIBRARY)
 
 
include $(CLEAR_VARS)
LOCAL_MODULE:= libavutil-52
LOCAL_SRC_FILES:= lib/libavutil-52.so
LOCAL_PRELINK_MODULE := true
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libspatializer
LOCAL_SRC_FILES:= lib/libspatializer.so
LOCAL_PRELINK_MODULE := true
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := spatialexjni
LOCAL_SRC_FILES := spatialexjni.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_CFLAGS += -march=armv7-a -marm -mfloat-abi=softfp -mfpu=vfpv3-d16
LOCAL_LDLIBS    := -llog
LOCAL_SHARED_LIBRARIES  := libspatializer libswresample-0 libavformat-55 libavcodec-55 libavutil-52 
include $(BUILD_SHARED_LIBRARY) 

