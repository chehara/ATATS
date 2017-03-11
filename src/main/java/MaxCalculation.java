import org.apache.commons.math3.complex.Complex;

/**
 * Created by hp on 12/2/2017.
 */
public class MaxCalculation {
    static double max=0;
    public static double maxOfCommercial(Complex[] data,int chunkSize){
        for (int j = 0; j < chunkSize; j++) {
            double maximum= Math.log(data[j].abs() + 1);
            if(max < maximum){
                max = maximum;
            }
        }
        return max;

    }

}
