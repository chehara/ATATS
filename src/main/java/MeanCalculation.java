import org.apache.commons.math3.complex.Complex;

/**
 * Created by hp on 12/2/2017.
 */
public class MeanCalculation {
static double mag=0;
    public static double meanOfCommercial(Complex[] data,int chunkSize){
        for (int j = 0; j < chunkSize; j++) {
            mag+= Math.log(data[j].abs() + 1);
        }
        return mag/chunkSize;

    }

}
