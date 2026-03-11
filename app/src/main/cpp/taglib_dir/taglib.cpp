#include <jni.h>
#include "taglib/taglib/fileref.h"
#include "taglib/taglib/tag.h"
#include <memory>
#include "toolkit/tfilestream.h"
#include "tpropertymap.h"
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

jclass g_pictureObjectClass = nullptr;
jmethodID g_pictureObjectCtor = nullptr;

jclass g_tagLibObjectClass = nullptr;
jmethodID g_tagLibObjectCtor = nullptr;

jobjectArray strListToJniArray(JNIEnv *env, const TagLib::StringList &stringList);
jobject propertyMapToHashMap(JNIEnv *env, const TagLib::PropertyMap &propertyMap);
TagLib::File* createByExtension(const TagLib::String &fileName, TagLib::IOStream *stream, bool readProps = true, TagLib::AudioProperties::ReadStyle style = TagLib::AudioProperties::Average);

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return JNI_ERR;

    auto cacheClass = [env](const char *name) {
        jclass tmp = env->FindClass(name);
        auto global = (jclass)env->NewGlobalRef(tmp);
        env->DeleteLocalRef(tmp);
        return global;
    };

    g_stringClass = cacheClass("java/lang/String");
    g_hashMapClass = cacheClass("java/util/HashMap");
    g_tagLibObjectClass = cacheClass("com/kamiruku/sonata/taglib/TagLibObject");
    g_pictureObjectClass = cacheClass("com/kamiruku/sonata/taglib/PictureObject");

    g_hashMapInit = env->GetMethodID(g_hashMapClass, "<init>", "(I)V");
    g_hashMapPut = env->GetMethodID(g_hashMapClass, "put",
                                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    g_pictureObjectCtor = env->GetMethodID(
            g_pictureObjectClass,
            "<init>",
            "([BLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

    g_tagLibObjectCtor = env->GetMethodID(
            g_tagLibObjectClass,
            "<init>",
            "(IIIIILjava/util/HashMap;)V");

    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return;

    env->DeleteGlobalRef(g_stringClass);
    env->DeleteGlobalRef(g_hashMapClass);
    env->DeleteGlobalRef(g_tagLibObjectClass);
    env->DeleteGlobalRef(g_pictureObjectClass);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_kamiruku_sonata_taglib_TagLib_getDetails(JNIEnv *env, jobject thiz, jint fd, jstring jfileName) {
    const char *cFileName = env->GetStringUTFChars(jfileName, nullptr);
    TagLib::String filename(cFileName);
    env->ReleaseStringUTFChars(jfileName, cFileName);

    fd = dup(fd);
    lseek(fd, 0, SEEK_SET);

    auto stream = std::make_unique<TagLib::FileStream>(fd, true);
    TagLib::File *f = createByExtension(filename, stream.get());
    TagLib::FileRef file(f);

    if (!file.isNull()) {
        auto props = file.audioProperties();
        jint length { -1 }, bitrate { -1 }, sampleRate { -1 }, channels { -1 }, bitsPerSample { -1 };

        if (props) {
            length = props->lengthInMilliseconds();
            bitrate = props->bitrate();
            sampleRate = props->sampleRate();
            channels = props->channels();

            if (auto *wav = dynamic_cast<TagLib::RIFF::WAV::Properties*>(props)) {
                bitsPerSample = wav->bitsPerSample();
            } else if (auto *flac = dynamic_cast<TagLib::FLAC::Properties*>(props)) {
                bitsPerSample = flac->bitsPerSample();
            } else if (auto *dsd = dynamic_cast<TagLib::DSDIFF::Properties*>(props)) {
                bitsPerSample = dsd->bitsPerSample();
            } else if (auto *dsf = dynamic_cast<TagLib::DSF::Properties*>(props)) {
                bitsPerSample = dsf->bitsPerSample();
            }
        }

        jobject propertyMap = propertyMapToHashMap(env, file.properties());

        jobject tagLibObj = env->NewObject(
                g_tagLibObjectClass,
                g_tagLibObjectCtor,
                length,
                bitrate,
                sampleRate,
                channels,
                bitsPerSample,
                propertyMap
        );

        env->DeleteLocalRef(propertyMap);
        return tagLibObj;
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_kamiruku_sonata_taglib_TagLib_getAlbumArt(JNIEnv *env, jobject thiz, jint fd, jstring jfileName) {
    const char *cFileName = env->GetStringUTFChars(jfileName, nullptr);
    TagLib::String filename(cFileName);
    env->ReleaseStringUTFChars(jfileName, cFileName);

    fd = dup(fd);
    lseek(fd, 0, SEEK_SET);

    auto stream = std::make_unique<TagLib::FileStream>(fd, true);
    TagLib::File *f = createByExtension(filename, stream.get());
    TagLib::FileRef file(f);

    if (file.isNull()) {
        return nullptr;
    }

    if (file.complexProperties("PICTURE").size() == 0) {
        return nullptr;
    }

    auto pictureList = file.complexProperties("PICTURE");

    jobjectArray jPictureArray = env->NewObjectArray(static_cast<jsize>(pictureList.size()),g_pictureObjectClass, nullptr);

    //https://taglib.org/api/classTagLib_1_1FileRef.html#a7ac19ab017f0372e272d9eaa1ce6c574

    for (int i = 0; i < pictureList.size(); i++) {
        const auto& picture = pictureList[i];

        const auto& data = picture["data"].toByteVector();
        const auto& description = picture["description"].toString();
        const auto& pictureType = picture["pictureType"].toString();
        const auto& mimeType = picture["mimeType"].toString();

        if (data.isEmpty()) {
            env->SetObjectArrayElement(jPictureArray, i, nullptr);
            continue;
        }

        const auto& jData = env->NewByteArray(static_cast<jint>(data.size()));
        const auto& jDescription = env->NewStringUTF(description.toCString());
        const auto& jPictureType = env->NewStringUTF(pictureType.toCString());
        const auto& jMimeType = env->NewStringUTF(mimeType.toCString());

        env->SetByteArrayRegion(
                jData,
                0,
                static_cast<jint>(data.size()),
                reinterpret_cast<const jbyte *>(data.data())
        );

        jobject pictureObj = env->NewObject(
                g_pictureObjectClass,
                g_pictureObjectCtor,
                jData,
                jDescription,
                jPictureType,
                jMimeType
        );

        env->SetObjectArrayElement(jPictureArray, i, pictureObj);
        env->DeleteLocalRef(jData);
        env->DeleteLocalRef(jDescription);
        env->DeleteLocalRef(jPictureType);
        env->DeleteLocalRef(jMimeType);
        env->DeleteLocalRef(pictureObj);
    }

    return jPictureArray;
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

    jint values[5] {-1, -1, -1, -1, -1};

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

jobjectArray strListToJniArray(JNIEnv *env, const TagLib::StringList &stringList) {
    jobjectArray array = env->NewObjectArray(static_cast<jint>(stringList.size()), g_stringClass, nullptr);

    for (size_t i { 0 }; i < stringList.size(); i++) {
        jstring str = env->NewStringUTF(stringList[i].toCString(true));
        env->SetObjectArrayElement(array, static_cast<jint>(i), str);
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
                                bool readProps,
                                TagLib::AudioProperties::ReadStyle style)
{
    int dot = fileName.rfind(".");
    if (dot < 0) return nullptr;

    auto ext = fileName.substr(dot + 1).upper();

    if (ext == "MP3")
        return new TagLib::MPEG::File(stream, readProps, style);

    if (ext == "FLAC")
        return new TagLib::FLAC::File(stream, readProps, style);

    if (ext == "WAV")
        return new TagLib::RIFF::WAV::File(stream, readProps, style);

    if (ext == "DSF")
        return new TagLib::DSF::File(stream, readProps, style);

    if (ext == "DFF" || ext == "DSDIFF")
        return new TagLib::DSDIFF::File(stream, readProps, style);

    return nullptr;
}