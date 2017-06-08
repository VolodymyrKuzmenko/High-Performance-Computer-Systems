package mpj.lab4;

import mpi.MPI;

public class Lab4 {

    public static void main(String[] args) {

        int P = Integer.parseInt(args[1]);
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int  N = 8;
        double[][] MA = new double[N][];

        double[][] L = new double[N][N];
        if (rank == 0) {
            MA = new double[][]{
                    {10, 1, 1, 1, 1, 1, 1, 1},
                    {1, 10, 1, 1, 1, 1, 1, 1},
                    {1, 1, 10, 1, 1, 1, 1, 1},
                    {1, 1, 1, 10, 1, 1, 1, 1},
                    {1, 1, 1, 1, 10, 1, 1, 1},
                    {1, 1, 1, 1, 1, 10, 1, 1},
                    {1, 1, 1, 1, 1, 1, 10, 1},
                    {1, 1, 1, 1, 1, 1, 1, 10}};

       }

        int h = N / P;

        double[][] MAh = new double[h][N];
        double[][] Lh = new double[h][N];
        double[] L_input = new double[N];

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

            if (sender_Task < P) {
                MPI.COMM_WORLD.Bcast(L_input, 0, N, MPI.DOUBLE, sender_Task);
            }

            for (int j = local_k; j < h; j++) {
                for (int i = k + 1; i < MAh[j].length; i++) {
                    MAh[j][i] = L_input[i] * MAh[j][k];
                }
            }
        }

        MPI.COMM_WORLD.Gather(MAh, 0, h, MPI.OBJECT, MA, 0, h, MPI.OBJECT, 0);
        MPI.COMM_WORLD.Gather(Lh, 0, h, MPI.OBJECT, L, 0, h, MPI.OBJECT, 0);

        if (rank == 0) {
            double det = 1;
            for (int i = 0; i < N; i++) {
                det = det * MA[i][i] * L[i][i];
            }
            System.out.println(det);
        }
        MPI.Finalize();
    }
}
