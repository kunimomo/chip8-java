package chip8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPU {

    private static final Logger logger = LoggerFactory.getLogger(CPU.class);

    private Memory memory;
    private Display display;
    private InputHandler inputHandler;

    // 汎用レジスタ
    private int[] V = new int[16];
    // インデックスレジスタ
    private int I;
    // プログラムカウンタ
    private int pc;

    // スタック・スタックポインタ
    private int[] stack = new int[16];
    private int sp;

    // タイマー
    private int delayTimer;
    private int soundTimer;

    public CPU(Memory memory, Display display, InputHandler inputHandler) {
        this.memory = memory;
        this.display = display;
        this.inputHandler = inputHandler;
        reset();
    }

    // CPUの初期状態を設定
    public void reset() {
        for (int i = 0; i < 16; i++) {
            V[i] = 0;
        }
        I = 0;
        pc = 0x200;
        sp = 0;
        delayTimer = 0;
        soundTimer = 0;
        for (int i = 0; i < stack.length; i++) {
            stack[i] = 0;
        }
    }

    // 一サイクルの処理
    public void executeCycle() {
        int opcode = fetchOpcode();
        decodeAndExecute(opcode);
        updateTimers();
    }

    // プログラムカウンタの位置から2バイト分読み取り、オペコードに変換
    private int fetchOpcode() {
        int highByte = Byte.toUnsignedInt(memory.getByte(pc));
        int lowByte = Byte.toUnsignedInt(memory.getByte(pc + 1));
        // 上位ビットを8ビット分左にシフトして、下位ビットとOR演算する
        return (highByte << 8) | lowByte;
    }

    // オペコードのデコードと実行
    private void decodeAndExecute(int opcode) {
        // 第2ニブル
        int x;
        // 第3ニブル
        int y;
        // 下位8ビット
        int kk;
        // アドレス
        int addr;
        // 下位4ビット
        int nibble;
        // オペコードの上位四ビット
        switch (opcode & 0xF000) {
            case 0x0000:
                // 00E0: CLS - 画面クリア
                if (opcode == 0x00E0) { 
                    display.clear();
                    pc += 2;
                // 00EE: RET - サブルーチンから戻る
                } else if (opcode == 0x00EE) { 
                    sp--;
                    pc = stack[sp];
                    pc += 2;
                // 0nnn: SYS addr (未使用)
                } else { 
                    pc += 2;
                }
                break;
            // 1nnn: JP addr - アドレスnnnにジャンプ
            case 0x1000:
                addr = opcode & 0x0FFF;
                pc = addr;
                break;
            // 2nnn: CALL addr - サブルーチンの呼び出し
            case 0x2000:
                addr = opcode & 0x0FFF;
                stack[sp] = pc;
                sp++;
                pc = addr;
                break;
            // 3xkk: SE Vx, byte - Vx == kk の場合、次の命令をスキップ
            case 0x3000:
                x = (opcode & 0x0F00) >> 8;
                kk = opcode & 0x00FF;
                pc += (V[x] == kk) ? 4 : 2;
                break;
            // 4xkk: SNE Vx, byte - Vx != kk の場合、次の命令をスキップ
            case 0x4000:
                x = (opcode & 0x0F00) >> 8;
                kk = opcode & 0x00FF;
                pc += (V[x] != kk) ? 4 : 2;
                break;
            // 5xy0: SE Vx, Vy - Vx == Vy の場合、次の命令をスキップ
            case 0x5000:
                x = (opcode & 0x0F00) >> 8;
                y = (opcode & 0x00F0) >> 4;
                pc += (V[x] == V[y]) ? 4 : 2;
                break;
            // 6xkk: LD Vx, byte - Vxに定数kkをセット
            case 0x6000:
                x = (opcode & 0x0F00) >> 8;
                kk = opcode & 0x00FF;
                V[x] = kk;
                pc += 2;
                break;
            // 7xkk: ADD Vx, byte - Vxにkkを加算
            case 0x7000: 
                x = (opcode & 0x0F00) >> 8;
                kk = opcode & 0x00FF;
                V[x] = (V[x] + kk) & 0xFF;
                pc += 2;
                break;
            case 0x8000:
                x = (opcode & 0x0F00) >> 8;
                y = (opcode & 0x00F0) >> 4;
                switch (opcode & 0x000F) {
                    // 8xy0: LD Vx, Vy
                    case 0x0: 
                        V[x] = V[y];
                        break;
                    // 8xy1: OR Vx, Vy
                    case 0x1:
                        V[x] |= V[y];
                        break;
                    // 8xy2: AND Vx, Vy
                    case 0x2:
                        V[x] &= V[y];
                        break;
                    // 8xy3: XOR Vx, Vy
                    case 0x3:
                        V[x] ^= V[y];
                        break;
                    // 8xy4: ADD Vx, Vy, VF = carry
                    case 0x4:
                        int sum = V[x] + V[y];
                        V[0xF] = (sum > 0xFF) ? 1 : 0;
                        V[x] = sum & 0xFF;
                        break;
                    // 8xy5: SUB Vx, Vy, VF = NOT borrow
                    case 0x5:
                        V[0xF] = (V[x] > V[y]) ? 1 : 0;
                        V[x] = (V[x] - V[y]) & 0xFF;
                        break;
                    // 8xy6: SHR Vx {, Vy}
                    case 0x6:
                        V[0xF] = V[x] & 0x1;
                        V[x] = V[x] >> 1;
                        break;
                    // 8xy7: SUBN Vx, Vy, VF = NOT borrow
                    case 0x7:
                        V[0xF] = (V[y] > V[x]) ? 1 : 0;
                        V[x] = (V[y] - V[x]) & 0xFF;
                        break;
                    // 8xyE: SHL Vx {, Vy}
                    case 0xE:
                        V[0xF] = (V[x] & 0x80) >> 7;
                        V[x] = (V[x] << 1) & 0xFF;
                        break;
                    default:
                        logger.error("Unknown opcode: 0x%X\n", opcode);
                        break;
                }
                pc += 2;
                break;
            // 9xy0: SNE Vx, Vy - Vx != Vy の場合、次の命令をスキップ
            case 0x9000:
                x = (opcode & 0x0F00) >> 8;
                y = (opcode & 0x00F0) >> 4;
                pc += (V[x] != V[y]) ? 4 : 2;
                break;
            // Annn: LD I, addr - Iにnnnをセット
            case 0xA000:
                I = opcode & 0x0FFF;
                pc += 2;
                break;
            // Bnnn: JP V0, addr - V0 + nnnにジャンプ
            case 0xB000:
                pc = (opcode & 0x0FFF) + V[0];
                break;
            // Cxkk: RND Vx, byte - Vxに乱数 AND kkをセット
            case 0xC000:
                x = (opcode & 0x0F00) >> 8;
                kk = opcode & 0x00FF;
                V[x] = (int)(Math.random() * 256) & kk;
                pc += 2;
                break;
            // Dxyn: DRW Vx, Vy, nibble - スプライト描画
            case 0xD000:
                x = (opcode & 0x0F00) >> 8;
                y = (opcode & 0x00F0) >> 4;
                nibble = opcode & 0x000F;
                boolean collision = display.drawSprite(V[x], V[y], nibble, I, memory);
                V[0xF] = collision ? 1 : 0;
                pc += 2;
                break;
            case 0xE000:
                x = (opcode & 0x0F00) >> 8;
                switch (opcode & 0x00FF) {
                    // Ex9E: SKP Vx - キーが押されていればスキップ
                    case 0x9E:
                        pc += (inputHandler.isKeyPressed(V[x])) ? 4 : 2;
                        break;
                    // ExA1: SKNP Vx - キーが押されていなければスキップ
                    case 0xA1:
                        pc += (!inputHandler.isKeyPressed(V[x])) ? 4 : 2;
                        break;
                    default:
                        System.err.printf("Unknown opcode: 0x%X\n", opcode);
                        pc += 2;
                        break;
                }
                break;
            case 0xF000:
                x = (opcode & 0x0F00) >> 8;
                switch (opcode & 0x00FF) {
                    // Fx07: LD Vx, DT
                    case 0x07:
                        V[x] = delayTimer;
                        pc += 2;
                        break;
                    // Fx0A: LD Vx, K - キー入力待ち
                    case 0x0A:
                        V[x] = inputHandler.waitForKeyPress();
                        pc += 2;
                        break;
                    // Fx15: LD DT, Vx
                    case 0x15:
                        delayTimer = V[x];
                        pc += 2;
                        break;
                    // Fx18: LD ST, Vx
                    case 0x18:
                        soundTimer = V[x];
                        pc += 2;
                        break;
                    // Fx1E: ADD I, Vx
                    case 0x1E:
                        I += V[x];
                        pc += 2;
                        break;
                    // Fx29: LD F, Vx - フォントスプライトのアドレスをIにセット
                    case 0x29:
                        I = Memory.SPRITE_START_ADDRESS + (V[x] * 5);
                        pc += 2;
                        break;
                    // Fx33: LD B, Vx - BCD変換してメモリに格納
                    case 0x33:
                        memory.setByte(I, (byte)(V[x] / 100));
                        memory.setByte(I + 1, (byte)((V[x] / 10) % 10));
                        memory.setByte(I + 2, (byte)(V[x] % 10));
                        pc += 2;
                        break;
                    // Fx55: LD [I], V0～Vx
                    case 0x55:
                        for (int i = 0; i <= x; i++) {
                            memory.setByte(I + i, (byte) V[i]);
                        }
                        pc += 2;
                        break;
                    // Fx65: LD V0～Vx, [I]
                    case 0x65:
                        for (int i = 0; i <= x; i++) {
                            V[i] = Byte.toUnsignedInt(memory.getByte(I + i));
                        }
                        pc += 2;
                        break;
                    default:
                        System.err.printf("Unknown opcode: 0x%X\n", opcode);
                        pc += 2;
                        break;
                }
                break;
            default:
                System.err.printf("Unknown opcode: 0x%X\n", opcode);
                pc += 2;
                break;
        }
    }

    private void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
            if (soundTimer == 0) {
                // TODO: 未実装
            }
        }
    }
}
