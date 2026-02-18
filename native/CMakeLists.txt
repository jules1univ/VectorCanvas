cmake_minimum_required(VERSION 3.16)
project(nativecanvas C)

set(CMAKE_C_STANDARD 11)

find_package(Java REQUIRED)
find_package(JNI REQUIRED)

if(NOT JNI_FOUND)
    message(FATAL_ERROR "JDK introuvable. Définissez JAVA_HOME ou JNI_INCLUDE_DIRS.")
endif()

message(STATUS "JNI includes : ${JNI_INCLUDE_DIRS}")
message(STATUS "JNI libs     : ${JNI_LIBRARIES}")

set(SOURCES
    src/nativecanvas.c
    src/drawing.c
)

set(HEADERS
    include/
)

add_library(nativecanvas SHARED ${SOURCES})

target_include_directories(nativecanvas PRIVATE
    include/
    ${JNI_INCLUDE_DIRS}
)

target_link_libraries(nativecanvas PRIVATE
    ${JNI_LIBRARIES}
    m 
)

if(CMAKE_SYSTEM_NAME STREQUAL "Linux")
    set_target_properties(nativecanvas PROPERTIES
        OUTPUT_NAME   "nativecanvas"
        PREFIX        "lib"
        SUFFIX        ".so"
    )

    target_compile_options(nativecanvas PRIVATE -Wall -Wextra -O2 -fPIC)

elseif(CMAKE_SYSTEM_NAME STREQUAL "Windows")
    set_target_properties(nativecanvas PROPERTIES
        OUTPUT_NAME   "nativecanvas"
        PREFIX        ""
        SUFFIX        ".dll"
    )

    target_compile_options(nativecanvas PRIVATE /W3 /O2)

    set_target_properties(nativecanvas PROPERTIES
        WINDOWS_EXPORT_ALL_SYMBOLS ON
    )
endif()

if(CMAKE_SYSTEM_NAME STREQUAL "Linux")
    set(NATIVE_OS "linux")
elseif(CMAKE_SYSTEM_NAME STREQUAL "Windows")
    set(NATIVE_OS "windows")
elseif(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
    set(NATIVE_OS "macos")
endif()

if(CMAKE_SYSTEM_PROCESSOR MATCHES "aarch64|arm64")
    set(NATIVE_ARCH "aarch64")
else()
    set(NATIVE_ARCH "x86_64")
endif()

set(NATIVE_OUT_DIR "${CMAKE_SOURCE_DIR}/../java/libs/native/${NATIVE_OS}-${NATIVE_ARCH}")

set_target_properties(nativecanvas PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${NATIVE_OUT_DIR}"  
    RUNTIME_OUTPUT_DIRECTORY "${NATIVE_OUT_DIR}"  
    ARCHIVE_OUTPUT_DIRECTORY "${NATIVE_OUT_DIR}"  
)

message(STATUS "Output dir : ${NATIVE_OUT_DIR}")

option(BUILD_JAR "Reconstruire le JAR après compilation" OFF)

if(BUILD_JAR)
    find_program(JAR_TOOL jar HINTS "${Java_HOME}/bin" "$ENV{JAVA_HOME}/bin")
    find_program(JAVAC_TOOL javac HINTS "${Java_HOME}/bin" "$ENV{JAVA_HOME}/bin")

    if(JAR_TOOL AND JAVAC_TOOL)
        add_custom_command(TARGET nativecanvas POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E echo "==> Compilation Java..."
            COMMAND ${JAVAC_TOOL} -d ${CMAKE_SOURCE_DIR}/../java/out/classes
                    ${CMAKE_SOURCE_DIR}/../java/src/com/example/nativecanvas/NativeCanvas.java
                    ${CMAKE_SOURCE_DIR}/../java/src/com/example/nativecanvas/NativeLibLoader.java
            COMMAND ${CMAKE_COMMAND} -E echo "==> Packaging JAR..."
            COMMAND ${JAR_TOOL} cf
                    ${CMAKE_SOURCE_DIR}/../java/out/nativecanvas-1.0.0.jar
                    -C ${CMAKE_SOURCE_DIR}/../java/out/classes .
                    -C ${CMAKE_SOURCE_DIR}/../java/libs .
            COMMENT "JAR reconstruit dans java/out/nativecanvas-1.0.0.jar"
        )
    else()
        message(WARNING "jar/javac introuvables, BUILD_JAR ignoré.")
    endif()
endif()