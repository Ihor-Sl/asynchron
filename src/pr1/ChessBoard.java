package pr1;

public class ChessBoard {

    private final StringBuilder moveLogs = new StringBuilder();

    public synchronized void applyMove(String playerName, String move) {
        String log = String.format("%s зробив хід: %s", playerName, move);
        moveLogs.append(log).append("\n");
        System.out.println(log);
    }

    public synchronized void printMoveHistory() {
        System.out.println("--- Журнал ходів ---");
        System.out.print(moveLogs);
        System.out.println("--------------------");
    }
}
