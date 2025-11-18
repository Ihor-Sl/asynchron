package pr3;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Задача 2: Пошук файлів більших за певний розмір у директорії
 * Реалізація через Work Stealing (Fork/Join Framework)
 */
public class Task2FileFinder {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Задача 2: Пошук великих файлів (Work Stealing) ===\n");

            // Введення директорії
            System.out.print("Введіть шлях до директорії: ");
            String directoryPath = scanner.nextLine().trim();

            File directory = new File(directoryPath);
            if (!directory.exists()) {
                System.out.println("Помилка: директорія не існує");
                return;
            }
            if (!directory.isDirectory()) {
                System.out.println("Помилка: це не директорія");
                return;
            }

            // Введення мінімального розміру
            System.out.print("Введіть мінімальний розмір файлу (в байтах): ");
            long minSize = scanner.nextLong();
            if (minSize < 0) {
                System.out.println("Помилка: розмір має бути >= 0");
                return;
            }

            System.out.printf("\nПошук файлів більших за %d байт (%.2f MB)...%n",
                    minSize, minSize / (1024.0 * 1024.0));
            System.out.println("У директорії: " + directory.getAbsolutePath());
            System.out.println("\n--- Знайдені файли ---");

            // Виконання пошуку
            long startTime = System.nanoTime();

            AtomicInteger foundFiles = new AtomicInteger(0);
            ForkJoinPool pool = new ForkJoinPool();

            FileSearchTask task = new FileSearchTask(directory, minSize, foundFiles);
            int totalFound = pool.invoke(task);

            long endTime = System.nanoTime();
            double executionTime = (endTime - startTime) / 1_000_000.0;

            // Виведення результатів
            System.out.println("\n=== Результати ===");
            System.out.println("Кількість знайдених файлів: " + totalFound);
            System.out.printf("Час виконання: %.3f мс%n", executionTime);
            System.out.println("Використано процесорів: " +
                    Runtime.getRuntime().availableProcessors());

        } catch (Exception e) {
            System.out.println("Помилка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}