#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define KEY 58

int main(int argc, char *argv[]) {
    if (argc != 3) {
        printf("Usage: %s <input.okk> <output.ogg>\n", argv[0]);
        return 1;
    }

    FILE *input = fopen(argv[1], "rb");
    if (!input) {
        printf("Error: Cannot open input file %s\n", argv[1]);
        return 1;
    }

    FILE *output = fopen(argv[2], "wb");
    if (!output) {
        printf("Error: Cannot create output file %s\n", argv[2]);
        fclose(input);
        return 1;
    }

    int byte;
    while ((byte = fgetc(input)) != EOF) {
        // Decrypt by XOR with key (same operation for encrypt/decrypt)
        byte = byte ^ KEY;
        fputc(byte, output);
    }

    fclose(input);
    fclose(output);

    printf("Successfully converted %s to %s\n", argv[1], argv[2]);
    return 0;
}