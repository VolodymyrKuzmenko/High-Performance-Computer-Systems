package mpj.lab5;

import mpi.MPI;

public class Lab5 {

    public static void main(String[] args) {

        int P = Integer.parseInt(args[1]);
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();

        MPI.Finalize();
    }
}
