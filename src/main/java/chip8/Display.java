package chip8;

public class Display {

    // 64x32 のピクセル配列
    public boolean[][] pixels = new boolean[64][32];
    // 描画フラグ
    public boolean drawFlag = false;

    // 画面クリア
    public void clear() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                pixels[x][y] = false;
            }
        }
        drawFlag = true;
    }

    // スプライト描画
    public boolean drawSprite(int vx, int vy, int height, int spriteAddress, Memory memory) {
        boolean collision = false;
        for (int row = 0; row < height; row++) {
            byte spriteByte = memory.getByte(spriteAddress + row);
            for (int col = 0; col < 8; col++) {
                int x = (vx + col) % 64;
                int y = (vy + row) % 32;
                boolean spritePixel = ((spriteByte >> (7 - col)) & 0x01) == 1;
                boolean oldPixel = pixels[x][y];
                boolean newPixel = oldPixel ^ spritePixel;
                pixels[x][y] = newPixel;
                if (oldPixel && !newPixel) {
                    collision = true;
                }
            }
        }
        drawFlag = true;
        return collision;
    }
}
