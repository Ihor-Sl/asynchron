package pr4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Task2AsyncSequenceCalculator {

    public static void main(String[] args) {

        long global = System.nanoTime();

        // 1) supplyAsync — генеруємо послідовність
        CompletableFuture<List<Double>> generateFuture = CompletableFuture.supplyAsync(() -> {
            long t = System.nanoTime();
            Random rnd = new Random();
            List<Double> list = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                list.add(rnd.nextDouble(100));
            }
            System.out.println("Генерація послідовності — " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t) + " ms");
            return list;
        });

        // 2) thenApplyAsync — обчислення формули
        CompletableFuture<Double> calcFuture = generateFuture.thenApplyAsync(list -> {
            long t = System.nanoTime();
            double sum = 0;
            for (int i = 0; i < list.size() - 1; i++) {
                sum += list.get(i) * list.get(i + 1);
            }
            System.out.println("Обчислення суми a1 * a2 + ... — " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t) + " ms");
            return sum;
        });

        // 3) thenAcceptAsync — вивести початкову послідовність
        CompletableFuture<Void> printInputFuture = generateFuture.thenAcceptAsync(list -> {
            long t = System.nanoTime();
            StringBuilder sb = new StringBuilder("Початкова послідовність:\n");
            for (double x : list) {
                sb.append(String.format("%.3f ", x));
            }
            System.out.println(sb);
            System.out.println("Вивід початкової послідовності — " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t) + " ms");
        });

        // 4) thenAcceptAsync — вивести результат
        CompletableFuture<Void> printResultFuture = calcFuture.thenAcceptAsync(result -> {
            long t = System.nanoTime();
            System.out.println("Результат обчислення: " + result);
            System.out.println("Вивід результату — " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t) + " ms");
        });

        // 5) thenRunAsync — фінальне повідомлення
        CompletableFuture.allOf(printInputFuture, printResultFuture).thenRunAsync(() -> {
            long total = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - global);
            System.out.println("Усі асинхронні операції Task2 завершені. Час: " + total + " ms");
        }).join();
    }
}
