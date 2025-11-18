package pr3;

import java.util.Scanner;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Задача 1: Знаходження суми елементів кожного стовпця матриці
 * Реалізація через Work Dealing (ExecutorService з Thread Pool)
 */
public class Task1WorkDealing {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Задача 1: Сума стовпців матриці (Work Dealing) ===\n");

            // Введення параметрів
            System.out.print("Введіть кількість рядків матриці: ");
            int rows = scanner.nextInt();
            if (rows <= 0) {
                System.out.println("Помилка: кількість рядків має бути > 0");
                return;
            }

            System.out.print("Введіть кількість стовпців матриці: ");
            int cols = scanner.nextInt();
            if (cols <= 0) {
                System.out.println("Помилка: кількість стовпців має бути > 0");
                return;
            }

            System.out.print("Введіть мінімальне значення елемента: ");
            int minValue = scanner.nextInt();

            System.out.print("Введіть максимальне значення елемента: ");
            int maxValue = scanner.nextInt();
            if (maxValue < minValue) {
                System.out.println("Помилка: максимум має бути >= мінімуму");
                return;
            }

            // Генерація матриці
            int[][] matrix = MatrixUtil.generateMatrix(rows, cols, minValue, maxValue);

            // Виведення матриці (якщо невелика)
            if (rows <= 20 && cols <= 20) {
                System.out.println("\nЗгенерована матриця:");
                MatrixUtil.printMatrix(matrix);
            } else {
                System.out.println("\nМатриця згенерована (занадто велика для виведення)");
            }

            // Обчислення з використанням ExecutorService (Work Dealing)
            long startTime = System.nanoTime();

            int processors = Runtime.getRuntime().availableProcessors();
            long[] columnSums = calculateMatrixColumnSums(matrix, processors);

            long endTime = System.nanoTime();
            double executionTime = (endTime - startTime) / 1_000_000.0;

            // Виведення результатів
            System.out.println("\n=== Результати ===");
            for (int col = 0; col < cols; col++) {
                System.out.printf("Сума стовпця %d: %d%n", col + 1, columnSums[col]);
            }

            System.out.printf("\nЧас виконання (Work Dealing): %.3f мс%n", executionTime);
            System.out.println("Розмір Thread Pool: " + processors);

        } catch (Exception e) {
            System.out.println("Помилка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    public static long[] calculateMatrixColumnSums(int[][] matrix, int processors) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(processors);

        int rows = matrix.length;
        int cols = matrix[0].length;

        // Розподіляємо роботу заздалегідь (Work Dealing)
        List<Future<ColumnResult>> futures = new ArrayList<>();
        long[] columnSums = new long[cols];

        // Створюємо задачі для кожного стовпця
        // При великій кількості рядків ділимо на частини
        int rowsPerTask = Math.max(1, rows / (processors * 2));

        for (int col = 0; col < cols; col++) {
            for (int startRow = 0; startRow < rows; startRow += rowsPerTask) {
                int endRow = Math.min(startRow + rowsPerTask, rows);
                futures.add(executor.submit(
                        new ColumnSumCallable(matrix, col, startRow, endRow)
                ));
            }
        }

        // Збираємо результати
        for (Future<ColumnResult> future : futures) {
            ColumnResult result = future.get();
            columnSums[result.column] += result.sum;
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        return columnSums;
    }
}