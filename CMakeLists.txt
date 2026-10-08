cmake_minimum_required(VERSION 3.16)

project(Remembirth
    VERSION 1.0
    LANGUAGES CXX
)

set(BUILD_SHARED_LIBS OFF)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_executable(main
    src/main.cpp
)

# --- Static linking ---
if(MINGW)
    # Windows and MinGW: all static (libgcc, libstdc++, winpthread)
    target_link_options(main PRIVATE -static -static-libgcc -static-libstdc++)
elseif(UNIX AND NOT APPLE)
    # Linux: static binary
    target_link_options(main PRIVATE -static)
endif()
