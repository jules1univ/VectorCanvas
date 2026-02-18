#pragma once
#ifndef CANVAS_H
#define CANVAS_H

#include "global.h"

#define TYPE_INT_ARGB 2

JNIEXPORT jobject JNICALL Java_com_jules1univ_vectorcanvas_NativeCanvas_render(
    JNIEnv *env, jclass cls, jstring jxmlString);

#endif // CANVAS_H