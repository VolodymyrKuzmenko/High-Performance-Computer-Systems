package mpj.lab3;

import com.google.common.primitives.Doubles;
import mpi.MPI;

import java.util.Arrays;

public class App {

    public static final int MESSAGE_1 = 1;
    public static final int MESSAGE_2 = 2;
    public static double[][] EMPTY;

    public static void main(String[] args) {

        int P = Integer.parseInt(args[1]);
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int N = 12;
        EMPTY = new double[N][0];
        double[][] A_resv = new double[N][N];
        double[] b = new double[N];

        if (me == 0) {
            //Data input into master thread
            A_resv = new double[][]{
                    {10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 10, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 10, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 10, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 10, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 10, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 10, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10}};
            b = new double[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

        }

        //Broadcasting input data to slave threads
        MPI.COMM_WORLD.Bcast(A_resv, 0, N, MPI.OBJECT, 0);
        MPI.COMM_WORLD.Bcast(b, 0, N, MPI.DOUBLE, 0);

        double[][] L = new double[N][N];
        double[] y = new double[N];
        double[] x = new double[N];


        for (int k = 0; k <= N - 1; k++) {
            for (int i = k + 1; i < N; i++) {
                L[i][k] = A_resv[i][k] / A_resv[k][k];
            }

            int iterationSize = (N - (k + 1));
            int h;
            if (iterationSize % P != 0) {
                h = iterationSize / P + 1;
            } else {
                h = iterationSize / P;
            }

            int[] start_buf = new int[1];
            int start;
            int finish;

            if (me == 0) {
                start = k + 1;
                finish = start_buf[0] = start + h;

                MPI.COMM_WORLD.Send(start_buf, 0, 1, MPI.INT, me + 1, MESSAGE_1);
            } else {

                MPI.COMM_WORLD.Recv(start_buf, 0, 1, MPI.INT, me - 1, MESSAGE_1);
                start = start_buf[0];
                finish = start_buf[0] + h;
                if (me != MPI.COMM_WORLD.Size() - 1) {
                    start_buf[0] = finish;
                    MPI.COMM_WORLD.Send(start_buf, 0, 1, MPI.INT, me + 1, MESSAGE_1);
                }
                if (finish > N) {
                    finish = N;
                }

            }
            MPI.COMM_WORLD.Barrier();
            MPI.COMM_WORLD.Barrier();
            double[][] A_send = new double[N][Math.abs(finish - start)];

            MPI.COMM_WORLD.Barrier();
            boolean calculation = false;
            int count = start;
            if (count > N) {
                count = N;
            }
            for (int m = 0; m < count; m++) {
                for (int t = 0; t < A_send[0].length; t++) {
                    A_send[m][t] = A_resv[m][t];
                }
            }
            MPI.COMM_WORLD.Barrier();

            for (int j = start, u = 0; j < finish; j++, u++) {
                calculation = true;
                for (int i = k + 1; i < N; i++) {
                   A_send[i][u] = A_resv[i][j] - L[i][k] * A_resv[k][j];

                }
            }
            MPI.COMM_WORLD.Barrier();
            MPI.COMM_WORLD.Barrier();
            if (me == MPI.COMM_WORLD.Size() - 1) {
                if (calculation) {
                    MPI.COMM_WORLD.Send(A_send, 0, N, MPI.OBJECT, me - 1, MESSAGE_2);
                } else {
                    MPI.COMM_WORLD.Send(EMPTY, 0, N, MPI.OBJECT, me - 1, MESSAGE_2);
                }

            } else {
                double[][] A_buf = new double[N][];
                if (me != 0) {

                    MPI.COMM_WORLD.Recv(A_buf, 0, N, MPI.OBJECT, me + 1, MESSAGE_2);
                    if (calculation) {
                        for (int m = 0; m < N; m++) {
                                if (A_buf[m] != null) {
                                A_send[m] = Doubles.concat(A_send[m], A_buf[m]);
                            }
                        }
                        MPI.COMM_WORLD.Send(A_send, 0, N, MPI.OBJECT, me - 1, MESSAGE_2);
                    } else {
                        MPI.COMM_WORLD.Send(EMPTY, 0, N, MPI.OBJECT, me - 1, MESSAGE_2);
                    }
                } else {

                    MPI.COMM_WORLD.Recv(A_buf, 0, N, MPI.OBJECT, me + 1, MESSAGE_2);
                  for (int m = 0; m < N; m++) {
                        A_send[m] = Doubles.concat(Arrays.copyOf(A_resv[m], start), A_send[m], A_buf[m]);
                    }
                    A_resv = A_send;
                }
            }
            MPI.COMM_WORLD.Bcast(A_resv, 0, N, MPI.OBJECT, 0);
        }

        for (int i = 0; i < N; i++) {
            y[i] = b[i];
            for (int s = 0; s <= i - 1; s++) {
                y[i] = y[i] - y[s] * L[i][s];
            }
        }

        for (int j = N - 1; j >= 0; j--) {
            x[j] = y[j] / A_resv[j][j];
            for (int i = 0; i <= j - 1; i++) {
                y[i] = y[i] - x[j] * A_resv[i][j];
            }
        }

        if (me == 0) {
            System.out.println(Arrays.toString(x));
        }
        MPI.Finalize();
    }
}
