package chip8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Memory {

    private static final Logger logger = LoggerFactory.getLogger(Memory.class);

    private byte[] memory = new byte[4096];
    public static final int SPRITE_START_ADDRESS = 0x000;

    public void loadDefaultFontSet() {
        byte[] fontset = {
            (byte)0xF0, (byte)0x90, (byte)0x90, (byte)0x90, (byte)0xF0, // 0
            (byte)0x20, (byte)0x60, (byte)0x20, (byte)0x20, (byte)0x70, // 1
            (byte)0xF0, (byte)0x10, (byte)0xF0, (byte)0x80, (byte)0xF0, // 2
            (byte)0xF0, (byte)0x10, (byte)0xF0, (byte)0x10, (byte)0xF0, // 3
            (byte)0x90, (byte)0x90, (byte)0xF0, (byte)0x10, (byte)0x10, // 4
            (byte)0xF0, (byte)0x80, (byte)0xF0, (byte)0x10, (byte)0xF0, // 5
            (byte)0xF0, (byte)0x80, (byte)0xF0, (byte)0x90, (byte)0xF0, // 6
            (byte)0xF0, (byte)0x10, (byte)0x20, (byte)0x40, (byte)0x40, // 7
            (byte)0xF0, (byte)0x90, (byte)0xF0, (byte)0x90, (byte)0xF0, // 8
            (byte)0xF0, (byte)0x90, (byte)0xF0, (byte)0x10, (byte)0xF0, // 9
            (byte)0xF0, (byte)0x90, (byte)0xF0, (byte)0x90, (byte)0x90, // A
            (byte)0xE0, (byte)0x90, (byte)0xE0, (byte)0x90, (byte)0xE0, // B
            (byte)0xF0, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0xF0, // C
            (byte)0xE0, (byte)0x90, (byte)0x90, (byte)0x90, (byte)0xE0, // D
            (byte)0xF0, (byte)0x80, (byte)0xF0, (byte)0x80, (byte)0xF0, // E
            (byte)0xF0, (byte)0x80, (byte)0xF0, (byte)0x80, (byte)0x80  // F
        };
        // フォントセットをメモリの先頭にロード
        System.arraycopy(fontset, 0, memory, 0, fontset.length);
    }

    public void loadROM(String filepath) {
        try {
            // ファイルから全バイトを読み込む
            byte[] romData = Files.readAllBytes(Paths.get(filepath));
            // メモリ容量のチェック
            if (romData.length + 0x200 > memory.length) {
                logger.error("ROM size exceeds memory capacity");
                return;
            }
            // ROMをメモリにコピー
            System.arraycopy(romData, 0, memory, 0x200, romData.length);
        } catch (IOException e) {
            logger.error("Failed to load ROM", e);
        }
    }

    public void setByte(int address, byte value) {
        memory[address] = value;
    }

    public byte getByte(int address) {
        return memory[address];
    }
}