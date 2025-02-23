package chip8;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Screen extends JPanel {

    private Display display;
    // 1ピクセルあたりの描画サイズ
    private int scale;
    @SuppressWarnings("unused")
    private InputHandler inputHandler;

    public Screen(Display display, int scale, InputHandler inputHandler) {
        this.display = display;
        this.scale = scale;
        this.inputHandler = inputHandler;
        setPreferredSize(new Dimension(64 * scale, 32 * scale));

        // キーリスナーを追加
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int chip8Key = convertKey(e.getKeyCode());
                if (chip8Key != -1) {
                    inputHandler.keys[chip8Key] = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int chip8Key = convertKey(e.getKeyCode());
                if(chip8Key != -1) {
                    inputHandler.keys[chip8Key] = false;
                }
            }   
        });
        // キー入力を受け取るためにフォーカス可能にし、フォーカスを要求
        setFocusable(true);
        requestFocusInWindow();
    }

    // CHIP-8のキーに変換するためのメソッド
    private int convertKey(int keyCode) {
        switch(keyCode) {
            case KeyEvent.VK_1: return 0x1;
            case KeyEvent.VK_2: return 0x2;
            case KeyEvent.VK_3: return 0x3;
            case KeyEvent.VK_4: return 0xC;
            case KeyEvent.VK_Q: return 0x4;
            case KeyEvent.VK_W: return 0x5;
            case KeyEvent.VK_E: return 0x6;
            case KeyEvent.VK_R: return 0xD;
            case KeyEvent.VK_A: return 0x7;
            case KeyEvent.VK_S: return 0x8;
            case KeyEvent.VK_D: return 0x9;
            case KeyEvent.VK_F: return 0xE;
            case KeyEvent.VK_Z: return 0xA;
            case KeyEvent.VK_X: return 0x0;
            case KeyEvent.VK_C: return 0xB;
            case KeyEvent.VK_V: return 0xF;
            default: return -1;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 背景色を黒に指定
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 各ピクセルの状態に応じて描画
        g.setColor(Color.WHITE);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                if (display.pixels[x][y]) {
                    g.fillRect(x * scale, y * scale, scale, scale);
                }
            }
        }
    }
}
