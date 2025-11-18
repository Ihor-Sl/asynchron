package pr3;

/**
 * Програма для порівняння ефективності Work Stealing та Work Dealing
 */
public class ComparisonTest {

    private static final int MATRIX_ROWS = 5000;
    private static final int MATRIX_COLS = 1000;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 100;
    private static final int TEST_RUNS = 5;
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        System.out.println("=== Порівняння Work Stealing vs Work Dealing ===\n");
        System.out.printf("Параметри тесту:%n");
        System.out.printf("- Розмір матриці: %d x %d%n", MATRIX_ROWS, MATRIX_COLS);
        System.out.printf("- Кількість запусків: %d%n", TEST_RUNS);
        System.out.printf("- Доступних процесорів: %d%n%n", PROCESSORS);

        // Генерація матриці
        System.out.println("Генерація матриці...");
        int[][] matrix = MatrixUtil.generateMatrix(MATRIX_ROWS, MATRIX_COLS, MIN_VALUE, MAX_VALUE);
        System.out.println("Готово!\n");

        // Тестування Work Stealing
        System.out.println("=== Тестування Work Stealing (Fork/Join) ===");
        double[] stealingTimes = new double[TEST_RUNS];
        for (int i = 0; i < TEST_RUNS; i++) {
            stealingTimes[i] = testWorkStealing(matrix);
            System.out.printf("Запуск %d: %.3f мс%n", i + 1, stealingTimes[i]);
        }
        double avgStealing = average(stealingTimes);
        System.out.printf("Середній час: %.3f мс%n%n", avgStealing);

        // Тестування Work Dealing
        System.out.println("=== Тестування Work Dealing (ExecutorService) ===");
        double[] dealingTimes = new double[TEST_RUNS];
        for (int i = 0; i < TEST_RUNS; i++) {
            dealingTimes[i] = testWorkDealing(matrix);
            System.out.printf("Запуск %d: %.3f мс%n", i + 1, dealingTimes[i]);
        }
        double avgDealing = average(dealingTimes);
        System.out.printf("Середній час: %.3f мс%n%n", avgDealing);

        // Порівняння результатів
        System.out.println("=== Підсумок ===");
        System.out.printf("Work Stealing середній час: %.3f мс%n", avgStealing);
        System.out.printf("Work Dealing середній час: %.3f мс%n", avgDealing);

        if (avgStealing < avgDealing) {
            double improvement = ((avgDealing - avgStealing) / avgDealing) * 100;
            System.out.printf("%nWork Stealing швидший на %.2f%%%n", improvement);
        } else {
            double improvement = ((avgStealing - avgDealing) / avgStealing) * 100;
            System.out.printf("%nWork Dealing швидший на %.2f%%%n", improvement);
        }
    }

    private static double testWorkStealing(int[][] matrix) {
        long startTime = System.nanoTime();

        Task1WorkStealing.calculateMatrixColumnSums(matrix, PROCESSORS);

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000.0;
    }

    private static double testWorkDealing(int[][] matrix) {
        long startTime = System.nanoTime();

        try {
            Task1WorkDealing.calculateMatrixColumnSums(matrix, PROCESSORS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000.0;
    }

    private static double average(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }
}