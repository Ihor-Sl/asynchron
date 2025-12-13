package pr5;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Task2 {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(6);

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("Оберіть задачу, для якої підбираємо ПЗ:");
            System.out.println("1) Відеомонтаж");
            System.out.println("2) Робота з документами / офіс");
            System.out.print("Ваш вибір (1/2): ");

            int choice = safeInt(scanner.nextLine(), 1);

            List<String> options = (choice == 2)
                    ? List.of("OfficePro", "DocsPlus", "PaperSuite")
                    : List.of("VideoCut", "EditMaster", "FilmForge");

            System.out.println("\nВаріанти для порівняння: " + options);

            // Для кожного варіанту ПЗ запускаємо паралельні запити: ціна, функціональність, підтримка
            List<CompletableFuture<Map.Entry<String, Double>>> evaluationFutures = options.stream()
                    .map(name -> evaluateSoftwareAsync(name, executor))
                    .toList();

            // anyOf(): швидка “перша відповідь” (хто першим порахувався)
            CompletableFuture<Object> any = CompletableFuture.anyOf(evaluationFutures.toArray(new CompletableFuture[0]));
            Map.Entry<String, Double> firstEvaluated = (Map.Entry<String, Double>) any.join();
            System.out.printf("%n[anyOf] Першим оцінили: %s (score=%.2f)%n",
                    firstEvaluated.getKey(), firstEvaluated.getValue());

            // allOf(): чекаємо завершення оцінки ВСІХ варіантів, щоб вибрати найкращий
            CompletableFuture<Void> all =
                    CompletableFuture.allOf(evaluationFutures.toArray(new CompletableFuture[0]));

            all.join();

            // Вибір найкращого
            List<Map.Entry<String, Double>> results = evaluationFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            Map.Entry<String, Double> best = results.stream()
                    .max(Map.Entry.comparingByValue())
                    .orElseThrow();

            System.out.println("\nРезультати порівняння:");
            results.forEach(e -> System.out.printf(" - %s: score=%.2f%n", e.getKey(), e.getValue()));

            System.out.printf("%nНайкращий варіант: %s (score=%.2f)%n",
                    best.getKey(), best.getValue());

        } finally {
            shutdown(executor);
        }
    }

    /**
     * Оцінка ПЗ:
     * - Паралельно отримуємо functionality і support
     * - Ціну отримуємо як залежний ланцюжок (thenCompose): basePrice -> discount -> finalPrice
     * - Результати об’єднуємо (thenCombine) у підсумковий score
     */
    private static CompletableFuture<Map.Entry<String, Double>> evaluateSoftwareAsync(String name, ExecutorService executor) {

        CompletableFuture<Integer> functionalityFuture = fetchFunctionality(name, executor);
        CompletableFuture<Integer> supportFuture = fetchSupport(name, executor);

        // thenCompose(): залежне отримання фінальної ціни через знижку
        CompletableFuture<Double> finalPriceFuture =
                fetchBasePrice(name, executor)
                        .thenCompose(base -> fetchDiscount(name, executor)
                                .thenApply(discount -> base * (1.0 - discount)));

        // thenCombine(): (functionality + support) -> проміжний бал
        CompletableFuture<Double> qualityScoreFuture =
                functionalityFuture.thenCombine(supportFuture, (func, support) ->
                        0.65 * func + 0.35 * support
                );

        // thenCombine(): додаємо фактор ціни до фінального score
        CompletableFuture<Double> totalScoreFuture =
                qualityScoreFuture.thenCombine(finalPriceFuture, (qualityScore, price) -> {
                    // нижча ціна = краще, тому віднімаємо невеликий штраф
                    double pricePenalty = price * 0.08; // умовний коефіцієнт
                    return qualityScore - pricePenalty;
                });

        return totalScoreFuture.thenApply(score -> Map.entry(name, score));
    }

    private static CompletableFuture<Double> fetchBasePrice(String name, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleepRandom(300, 900);
            double price = switch (name) {
                case "VideoCut" -> 25.0;
                case "EditMaster" -> 40.0;
                case "FilmForge" -> 30.0;
                case "OfficePro" -> 15.0;
                case "DocsPlus" -> 10.0;
                case "PaperSuite" -> 12.0;
                default -> 20.0;
            };
            System.out.println("Ціна (base) для " + name + ": $" + price);
            return price;
        }, executor);
    }

    private static CompletableFuture<Double> fetchDiscount(String name, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleepRandom(150, 600);
            double discount = switch (name) {
                case "DocsPlus" -> 0.10;
                case "VideoCut" -> 0.05;
                default -> 0.0;
            };
            System.out.println("Знижка для " + name + ": " + (int)(discount * 100) + "%");
            return discount;
        }, executor);
    }

    private static CompletableFuture<Integer> fetchFunctionality(String name, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleepRandom(250, 900);
            int functionality = switch (name) {
                case "VideoCut" -> 78;
                case "EditMaster" -> 92;
                case "FilmForge" -> 85;
                case "OfficePro" -> 80;
                case "DocsPlus" -> 70;
                case "PaperSuite" -> 75;
                default -> 60;
            };
            System.out.println("Функціональність для " + name + ": " + functionality + "/100");
            return functionality;
        }, executor);
    }

    private static CompletableFuture<Integer> fetchSupport(String name, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleepRandom(200, 850);
            int support = switch (name) {
                case "VideoCut" -> 70;
                case "EditMaster" -> 88;
                case "FilmForge" -> 75;
                case "OfficePro" -> 85;
                case "DocsPlus" -> 65;
                case "PaperSuite" -> 72;
                default -> 60;
            };
            System.out.println("Підтримка для " + name + ": " + support + "/100");
            return support;
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

    private static int safeInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
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

