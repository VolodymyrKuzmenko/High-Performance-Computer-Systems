package mpj.lab3;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import mpi.Datatype;
import mpi.MPI;

import java.util.Arrays;

/**
 * Hello world!
 */
public class App {

    public static final int MESSAGE_1 = 1;
    public static final int MESSAGE_2 = 2;
    public static double [][] EMPTY;

    public static void main(String[] args) {

        int P = Integer.parseInt(args[1]);
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int N = 12;
        EMPTY = new double[N][0];



        double[][] A_resv = new double[N][N];

        double[] b = new double[N];

        if (me == 0){
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
        
        MPI.COMM_WORLD.Bcast(A_resv, 0, N, MPI.OBJECT, 0);
        MPI.COMM_WORLD.Bcast(b, 0, N, MPI.DOUBLE, 0);

        double[][] L = new double[N][N];
        double[] y = new double[N];
        double[] x = new double[N];


        for (int k = 0; k <= N-1; k++) {
            for (int i = k + 1; i < N; i++) {
                L[i][k] = A_resv[i][k] / A_resv[k][k];
            }

            int iterationSize = (N - (k + 1));
            int h;
            if (iterationSize % P != 0){
                h = iterationSize/P + 1;
            }else {
                h = iterationSize/P;
            }

            int [] start_buf = new int [1];
            int  start;
            int finish;


            if (me == 0){
                start = k + 1;
                finish = start_buf[0] = start + h;

                MPI.COMM_WORLD.Send(start_buf,0,1, MPI.INT, me+1, MESSAGE_1);
            }else {

                MPI.COMM_WORLD.Recv(start_buf,0,1, MPI.INT, me-1, MESSAGE_1);
                start = start_buf[0];
                finish = start_buf[0]+h;
                if (me != MPI.COMM_WORLD.Size()-1){
                    start_buf[0] = finish;
                    MPI.COMM_WORLD.Send(start_buf,0,1, MPI.INT, me+1, MESSAGE_1);
                }
                if (finish > N){
                    finish = N;
                }

            }

            MPI.COMM_WORLD.Barrier();
            log( me, start, finish, k, h);



            MPI.COMM_WORLD.Barrier();
            double[][] A_send = new double[N][ Math.abs(finish - start)];

            MPI.COMM_WORLD.Barrier();
            boolean calculation = false;



            for (int j = start, u=0; j < finish ; j++, u++) {
                calculation = true;

            //for (int j = k + 1; j < N; j++) {
                for (int i = k + 1; i < N; i++){
                    //System.out.println("i="+ i + "  u="+ u + "  j="+j + "  k="+k + "  me="+ me +"  h="+ h);
                    //System.out.println("A_send=" + A_send.length + "   A_send[i]=" + A_send[i].length);
                    double test1 = A_send[i][u];
                    double test2 = A_resv[i][j];
                    double test3 = L[i][k];
                    double test4 = A_resv[k][j];
                    A_send[i][u] = A_resv[i][j] - L[i][k] * A_resv[k][j];

                }
            }
            if(me == MPI.COMM_WORLD.Size()-1){
                log("A_send from", A_send, me);
                if (calculation){
                    MPI.COMM_WORLD.Send(A_send, 0, N, MPI.OBJECT, me-1, MESSAGE_2);
                }else {
                    MPI.COMM_WORLD.Send(EMPTY, 0, N, MPI.OBJECT, me-1, MESSAGE_2);
                }

            }else {
                double [][] A_buf = new double[N][];
                if (me != 0){

                    MPI.COMM_WORLD.Recv(A_buf, 0, N, MPI.OBJECT, me+1, MESSAGE_2);
                    log("A_recv", A_buf, me);


                    if (calculation){
                        for (int m =0; m<N; m++){
                            //System.out.println("me = "+me+ "; "+Arrays.toString(A_buf[m]));
                            if (A_buf[m]!=null){
                                A_send[m] = Doubles.concat(A_send[m], A_buf[m]);
                            }
                        }
                        MPI.COMM_WORLD.Send(A_send, 0, N, MPI.OBJECT, me-1, MESSAGE_2);
                    }else {
                        MPI.COMM_WORLD.Send(EMPTY, 0, N, MPI.OBJECT, me-1, MESSAGE_2);
                    }
                }else {

                    MPI.COMM_WORLD.Recv(A_buf, 0, N , MPI.OBJECT, me+1, MESSAGE_2);
                    System.out.println("=============================");
                    log("A_buf", A_buf, me);
                    log("A_send", A_send, me);
                    log("A_resv", A_resv, me);

                    for (int m =0; m<N; m++){

                            A_send[m] = Doubles.concat(Arrays.copyOf(A_resv[m], start), A_send[m], A_buf[m]);
                       }
                    log("A_resv after concating", A_resv, me);
                    log("A_send after concating", A_send, me);
                    System.out.println("=============================");
                    A_resv = A_send;
                }
            }

            if (me==0){
                //log("A_recv", A_resv, me);
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

        if (me==0){
            System.out.println(Arrays.toString(x));
        }


        //System.out.println("Hi from <"+me+">");
        MPI.Finalize();
    }


    public static void log (String text, double [][] array, int me){

        System.out.println("me: " + me);
        System.out.print(" "+ text);
        System.out.println();
        for (double [] item : array){
            System.out.println(Arrays.toString(item));
        }
        System.out.println();
    }

    public static void log (int me, int start, int finish, int k,  int h){
        System.out.println("me = "+ me + "  start = "+ start + "  finish = "+ finish + "  k = "+ k +   "  h = "+ h);
    }
}
