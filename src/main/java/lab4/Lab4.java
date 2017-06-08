package lab4;

import mpi.MPI;
import mpj.lab3.Lab3;

import java.util.Arrays;


public class Lab4 {

    public static void main(String[] args) {

        int P = Integer.parseInt(args[1]);
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int N = 12;
        double[][] MA = new double[N][];

        double[][] L;
        if (rank == 0) {
            MA = new double[][]{
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
            System.out.println("data inputted");
        }


        int h = N / P;

        double[][] MAh = new double[h][N];
        double[][] Lh = new double[h][N];
        double[] L_input = new double[N];


        for (double[] item : MAh) {

        }

        MPI.COMM_WORLD.Scatter(MA, 0, h, MPI.OBJECT, MAh, 0, h, MPI.OBJECT, 0);

        for (int k = 0; k < N - 1; k++) {
            int beg_index = rank * h - h;
            int end_index = beg_index + h - 1;
            int local_k = 0;
            if (k >= beg_index && k <= end_index) {
                local_k = k - beg_index;
                for (int i = k + 1; i < MAh.length; i++) {
                    Lh[local_k][i] = MAh[local_k][i] / MAh[local_k][k];
                    L_input[i] = Lh[local_k][i];

                }
            }

            int sender_Task = k / h + 1;

            if (sender_Task< P){
                MPI.COMM_WORLD.Bcast(L_input, 0, N, MPI.DOUBLE, sender_Task);
            }






        }


        MPI.Finalize();
    }
}
