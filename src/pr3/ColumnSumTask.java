package pr3;

import java.util.concurrent.RecursiveTask;

// RecursiveTask для обчислення суми одного стовпця
class ColumnSumTask extends RecursiveTask<Long> {
    private final int[][] matrix;
    private final int column;
    private final int startRow;
    private final int endRow;
    private static final int THRESHOLD = 1000; // Поріг для поділу задачі

    public ColumnSumTask(int[][] matrix, int column, int startRow, int endRow) {
        this.matrix = matrix;
        this.column = column;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    protected Long compute() {
        int rows = endRow - startRow;

        // Якщо задача мала - виконуємо послідовно
        if (rows <= THRESHOLD) {
            long sum = 0;
            for (int i = startRow; i < endRow; i++) {
                sum += matrix[i][column];
            }
            return sum;
        }

        // Інакше - ділимо на підзадачі (Work Stealing)
        int mid = startRow + rows / 2;
        ColumnSumTask leftTask = new ColumnSumTask(matrix, column, startRow, mid);
        ColumnSumTask rightTask = new ColumnSumTask(matrix, column, mid, endRow);

        // fork() - додає задачу в чергу для виконання іншими потоками
        leftTask.fork();

        // Поточний потік виконує правий task
        long rightResult = rightTask.compute();

        // join() - чекає завершення і отримує результат
        long leftResult = leftTask.join();

        return leftResult + rightResult;
    }
}
