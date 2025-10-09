package pr1;

import java.util.List;
import java.util.concurrent.Semaphore;

public class ChessGame {

    public static void main(String[] args) {
        System.out.println("Початок гри у шахи (симуляція двох гравців)...\n");

        Semaphore whiteSemaphore = new Semaphore(1); // Білий починає гру тому має 1
        Semaphore blackSemaphore = new Semaphore(0); // Чорний чекає другого ходу тому має 0

        ChessBoard board = new ChessBoard();

        List<String> whiteMoves = List.of("e2->e4", "g1->f3", "f1->c4", "d1->h5");
        List<String> blackMoves = List.of("e7->e5", "b8->c6", "g8->f6", "b7->b5");

        Player whitePlayer = new Player("Білий", whiteMoves, whiteSemaphore, blackSemaphore, board);
        Player blackPlayer = new Player("Чорний", blackMoves, blackSemaphore, whiteSemaphore, board);

        Thread whiteThread = new Thread(whitePlayer, "White-Thread");
        Thread blackThread = new Thread(blackPlayer, "Black-Thread");

        whiteThread.start();
        blackThread.start();

        // Демонстрація станів потоків у головному потоці поки гра триває.
        try {
            for (int i = 0; i < 10; i++) {
                System.out.printf("[СТАНИ %d] %s: %s, %s: %s\n", i + 1,
                        whiteThread.getName(), whiteThread.getState(),
                        blackThread.getName(), blackThread.getState()
                );
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            System.out.println("Головний потік було перервано під час перевірки станів.");
            Thread.currentThread().interrupt();
        }

        try {
            whiteThread.join();
            blackThread.join();
        } catch (InterruptedException e) {
            System.out.println("Головний потік перерваний під час очікування завершення гравців.");
            Thread.currentThread().interrupt();
        }

        System.out.println("\nГру завершено. Ось підсумок ходів:");
        board.printMoveHistory();
    }
}
