package pr2;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class AsyncArrayProcessor {

    private static final int MIN_ARRAY_SIZE = 40;
    private static final int MAX_ARRAY_SIZE = 60;
    private static final int CHUNK_SIZE = 10; // Розмір частини масиву
    private static final int N_THREADS = 6;

    public static void main(String[] args) {
        // Ввід даних
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть нижню межу діапазону: ");
        double min = scanner.nextDouble();

        System.out.print("Введіть верхню межу діапазону: ");
        double max = scanner.nextDouble();

        scanner.close();

        long startTime = System.currentTimeMillis();

        // Генерація масиву
        double[] numbers = generateRandomArray(min, max);
        System.out.println("\nЗгенерований масив (" + numbers.length + " елементів):");
        String formattedNumbers = Arrays.stream(numbers)
                .mapToObj(v -> String.format(Locale.US, "%.2f", v))
                .collect(Collectors.joining(", "));
        System.out.println("[" + formattedNumbers + "]");

        // Розбиття на частини
        List<double[]> chunks = splitIntoChunks(numbers, CHUNK_SIZE);
        System.out.println("\nРозбито на " + chunks.size() + " частин(и)");

        ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);

        // Відправлення завдань у циклі
        List<Future<double[]>> futures = new ArrayList<>();
        for (double[] chunk : chunks) {
            Future<double[]> future = executor.submit(new SquareCalculator(chunk));
            futures.add(future);
        }

        // Очікування виконання
        CopyOnWriteArraySet<Double> results = waitForCompletion(futures);

        executor.shutdown();

        // Вивід результату і часу
        System.out.println("\nРезультати (квадрати чисел, " + results.size() + " унікальних значень):");
        String formattedResults = results.stream()
                .map(v -> String.format(Locale.US, "%.2f", v))
                .collect(Collectors.joining(", "));
        System.out.println("[" + formattedResults + "]");

        long endTime = System.currentTimeMillis();
        System.out.println("\n\nЧас виконання програми: " + (endTime - startTime) + " мс");
    }

    private static double[] generateRandomArray(double min, double max) {
        Random rand = new Random();
        int size = MIN_ARRAY_SIZE + rand.nextInt(MAX_ARRAY_SIZE - MIN_ARRAY_SIZE + 1);

        return DoubleStream
                .generate(() -> min + (max - min) * rand.nextDouble())
                .limit(size)
                .toArray();
    }

    private static List<double[]> splitIntoChunks(double[] array, int chunkSize) {
        List<double[]> chunks = new ArrayList<>();

        for (int i = 0; i < array.length; i += chunkSize) {
            int end = Math.min(array.length, i + chunkSize);
            double[] chunk = Arrays.copyOfRange(array, i, end);
            chunks.add(chunk);
        }

        return chunks;
    }

    private static CopyOnWriteArraySet<Double> waitForCompletion(List<Future<double[]>> futures) {

        CopyOnWriteArraySet<Double> results = new CopyOnWriteArraySet<>();
        boolean allTasksCompleted = false;

        // Перевірка стану всіх завдань
        while (!allTasksCompleted) {
            allTasksCompleted = true;

            for (Future<double[]> future : futures) {
                if (!future.isDone()) {
                    allTasksCompleted = false;
                    System.out.println("Завдання ще виконується...");
                    break;
                }

                // Перевірка чи завдання було скасоване
                if (future.isCancelled()) {
                    System.out.println("Увага: одне з завдань було скасоване!");
                }
            }
        }

        // Отримання результатів тільки після завершення всіх завдань
        System.out.println("Всі завдання завершені. Отримую результати...");

        for (Future<double[]> future : futures) {
            if (!future.isCancelled()) {
                try {
                    double[] chunkResults = future.get();
                    for (double value : chunkResults) {
                        results.add(value);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Помилка при отриманні результату: " + e.getMessage());
                }
            } else {
                System.out.println("Пропускаємо скасоване завдання");
            }
        }

        return results;
    }
}