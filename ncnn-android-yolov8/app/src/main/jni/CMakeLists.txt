project(yolov8ncnn)

cmake_minimum_required(VERSION 3.10)

set(THIRD_PARTY_DIR ${CMAKE_SOURCE_DIR}/.. CACHE PATH "Third party root directory")
set(OpenCV_DIR ${OpenCV_DIR} CACHE PATH "OpenCV DIR override")
if(NOT OpenCV_DIR)
    set(OpenCV_DIR ${THIRD_PARTY_DIR}/jni/opencv-mobile/sdk/native/jni)
endif()
find_package(OpenCV REQUIRED core imgproc)

set(ncnn_DIR ${ncnn_DIR} CACHE PATH "ncnn DIR override")
if(NOT ncnn_DIR)
    set(ncnn_DIR ${THIRD_PARTY_DIR}/jni/ncnn/${ANDROID_ABI}/lib/cmake/ncnn)
endif()
find_package(ncnn REQUIRED)

add_library(yolov8ncnn SHARED yolov8ncnn.cpp yolo.cpp ndkcamera.cpp)

target_link_libraries(yolov8ncnn ncnn ${OpenCV_LIBS} camera2ndk mediandk)
