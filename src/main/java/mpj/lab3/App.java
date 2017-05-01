package mpj.lab3;

import mpi.MPI;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hello world from <"+me+"> from <"+size);
        MPI.Finalize();
    }
}
