LIBS  = 
CFLAGS = -std=gnu99 -Wzero-length-array -Wall -Wpedantic -O0 -g #-mmacosx-version-min=10.9 #-fsanitize=undefined
#CFLAGS = -O3 -g
CC = gcc
LD = $(CC)
AR = ar

# Should be equivalent to your list of C files, if you don't build selectively
SRC_SHARED = bitmap.c collector.c heap.c Immix.c other.c runtime.c stack.c thread.c typeinfo_static.c
SRC_EXE    = executable.c
OBJ_SHARED = $(patsubst %.c, %.o, $(SRC_SHARED))
OBJ_EXE    = $(patsubst %.c, %.o, $(SRC_EXE))
HEADER = $(wildcard *.h)

all : uvmrt.a uvmshared.a uvmexe.a

clean :
	rm *.o uvmrt.a
	
uvmrt.a : $(OBJ_SHARED) $(OBJ_EXE)
	$(AR) rcs $@ $^

uvmshared.a : $(OBJ_SHARED)
	$(AR) rcs $@ $^

uvmexe.a : $(OBJ_EXE)
	$(AR) rcs $@ $^

$(OBJ) : $(SRC) $(HEADER)
	$(CC) -c $(SRC) $(CFLAGS) $(LIBS)
