package lab4;

import mpi.MPI;


public class Lab4 {

    public static void main(String[] args) {

        int P = Integer.parseInt(args[1]);
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int N = 12;
        double[][] MA = new double[N][];

        double[][] L;
        if (MPI.COMM_WORLD.Rank() == 0) {
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
            if(k>=beg_index && k<=end_index){
                local_k = k - beg_index;
                for(int i = k+1; i < MAh.length; i++){
                    Lh[local_k ][ i] = MAh[local_k] [ i] / MAh[local_k ][ k];
                    L_input[i] = Lh[local_k ][ i];

                }
            }else if (k > end_index) continue;

        }


        MPI.Finalize();
    }
}
