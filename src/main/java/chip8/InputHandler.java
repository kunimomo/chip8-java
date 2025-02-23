package chip8;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputHandler {

    private static final Logger logger = LoggerFactory.getLogger(InputHandler.class);
    Scanner scanner = new Scanner(System.in);
    // 16キーの状態
    public boolean[] keys = new boolean[16];

    // キーが押された時
    public boolean isKeyPressed(int keyValue) {
        if (keyValue < 0 || keyValue > 15) {
            return false;
        }
        return keys[keyValue];
    }

    // キー入力
    public int waitForKeyPress() {
        String input = scanner.nextLine().trim();
        try {
            int key = Integer.parseInt(input , 16);
            if (key >= 0 && key < 16) {
                keys[key] = true;
            }
            return key;
        } catch(NumberFormatException e) {
            logger.error("Invalid key input");
            return 0;
        }
    }
}
