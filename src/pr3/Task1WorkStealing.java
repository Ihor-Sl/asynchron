package pr3;

import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

/**
 * Задача 1: Знаходження суми елементів кожного стовпця матриці
 * Реалізація через Work Stealing (Fork/Join Framework)
 */
public class Task1WorkStealing {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Задача 1: Сума стовпців матриці (Work Stealing) ===\n");

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

            // Обчислення з використанням Fork/Join
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

            System.out.printf("\nЧас виконання (Work Stealing): %.3f мс%n", executionTime);
            System.out.println("Кількість процесорів: " + processors);

        } catch (Exception e) {
            System.out.println("Помилка: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    public static long[] calculateMatrixColumnSums(int[][] matrix, int processors) {
        ForkJoinPool pool = new ForkJoinPool(processors);

        int rows = matrix.length;
        int cols = matrix[0].length;

        long[] columnSums = new long[cols];

        for (int col = 0; col < cols; col++) {
            ColumnSumTask task = new ColumnSumTask(matrix, col, 0, rows);
            columnSums[col] = pool.invoke(task);
        }
        return columnSums;
    }
}