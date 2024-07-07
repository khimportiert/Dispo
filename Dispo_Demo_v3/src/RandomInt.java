import java.util.Random;

public class RandomInt {

    static int between(int low, int high) {
        Random r = new Random();
        return r.nextInt(high-low) + low;
    }
}
