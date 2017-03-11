/*File ForwardDCT01.java
Copyright 2006, R.G.Baldwin
Rev 01/03/06

The static method named transform performs a forward
Discreet Cosine Transform (DCT) on an incoming time series
and returns the DCT spectrum.

See http://en.wikipedia.org/wiki/Discrete_cosine_transform
#DCT-II and http://rkb.home.cern.ch/rkb/AN16pp/node61.html
for background on the DCT.

This formulation is from
http://www.cmlab.csie.ntu.edu.tw/cml/dsp/training/
coding/transform/dct.html


Incoming parameters are:
  double[] x - incoming real data
  double[] y - outgoing real data

Tested using J2SE 5.0 under WinXP.  Requires J2SE 5.0 or
later due to the use of static import of Math class.
**********************************************************/
import static java.lang.Math.*;

public class ForwardDCT01{

    public static void transform(double[] x,
                                 double[] y){

        int N = x.length;

        //Outer loop interates on frequency values.
        for(int k=0; k < N;k++){
            double sum = 0.0;
            //Inner loop iterates on time-series points.
            for(int n=0; n < N; n++){
                double arg = PI*k*(2.0*n+1)/(2*N);
                double cosine = cos(arg);
                double product = x[n]*cosine;
                sum += product;
            }//end inner loop

            double alpha;
            if(k == 0){
                alpha = 1.0/sqrt(2);
            }else{
                alpha = 1;
            }//end else
            y[k] = sum*alpha*sqrt(2.0/N);
        }//end outer loop
    }//end transform method
    //-----------------------------------------------------//
}//end class ForwardDCT01