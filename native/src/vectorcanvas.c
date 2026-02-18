#include "vectorcanvas.h"

static char *get_string(JNIEnv *env, jstring jstr) {
  if (!jstr) {
    return NULL;
  }
  const char *chars = (*env)->GetStringUTFChars(env, jstr, NULL);
  if (!chars) {
    return NULL;
  }
  char *result = strdup(chars);
  (*env)->ReleaseStringUTFChars(env, jstr, chars);
  return result;
}

static void throw_exception(JNIEnv *env, const char *class_name,
                            const char *message) {
  jclass exceptionClass = (*env)->FindClass(env, class_name);
  if (exceptionClass) {
    (*env)->ThrowNew(env, exceptionClass, message);
    (*env)->DeleteLocalRef(env, exceptionClass);
  }
}

static jobject render_svg_to_image(JNIEnv *env, char *xml_string) {

  struct NSVGimage *svg_image;
  svg_image = nsvgParse(xml_string, "px", 96.0f);
  free(xml_string);

  if (!svg_image) {
    throw_exception(env, "java/lang/IllegalArgumentException",
                    "Failed to parse SVG string.");
    return NULL;
  }

  jclass bufferedImageClass =
      (*env)->FindClass(env, "java/awt/image/BufferedImage");
  if (!bufferedImageClass) {
    throw_exception(env, "java/lang/IllegalArgumentException",
                    "Failed to find BufferedImage class.");
    return NULL;
  }

  jmethodID constructor =
      (*env)->GetMethodID(env, bufferedImageClass, "<init>", "(III)V");
  if (!constructor) {
    (*env)->DeleteLocalRef(env, bufferedImageClass);
    throw_exception(env, "java/lang/IllegalArgumentException",
                    "Failed to find BufferedImage constructor.");
    return NULL;
  }

  jobject image = (*env)->NewObject(
      env, bufferedImageClass, constructor, (jint)svg_image->width,
      (jint)svg_image->height, (jint)TYPE_INT_ARGB);
  (*env)->DeleteLocalRef(env, bufferedImageClass);
  if (!image) {
    throw_exception(env, "java/lang/IllegalArgumentException",
                    "Failed to create BufferedImage instance.");
    return NULL;
  }

  int width = (int)svg_image->width;
  int height = (int)svg_image->height;

  unsigned char *pixels = (unsigned char *)malloc(width * height * 4);
  if (!pixels) {
    (*env)->DeleteLocalRef(env, image);
    nsvgDelete(svg_image);
    throw_exception(env, "java/lang/OutOfMemoryError",
                    "Failed to allocate pixel buffer.");
    return NULL;
  }

  NSVGrasterizer *rast = nsvgCreateRasterizer();
  if (!rast) {
    free(pixels);
    (*env)->DeleteLocalRef(env, image);
    nsvgDelete(svg_image);
    throw_exception(env, "java/lang/OutOfMemoryError",
                    "Failed to create SVG rasterizer.");
    return NULL;
  }

  nsvgRasterize(rast, svg_image, 0.0f, 0.0f, 1.0f, pixels, width, height,
                width * 4);
  nsvgDeleteRasterizer(rast);
  nsvgDelete(svg_image);

  int pixel_count = width * height;
  jintArray jPixels = (*env)->NewIntArray(env, pixel_count);
  if (!jPixels) {
    free(pixels);
    (*env)->DeleteLocalRef(env, image);
    throw_exception(env, "java/lang/OutOfMemoryError",
                    "Failed to allocate pixel array.");
    return NULL;
  }

  jint *dst = (*env)->GetIntArrayElements(env, jPixels, NULL);
  if (!dst) {
    free(pixels);
    (*env)->DeleteLocalRef(env, jPixels);
    (*env)->DeleteLocalRef(env, image);
    throw_exception(env, "java/lang/OutOfMemoryError",
                    "Failed to get pixel array elements.");
    return NULL;
  }

  for (int i = 0; i < pixel_count; i++) {
    unsigned char r = pixels[i * 4 + 0];
    unsigned char g = pixels[i * 4 + 1];
    unsigned char b = pixels[i * 4 + 2];
    unsigned char a = pixels[i * 4 + 3];
    dst[i] = ((jint)a << 24) | ((jint)r << 16) | ((jint)g << 8) | (jint)b;
  }

  (*env)->ReleaseIntArrayElements(env, jPixels, dst, 0);
  free(pixels);

  jclass bufferedImageClass2 = (*env)->GetObjectClass(env, image);
  jmethodID setRGB =
      (*env)->GetMethodID(env, bufferedImageClass2, "setRGB", "(IIII[III)V");

  (*env)->DeleteLocalRef(env, bufferedImageClass2);
  if (!setRGB) {
    (*env)->DeleteLocalRef(env, jPixels);
    (*env)->DeleteLocalRef(env, image);
    throw_exception(env, "java/lang/IllegalArgumentException",
                    "Failed to find BufferedImage.setRGB method.");
    return NULL;
  }

  (*env)->CallVoidMethod(env, image, setRGB, (jint)0, (jint)0, (jint)width,
                         (jint)height, jPixels, (jint)0, (jint)width);
  (*env)->DeleteLocalRef(env, jPixels);

  if ((*env)->ExceptionCheck(env)) {
    (*env)->DeleteLocalRef(env, image);
    return NULL;
  }

  return image;
}

JNIEXPORT jobject JNICALL Java_com_jules1univ_vectorcanvas_NativeCanvas_render(
    JNIEnv *env, jclass cls, jstring jxmlString) {

  char *xml_string = get_string(env, jxmlString);
  if (!xml_string) {
    throw_exception(env, "java/lang/IllegalArgumentException",
                    "Input string is null or invalid.");
    return NULL;
  }

  return render_svg_to_image(env, xml_string);
}
