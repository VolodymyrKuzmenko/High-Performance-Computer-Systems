package mpj.lab3;

import java.util.Arrays;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        int n = 5;
        double[][] A = new double[][]{
                {10, 1, 1, 1, 1},
                {1, 10, 1, 1, 1},
                {1, 1, 10, 1, 1},
                {1, 1, 1, 10, 1},
                {1, 1, 1, 1, 10}};

        double[][] L = new double[n][n];

        double[] b = new double[]{1, 1, 1, 1, 1};
        double[] y = new double[n];
        double[] x = new double[n];


        for (int k = 0; k <= n-1; k++) {
            for (int i = k + 1; i < n; i++) {
                L[i][k] = A[i][k] / A[k][k];
            }
            for (int j = k + 1; j < n; j++) {
                for (int i = k + 1; i < n; i++){
                    A[i][j] = A[i][j] - L[i][k] * A[k][j];
                }
            }
        }

        for (int i = 0; i < n; i++) {
            y[i] = b[i];
            for (int s = 0; s <= i - 1; s++) {
                y[i] = y[i] - y[s] * L[i][s];
            }
        }

        for (int j = n - 1; j >= 0; j--) {
            x[j] = y[j] / A[j][j];
            for (int i = 0; i <= j - 1; i++) {
                y[i] = y[i] - x[j] * A[i][j];
            }
        }

        System.out.println(Arrays.toString(x));
    }
}
