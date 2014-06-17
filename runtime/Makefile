C_FILES := $(wildcard **/*.c)
OBJ_FILES := $(addprefix obj/, $(notdir $(C_FILES:.c=.o)))
CC_FLAGS := -O3
CC := gcc

all : dir $(OBJ_FILES)

dir:
	mkdir -p obj

$(OBJ_FILES) : $(C_FILES)
	$(CC) $(CC_FLAGS) -c -o $@ $<
