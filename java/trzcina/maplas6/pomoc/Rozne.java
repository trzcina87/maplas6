package trzcina.maplas6.pomoc;

/**
 * Created by piotr.trzcinski on 21.08.2017.
 */

public class Rozne {

    public static void czekaj(int milisekundy) {
        try {
            Thread.sleep(milisekundy);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
