package pr3;

import java.util.Random;

public class MatrixUtil {

    public static int[][] generateMatrix(int rows, int cols, int min, int max) {
        Random random = new Random();
        int[][] matrix = new int[rows][cols];
        int range = max - min + 1;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(range) + min;
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.printf("%6d ", value);
            }
            System.out.println();
        }
    }
}
