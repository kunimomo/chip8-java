package chip8;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        
        Chip8 chip8 = new Chip8();
        if (args.length > 0) {
            chip8.loadROM(args[0]);
        } else {
            System.out.println("Please provide a ROM file path as a command line argument.");
            System.exit(1);
        }

        // 画面表示
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CHIP-8 Emulator");
            Screen screen = new Screen(chip8.getDisplay(), 10, chip8.getInputHandler());
            frame.add(screen);
            // フレームサイズの自動調整
            frame.pack();
            // ウインドウを閉じた時にアプリケーションを終了
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // フレームを表示状態にする
            frame.setVisible(true);
        });

        // 処理の開始
        new Thread(() -> {
            chip8.start();
        }).start();
    }
}
