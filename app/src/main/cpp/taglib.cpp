#include <jni.h>
#include "taglib/taglib/fileref.h"
#include "taglib/taglib/tag.h"
#include <memory>
#include <android/log.h>
#include "toolkit/tfilestream.h"
#include "toolkit/tstringlist.h"
#include "tpropertymap.h"
#include "wav/wavproperties.h"
#include "flacproperties.h"
#include "mpegfile.h"
#include "flacfile.h"
#include "riff/wav/wavfile.h"
#include "dsffile.h"
#include "dsdifffile.h"

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

TagLib::File* createByExtension(const TagLib::String &fileName,
                                TagLib::IOStream *stream,
                                bool readProps = true,
                                TagLib::AudioProperties::ReadStyle style = TagLib::AudioProperties::Average)
{
    int dot = fileName.rfind(".");
    if(dot < 0) return nullptr;

    auto ext = fileName.substr(dot + 1).upper();

    if(ext == "MP3")
        return new TagLib::MPEG::File(stream, readProps, style);

    if(ext == "FLAC")
        return new TagLib::FLAC::File(stream, readProps, style);

    if(ext == "WAV")
        return new TagLib::RIFF::WAV::File(stream, readProps, style);

    if(ext == "DSF")
        return new TagLib::DSF::File(stream, readProps, style);

    if(ext == "DFF" || ext == "DSDIFF")
        return new TagLib::DSDIFF::File(stream, readProps, style);

    return nullptr;
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
Java_com_kamiruku_sonata_taglib_TagLib_getMetadata(JNIEnv *env,jobject thiz, jint fd, jstring jfileName) {
    const char *cFilename = env->GetStringUTFChars(jfileName, nullptr);
    TagLib::String filename(cFilename);
    env->ReleaseStringUTFChars(jfileName, cFilename);

    fd = dup(fd);
    lseek(fd, 0, SEEK_SET);

    auto stream = std::make_unique<TagLib::FileStream>(fd, true);
    TagLib::File *f = createByExtension(filename, stream.get());
    TagLib::FileRef file(f);

    jobject propertiesMap = propertyMapToHashMap(env, file.properties());
    return propertiesMap;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_kamiruku_sonata_taglib_TagLib_getAudioProperties(JNIEnv* env,jobject thiz, jint fd, jstring jfilename) {
    const char *cFilename = env->GetStringUTFChars(jfilename, nullptr);
    TagLib::String filename(cFilename);
    env->ReleaseStringUTFChars(jfilename, cFilename);

    fd = dup(fd);
    lseek(fd, 0, SEEK_SET);

    auto stream = std::make_unique<TagLib::FileStream>(fd, true);
    TagLib::File *f = createByExtension(filename, stream.get());
    TagLib::FileRef file(f);

    jint values[5] = {-1, -1, -1, -1, -1};

    if (!file.isNull()) {
        auto props = file.audioProperties();
        if (props) {
            values[0] = props->lengthInMilliseconds();
            values[1] = props->bitrate();
            values[2] = props->sampleRate();
            values[3] = props->channels();

            int bitsPerSample = -1;
            if (auto *wav = dynamic_cast<TagLib::RIFF::WAV::Properties*>(props)) {
                bitsPerSample = wav->bitsPerSample();
            } else if (auto *flac = dynamic_cast<TagLib::FLAC::Properties*>(props)) {
                bitsPerSample = flac->bitsPerSample();
            }

            values[4] = bitsPerSample;
        }
    }

    jintArray result = env->NewIntArray(5);
    env->SetIntArrayRegion(result, 0, 5, values);
    return result;
}