#include <jni.h>
#include <string>
#include <GLES3/gl3.h>
#include <android/log.h>
#include "TuVideoSDK/VideoRender.hpp"
#include "TuVideoSDK/PreviewDecoder.hpp"
#include "TuVideoSDK/VideoRenderConfig.hpp"
#include "TuVideoSDK/TimeLineDecoder.hpp"
extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswresample/swresample.h>
#include <libswscale/swscale.h>
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_wuvideoedit_MainRenderer_previewDecoderInit(
        JNIEnv* env,
        jobject thisObj,
        jstring videoPath) {

//    VideoRenderConfig::shareInstance()->applyInversionFilter = true;
//    VideoRenderConfig::shareInstance()->applyGrayscaleFilter = false;
//
//    VideoRenderConfig::shareInstance()->applyMirrorEffect = false;
//    VideoRenderConfig::shareInstance()->applyFourGridEffect = true;

    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);

    jclass mainRendererClass = env->GetObjectClass(thisObj);
    jmethodID displayVideoId = env->GetMethodID(mainRendererClass, "displayVideo",
                                                "(J)V");
    jobject globalThisObj = env->NewGlobalRef(thisObj);

    const char* videoPathStr = env->GetStringUTFChars(videoPath, NULL);
    PreviewDecoder *previewDecoder = new PreviewDecoder(videoPathStr, [javaVM, globalThisObj, displayVideoId](AVFrame *display_frame){
        JNIEnv *env;
        if (javaVM->AttachCurrentThread(&env, nullptr) == 0) {
            env->CallVoidMethod(globalThisObj, displayVideoId, reinterpret_cast<jlong>(display_frame));
        }
    });

    return reinterpret_cast<jlong>(previewDecoder);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_wuvideoedit_MainRenderer_videoRenderInit(
        JNIEnv* env,
        jobject /* this */,
        jstring basePath) {
    const char* basePathStr = env->GetStringUTFChars(basePath, NULL);
    VideoRender *videoRender = new VideoRender(basePathStr);
    videoRender->setupViewport(1080, 1080.0 / 1172.0 * 720);
    return reinterpret_cast<jlong>(videoRender);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_wuvideoedit_MainRenderer_videoPlayDecode(
        JNIEnv* env,
        jobject /* this */,
        jlong previewDecoderHandle) {
    if (previewDecoderHandle != 0) {
        PreviewDecoder *previewDecoder = reinterpret_cast<PreviewDecoder *>(previewDecoderHandle);

//        previewDecoder->seekRow = 234;
//        previewDecoder->videoPreviewDecode(234);

        previewDecoder->setPause(false);
        previewDecoder->videoPlayDecode();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_wuvideoedit_MainRenderer_audioPlayDecode(
        JNIEnv* env,
        jobject /* this */,
        jlong previewDecoderHandle) {
    if (previewDecoderHandle != 0) {
        PreviewDecoder *previewDecoder = reinterpret_cast<PreviewDecoder *>(previewDecoderHandle);
        previewDecoder->audioPlayDecode();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_wuvideoedit_MainRenderer_updateFrame(
        JNIEnv* env,
        jobject /* this */,
        jlong previewDecoderHandle) {
    if (previewDecoderHandle != 0) {
        PreviewDecoder *previewDecoder = reinterpret_cast<PreviewDecoder *>(previewDecoderHandle);
        previewDecoder->updateFrame();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_wuvideoedit_MainRenderer_displayFrame(
        JNIEnv* env,
        jobject /* this */,
        jlong videoRenderHandle,
        jlong  frameHandle) {
    if (videoRenderHandle != 0 && frameHandle != 0) {
        VideoRender *videoRender = reinterpret_cast<VideoRender *>(videoRenderHandle);
        AVFrame *frame = reinterpret_cast<AVFrame *>(frameHandle);
        videoRender->displayFrame(frame);
    }
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_wuvideoedit_EditActivity_timeLineDecoderInit(
        JNIEnv* env,
        jobject thisObj,
        jstring videoPath) {

    const char* videoPathStr = env->GetStringUTFChars(videoPath, NULL);
    TimeLineDecoder *timeLineDecoder = new TimeLineDecoder(videoPathStr);

    return reinterpret_cast<jlong>(timeLineDecoder);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_wuvideoedit_EditActivity_timeLineDecode(
        JNIEnv* env,
        jobject thisObj,
        jlong timeLineDecoderHandle) {

    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);

    jclass mainRendererClass = env->GetObjectClass(thisObj);
    jmethodID refreshCellsId = env->GetMethodID(mainRendererClass, "refreshCells",
                                                "()V");
    jobject globalThisObj = env->NewGlobalRef(thisObj);

    if (timeLineDecoderHandle != 0) {
        TimeLineDecoder *timeLineDecoder = reinterpret_cast<TimeLineDecoder *>(timeLineDecoderHandle);
        timeLineDecoder->videoDecode(0, 100, [javaVM, globalThisObj, refreshCellsId](int startRow, int endRow) -> void {
            JNIEnv *env;
            if (javaVM->AttachCurrentThread(&env, nullptr) == 0) {
                env->CallVoidMethod(globalThisObj, refreshCellsId);
            }
        });
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_wuvideoedit_EditActivity_getTimeLineFrame(
        JNIEnv* env,
        jobject obj,
        jlong timeLineDecoderHandle,
        int row,
        jbyteArray byteArray) {
    if (timeLineDecoderHandle == 0) {
        return;
    }
    TimeLineDecoder *timeLineDecoder = reinterpret_cast<TimeLineDecoder *>(timeLineDecoderHandle);
    AVFrame *frame = timeLineDecoder->frameForIndex(row);
    if (frame == nullptr) return;
    int frameWidth = frame->width;
    int frameHeight = frame->height;
    if (frameWidth == 0 || frameHeight == 0) {
        return;
    }
    // 创建RGB数据缓冲区
    uint8_t *rgbaBuffer = (uint8_t*)malloc(frameWidth * frameHeight * 4); // 3 bytes per pixel for RGB
    if (!rgbaBuffer) {
        // 内存分配失败，处理错误
    }

    // 设置转换参数
    struct SwsContext *sws_ctx = sws_getContext(frameWidth, frameHeight, AV_PIX_FMT_YUV420P,
                                                frameWidth, frameHeight, AV_PIX_FMT_RGBA,
                                                SWS_BILINEAR, NULL, NULL, NULL);
    if (!sws_ctx) {
        // 创建转换上下文失败，处理错误
        return;
    }

    int dstStride[AV_NUM_DATA_POINTERS] = {0};
    dstStride[0] = frameWidth * 4;
    // 执行转换
    sws_scale(sws_ctx, frame->data, frame->linesize, 0, frame->height,
              &rgbaBuffer, dstStride);


    jbyte *buffer = env->GetByteArrayElements(byteArray, nullptr);
    memcpy(buffer, rgbaBuffer, dstStride[0] * frameHeight);

    env->ReleaseByteArrayElements(byteArray, buffer, 0);
}