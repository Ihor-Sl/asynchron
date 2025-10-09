package pr1;

import java.util.List;
import java.util.concurrent.Semaphore;

public class Player implements Runnable {

    private final String name;
    private final List<String> moves; // наперед задані ходи для симуляції
    private final Semaphore mySemaphore; // семафор цього гравця
    private final Semaphore opponentSemaphore; // семафор опонента
    private final ChessBoard board;

    public Player(String name, List<String> moves, Semaphore mySemaphore, Semaphore opponentSemaphore,
                  ChessBoard board) {
        this.name = name;
        this.moves = moves;
        this.mySemaphore = mySemaphore;
        this.opponentSemaphore = opponentSemaphore;
        this.board = board;
    }

    @Override
    public void run() {
        try {
            for (String move : moves) {
                mySemaphore.acquire(); // Очікуємо дозволу на хід
                System.out.printf("%s: починає хід.\n", name);

                Thread.sleep(200 + (int) (Math.random() * 400)); // Імітація часу роздуму 200-600 ms думання

                board.applyMove(name, move);

                opponentSemaphore.release(); // Після виконання ходу передаємо хід опоненту
            }
            System.out.printf("%s: завершив свої ходи.\n", name);
            opponentSemaphore.release(); // Даємо одне звільнення опоненту після всіх ходів, щоб він не залишився назавжди заблокованим.

        } catch (InterruptedException e) {
            System.out.printf("%s: отримано переривання під час очікування.\n", name);
            Thread.currentThread().interrupt();
        }
    }
}
