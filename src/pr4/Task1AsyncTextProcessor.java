package pr4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Task1AsyncTextProcessor {

    public static void main(String[] args) {

        List<Path> files = List.of(
                Path.of("src/pr4/files/file1.txt"),
                Path.of("src/pr4/files/file2.txt"),
                Path.of("src/pr4/files/file3.txt")
        );

        long globalStart = System.nanoTime();

        // 1) Асинхронно читаємо текст з файлів
        List<CompletableFuture<String>> readFilesFutures = files.stream()
                .map(path -> CompletableFuture.supplyAsync(() -> {
                    try {
                        long start = System.nanoTime();
                        String content = Files.readString(path);
                        System.out.println("Читання файлу " + path.getFileName() + " — час виконання: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
                        return content;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .toList();

        CompletableFuture<List<String>> readAllFilesFuture = allAsList(readFilesFutures);

        // 2) Асинхронно видаляємо всі літери
        CompletableFuture<char[]> processedFuture = readAllFilesFuture.thenApplyAsync(sentences -> {
            long start = System.nanoTime();

            StringBuilder sb = new StringBuilder();
            for (String s : sentences) {
                for (char c : s.toCharArray()) {
                    if (!Character.isLetter(c)) {
                        sb.append(c);
                    }
                }
            }

            System.out.println("Обробка тексту (видалення літер) — час виконання: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
            return sb.toString().toCharArray();
        });

        // 3) Асинхронно вивести початкові речення
        CompletableFuture<Void> printInputFuture = readAllFilesFuture.thenAcceptAsync(list -> {
            long start = System.nanoTime();
            System.out.println("Початкові речення:");
            list.forEach(System.out::println);
            System.out.println("Вивід початкових речень — час виконання: " +
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
        });

        // 4) Асинхронно вивести масив результату
        CompletableFuture<Void> printResultFuture = processedFuture.thenAcceptAsync(arr -> {
            long start = System.nanoTime();
            System.out.println("Результуючий масив символів:");
            System.out.println(new String(arr));
            System.out.println("Вивід результуючого масиву — час виконання: " +
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
        });

        // 5) Фінальний thenRunAsync()
        CompletableFuture.allOf(printInputFuture, printResultFuture).thenRunAsync(() -> {
            long total = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - globalStart);
            System.out.println("Усі асинхронні операції Task1 завершені. Загальний час: " + total + " ms");
        }).join();
    }

    public static <T> CompletableFuture<List<T>> allAsList(final List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }
}
