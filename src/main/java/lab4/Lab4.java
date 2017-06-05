package lab4;

import mpi.MPI;


public class Lab4 {

    public static void main(String[] args) {

        int P = Integer.parseInt(args[1]);
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int N = 12;

        MPI.Finalize();
    }
}
