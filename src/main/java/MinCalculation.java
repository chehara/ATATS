import org.apache.commons.math3.complex.Complex;

/**
 * Created by hp on 12/2/2017.
 */
public class MinCalculation {
    static double min=1000000000;
    public static double minOfCommercial(Complex[] data, int chunkSize){
        for (int j = 0; j < chunkSize; j++) {
            double minimum= Math.log(data[j].abs() + 1);
            if(min > minimum){
                min = minimum;
            }
        }
        return min;

    }
}
