cmake_minimum_required(VERSION 3.10)
project(OkkTools)

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra")

# Add executables
add_executable(ogg2okk ogg2okk.c)
add_executable(okk2ogg okk2ogg.c)

# Link libraries if needed
# target_link_libraries(ogg2okk ${LIBS})
# target_link_libraries(okk2ogg ${LIBS})