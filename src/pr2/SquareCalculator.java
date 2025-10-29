package pr2;

import java.util.concurrent.Callable;

public class SquareCalculator implements Callable<double[]> {

    private final double[] nums;

    public SquareCalculator(double[] nums) {
        this.nums = nums;
    }

    @Override
    public double[] call() {

        double[] result = new double[nums.length];

        for (int i = 0; i < nums.length; i++) {
            result[i] = Math.pow(nums[i], 2);
        }

        return result;
    }
}
