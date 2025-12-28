cmake_minimum_required(VERSION 3.22.1)

project(sonata)

add_subdirectory(taglib)

include_directories(
        taglib/taglib
        taglib/taglib/dsdiff
        taglib/taglib/dsf
        taglib/taglib/flac
        taglib/taglib/ogg
        taglib/taglib/wavpack
        taglib/taglib/mpeg
        taglib/taglib/toolkit
)

add_library(${CMAKE_PROJECT_NAME}
        SHARED
        sonata.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    tag
    log
)
