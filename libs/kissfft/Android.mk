
TOP_LOCAL_PATH:=$(call my-dir)
include $(call all-subdir-makefiles)
LOCAL_PATH := $(TOP_LOCAL_PATH)

include $(CLEAR_VARS)
LOCAL_MODULE:=fftNative
LOCAL_SRC_FILES:=com.badlogic.gdx.audio.analysis.KissFFT.cpp kiss_fft.c kiss_fftr.c

include $(BUILD_SHARED_LIBRARY)