LIBS  = 
CFLAGS = -std=gnu99 -Wall -Wpedantic -O0 -g #-mmacosx-version-min=10.9 #-fsanitize=undefined
#CFLAGS = -O3 -g
CC = gcc
LD = $(CC)
AR = ar

# Should be equivalent to your list of C files, if you don't build selectively
SRC = $(wildcard *.c)
OBJ = $(patsubst %.c, %.o, $(SRC))

all : uvmrt.a

clean :
	rm *.o uvmrt.a
	
uvmrt.a : $(OBJ)
	$(AR) rcs $@ $^
	
$(OBJ) : $(SRC)
	$(CC) -c $^ $(CFLAGS) $(LIBS)
