package chip8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chip8 {

    private static final Logger logger = LoggerFactory.getLogger(Chip8.class);

    private CPU cpu;
    private Memory memory;
    private Display display;
    private InputHandler inputHandler;

    public Chip8() {
        memory = new Memory();
        display = new Display();
        inputHandler = new InputHandler();
        cpu = new CPU(memory, display, inputHandler);
        initialize();
    }

    // 初期化
    private void initialize() {
        // フォントセットのロード
        memory.loadDefaultFontSet();
    }

    // ROMの読み込み
    public void loadROM(String path) {
        memory.loadROM(path);
    }

    // 処理のスタート
    public void start() {
        while(true) {
            cpu.executeCycle();
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                logger.error("Main loop sleep interrupted", e);
            }
        }
    }

    // ディスプレイの取得
    public Display getDisplay() {
        return display;
    }

    // インプットハンドラーの取得
    public InputHandler getInputHandler() {
        return inputHandler;
    }
}