cmake_minimum_required(VERSION 3.16)
project(vectorcanvas C)

set(CMAKE_C_STANDARD 11)

find_package(Java REQUIRED)
find_package(JNI REQUIRED)

message(STATUS "JNI includes : ${JNI_INCLUDE_DIRS}")
message(STATUS "JNI libs     : ${JNI_LIBRARIES}")

add_subdirectory(lib/nanosvg)

file(GLOB_RECURSE SOURCES src/*.c)

add_library(vectorcanvas SHARED ${SOURCES})

target_include_directories(vectorcanvas PRIVATE
    include/
    lib/nanosvg/src/
    ${JNI_INCLUDE_DIRS}
)

target_link_libraries(vectorcanvas PRIVATE
    nanosvg
    nanosvgrast
    ${JNI_LIBRARIES}
)

if(UNIX)
    target_link_libraries(vectorcanvas PRIVATE m)
endif()

if(CMAKE_SYSTEM_NAME STREQUAL "Linux")
    set_target_properties(vectorcanvas PROPERTIES
        OUTPUT_NAME "vectorcanvas"
        PREFIX      "lib"
        SUFFIX      ".so"
    )
    target_compile_options(vectorcanvas PRIVATE -Wall -Wextra -O2 -fPIC)

elseif(CMAKE_SYSTEM_NAME STREQUAL "Windows")
    set_target_properties(vectorcanvas PROPERTIES
        OUTPUT_NAME            "vectorcanvas"
        PREFIX                 ""
        SUFFIX                 ".dll"
        WINDOWS_EXPORT_ALL_SYMBOLS ON
    )
      target_compile_options(vectorcanvas PRIVATE
        /W3
        $<$<NOT:$<CONFIG:Debug>>:/O2> # disable /O2 in debug
    )

elseif(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
    set_target_properties(vectorcanvas PROPERTIES
        OUTPUT_NAME "vectorcanvas"
        PREFIX      "lib"
        SUFFIX      ".dylib"
    )
    target_compile_options(vectorcanvas PRIVATE -Wall -Wextra -O2 -fPIC)
endif()

if(CMAKE_SYSTEM_NAME STREQUAL "Linux")
    set(NATIVE_OS "linux")
elseif(CMAKE_SYSTEM_NAME STREQUAL "Windows")
    set(NATIVE_OS "windows")
elseif(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
    set(NATIVE_OS "macos")
endif()

if(CMAKE_SYSTEM_PROCESSOR MATCHES "aarch64|arm64|ARM64")
    set(NATIVE_ARCH "aarch64")
else()
    set(NATIVE_ARCH "x86_64")
endif()


set(NATIVE_OUT_DIR "${CMAKE_SOURCE_DIR}/../java/lib/native/${NATIVE_OS}-${NATIVE_ARCH}")

set_target_properties(vectorcanvas PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${NATIVE_OUT_DIR}"
    RUNTIME_OUTPUT_DIRECTORY "${NATIVE_OUT_DIR}"
    ARCHIVE_OUTPUT_DIRECTORY "${NATIVE_OUT_DIR}"
)