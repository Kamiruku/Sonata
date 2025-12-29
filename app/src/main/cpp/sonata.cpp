#include <jni.h>
#include "taglib/taglib/fileref.h"
#include "taglib/taglib/tag.h"
#include <memory>
#include <android/log.h>
#include "toolkit/tfilestream.h"
#include "toolkit/tstringlist.h"
#include "tpropertymap.h"

jclass g_stringClass = nullptr;

jclass g_hashMapClass = nullptr;
jmethodID g_hashMapInit = nullptr;
jmethodID g_hashMapPut = nullptr;

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return JNI_ERR;

    auto cacheClass = [env](const char *name) {
        jclass tmp = env->FindClass(name);
        jclass global = (jclass)env->NewGlobalRef(tmp);
        env->DeleteLocalRef(tmp);
        return global;
    };

    g_stringClass = cacheClass("java/lang/String");
    g_hashMapClass = cacheClass("java/util/HashMap");

    g_hashMapInit = env->GetMethodID(g_hashMapClass, "<init>", "(I)V");
    g_hashMapPut = env->GetMethodID(g_hashMapClass, "put",
                                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    return JNI_VERSION_1_6;
}

jobjectArray strListToJniArray(JNIEnv *env, const TagLib::StringList &stringList) {
    jobjectArray array = env->NewObjectArray(stringList.size(),g_stringClass, nullptr);

    for (size_t i = 0; i < stringList.size(); ++i) {
        jstring str = env->NewStringUTF(stringList[i].toCString(true));
        env->SetObjectArrayElement(array, i, str);
        env->DeleteLocalRef(str);
    }
    return array;
}

jobject propertyMapToHashMap(JNIEnv *env, const TagLib::PropertyMap &propertyMap) {
    jobject map = env->NewObject(g_hashMapClass, g_hashMapInit, static_cast<jint>(propertyMap.size()));

    for (const auto& [key, values]: propertyMap) {
        jobjectArray valueArray = strListToJniArray(env, values);
        jstring keyStr = env->NewStringUTF(key.toCString(true));

        env->CallObjectMethod(map, g_hashMapPut, keyStr, valueArray);

        env->DeleteLocalRef(keyStr);
        env->DeleteLocalRef(valueArray);
    }

    return map;
}

extern "C"
JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return;

    env->DeleteGlobalRef(g_stringClass);
    env->DeleteGlobalRef(g_hashMapClass);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_kamiruku_sonata_taglib_TagLib_getMetadata(JNIEnv *env,jobject thiz,jint fd) {
    fd = dup(fd);
    auto stream = std::make_unique<TagLib::FileStream>(fd, true);
    TagLib::FileRef file(stream.get(), true);

    jobject propertiesMap = propertyMapToHashMap(env, file.properties());
    return propertiesMap;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_kamiruku_sonata_taglib_TagLib_getAudioProperties(JNIEnv* env,jobject thiz, jint fd) {
    fd = dup(fd);
    auto stream = std::make_unique<TagLib::FileStream>(fd, true);
    TagLib::FileRef file(stream.get(), true);

    jint values[4] = {0, 0, 0, 0};

    if (!file.isNull()) {
        auto props = file.audioProperties();
        if (props) {
            values[0] = props->lengthInMilliseconds();
            values[1] = props->bitrate();
            values[2] = props->sampleRate();
            values[3] = props->channels();
        }
    }

    jintArray result = env->NewIntArray(4);
    env->SetIntArrayRegion(result, 0, 4, values);
    return result;
}