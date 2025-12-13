package pr5;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Task1 {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            // Базова асинхронна операція (наприклад, пошук userId за логіном)
            CompletableFuture<Long> userIdFuture =
                    CompletableFuture.supplyAsync(() -> findUserIdByLogin("ihor"), executor);

            // thenCompose(): залежне асинхронне продовження (спочатку userId, потім профіль)
            CompletableFuture<String> profileFuture =
                    userIdFuture.thenCompose(userId -> fetchUserProfile(userId, executor));

            // Ще одне асинхронне завдання, яке в результаті теж залежить від userId,
            // але після отримання userId виконується ПАРАЛЕЛЬНО з profileFuture
            CompletableFuture<Integer> ordersCountFuture =
                    userIdFuture.thenCompose(userId -> fetchOrdersCount(userId, executor));

            // anyOf(): показуємо, що завершилось першим (профіль чи кількість замовлень)
            Object firstReady = CompletableFuture.anyOf(profileFuture, ordersCountFuture).join();
            System.out.println("[anyOf] Перше готове значення: " + firstReady);

            // thenCombine(): об’єднуємо результати двох паралельних задач
            CompletableFuture<String> combinedResult =
                    profileFuture.thenCombine(ordersCountFuture, (profile, ordersCount) ->
                            "Об'єднаний результат -> " + profile + ", orders=" + ordersCount
                    );

            // allOf(): чекаємо завершення обох задач (можна ще додати інші, якщо треба)
            CompletableFuture<Void> allDone =
                    CompletableFuture.allOf(profileFuture, ordersCountFuture);

            allDone.join(); // гарантовано дочекаємось

            System.out.println("[thenCombine] " + combinedResult.join());
            System.out.println("[allOf] Обидві задачі завершені.");

        } finally {
            shutdown(executor);
        }
    }

    private static long findUserIdByLogin(String login) {
        sleepRandom(300, 800);
        long id = ThreadLocalRandom.current().nextLong(1000, 9999);
        System.out.println("Знайдено userId для '" + login + "': " + id);
        return id;
    }

    private static CompletableFuture<String> fetchUserProfile(long userId, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleepRandom(600, 1200);
            return "Profile{userId=" + userId + ", name='Ihor'}";
        }, executor);
    }

    private static CompletableFuture<Integer> fetchOrdersCount(long userId, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleepRandom(400, 1100);
            int count = ThreadLocalRandom.current().nextInt(0, 20);
            System.out.println("Пораховано замовлення для userId=" + userId + ": " + count);
            return count;
        }, executor);
    }

    private static void sleepRandom(int fromMs, int toMs) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(fromMs, toMs + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        }
    }

    private static void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

