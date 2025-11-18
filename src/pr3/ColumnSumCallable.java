package pr3;

import java.util.concurrent.Callable;

// Callable для обчислення суми одного стовпця
public class ColumnSumCallable implements Callable<ColumnResult> {
    private final int[][] matrix;
    private final int column;
    private final int startRow;
    private final int endRow;

    public ColumnSumCallable(int[][] matrix, int column, int startRow, int endRow) {
        this.matrix = matrix;
        this.column = column;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public ColumnResult call() {
        long sum = 0;
        for (int i = startRow; i < endRow; i++) {
            sum += matrix[i][column];
        }
        return new ColumnResult(column, sum);
    }
}
