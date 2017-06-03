package mpj.lab3;

import mpi.Datatype;
import mpi.MPI;

import java.util.Arrays;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {

        int P = Integer.parseInt(args[1]);
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int N = 5;
        int H = N/P;

        double[][] A = new double[N][];
        double[] b = new double[N];

        double[][] L_resv = new double[N][N];



        if (me == 0){
            A = new double[][]{
                    {10, 1, 1, 1, 1},
                    {1, 10, 1, 1, 1},
                    {1, 1, 10, 1, 1},
                    {1, 1, 1, 10, 1},
                    {1, 1, 1, 1, 10}};
            b = new double[]{1, 1, 1, 1, 1};

        }
        
        MPI.COMM_WORLD.Bcast(A, 0, N, MPI.OBJECT, 0);
        MPI.COMM_WORLD.Bcast(b, 0, N, MPI.DOUBLE, 0);

        double[][] L = new double[N][N];



        double[] y = new double[N];
        double[] x = new double[N];


        for (int k = 0; k <= N-1; k++) {
            for (int i = k + 1; i < N; i++) {
                L[i][k] = A[i][k] / A[k][k];
            }
            for (int j = k + 1; j < N; j++) {
                for (int i = k + 1; i < N; i++){
                    A[i][j] = A[i][j] - L[i][k] * A[k][j];
                }
            }
        }

        for (int i = 0; i < N; i++) {
            y[i] = b[i];
            for (int s = 0; s <= i - 1; s++) {
                y[i] = y[i] - y[s] * L[i][s];
            }
        }

        for (int j = N - 1; j >= 0; j--) {
            x[j] = y[j] / A[j][j];
            for (int i = 0; i <= j - 1; i++) {
                y[i] = y[i] - x[j] * A[i][j];
            }
        }

        System.out.println(Arrays.toString(x));

        System.out.println("Hi from <"+me+">");
        MPI.Finalize();
    }
}
