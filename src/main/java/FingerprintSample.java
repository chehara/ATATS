import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.math3.complex.Complex;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class FingerprintSample {

    PrintWriter out1 = new PrintWriter("filename.txt");
    private static final int MIN_MAP_SIZE = 108;//accoding to the chunk size we use for matching this can be changed 10s
    private static final int MAX_MAP_SIZE = 968;//accoding to the chunk size we use for matching this can be changed 90s
    private static LinkedHashMap<String, LinkedHashMap<Double, Long>> databaseHashesMap = new LinkedHashMap<String, LinkedHashMap<Double, Long>>();
    private static CSVWriter csvWriter;
    static double meanCount=0, peak=0,base=10000;
    private static int count=0,count2=0;


    public FingerprintSample() throws FileNotFoundException {
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        //Read from DB and load to LHM


        File file = new File("D:/msckFullsampleFFTMagnitude.csv");
        if (!file.isFile()) {
            file.createNewFile();
        }

        csvWriter = new CSVWriter(new FileWriter(file));


        try {
            BufferedReader in = new BufferedReader(
                    new FileReader("audio/timehashMapNew.txt"));
            String str;
            str = in.readLine();

            while ((str = in.readLine()) != null) {
                String[] ar=str.split(",");
                LinkedHashMap<Double,Long> lhm=new LinkedHashMap<Double, Long>();
                //System.out.println(ar[0]+"  "+ar[1]+"   "+ar[2]);
                lhm.put(new Double(ar[1]),new Long(ar[2]));
                if(databaseHashesMap.containsKey(ar[0]))
                    databaseHashesMap.get(ar[0]).put(new Double(ar[1]), new Long(ar[2]));
                else
                    databaseHashesMap.putIfAbsent(ar[0],lhm);
                //System.out.println(databaseHashesMap);
            }
            // System.out.println(databaseHashesMap);
            in.close();

        } catch (IOException e) {
            System.out.println("File Read Error");
        }


        //Loading stream and matching
        File fileIn = new File("audio/TV/recording.wav");
        LinkedHashMap<Double,Long> audioMap = new FingerprintSample().generateFingerprint(fileIn);

        //matching
        Matching m=new Matching();

        //parsing chunk block to match
        m.slidingWindowMatchNew(audioMap,databaseHashesMap);

        if(MIN_MAP_SIZE <= audioMap.size() && MAX_MAP_SIZE >= audioMap.size()){
            //Matching match=new Matching();
        }

        //System.out.println("Mean magnitude value of the signal : "+new FingerprintSample().roundDoubles(meanCount/(count*count2)));
        //System.out.println("Maximum magnitude value of the signal : "+new FingerprintSample().roundDoubles(peak));
        //System.out.println("Minimum magnitude value of the signal : "+new FingerprintSample().roundDoubles(base));
    }


    private final int[] RANGE = new int[] {40,120,300};
    private final int[] RANGE2 = new int[] {3980,4040,4096};
    private long FUZ_FACTOR = 2;
    private int LOWER_LIMIT = 40;
    private int UPPER_LIMIT = 300;
    private int LOWER_LIMIT2 = 3980;
    private int UPPER_LIMIT2 = 4096;

    public LinkedHashMap<Double,Long> generateFingerprint(File sampleFile) throws UnsupportedAudioFileException, IOException {

        AudioInputStream in = AudioSystem.getAudioInputStream(sampleFile);
        AudioFormat baseFormat = in.getFormat();
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels()*2,
                baseFormat.getSampleRate(),
                false);
        int samplesPerChannel = (int) Math.rint(baseFormat.getFrameRate());
        int channelCount = baseFormat.getChannels();
        int size = samplesPerChannel * channelCount * 2;
        AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
        byte[] buffer = new byte[size];
        double[][] channels = new double[samplesPerChannel][];
        double[] avgBuffer = new double[samplesPerChannel];
        LinkedHashMap<Double,Long> hashTimeMap = new LinkedHashMap<Double,Long>();
        double second = 0;
        double timeIncrement;
        float sampleRate = baseFormat.getSampleRate();
        int chunkSize;

        for (int i = 0; i < channelCount; i++)
        {
            channels[i] = new double[samplesPerChannel];
        }

        while (din.read(buffer, 0, size) > -1) {
            count++;
            LinkedHashMap<Double, Long> streamLHM = new LinkedHashMap<Double, Long>();
            //System.out.println(Arrays.toString(buffer));
            for (int ch = 0; ch < channelCount; ch++) {
                for (int i = 0; i < samplesPerChannel; i++) {
                    channels[ch][i] = (buffer[channelCount*2*i + 2*ch ] + 256.0*buffer[channelCount*2*i + 2*ch+1]) / 32768.0;
                }
            }
            for (int i = 0; i < samplesPerChannel; i++) {
                double temp = channels[0][i];
                for (int ch = 1; ch < channelCount; ch++) {
                    temp += channels[ch][i];
                }
                avgBuffer[i] = temp / channelCount;
            }

            //System.out.println(avgBuffer);

            int totalSize = avgBuffer.length;
            if(32000 <= sampleRate){
                chunkSize = 4096;
                timeIncrement = 1/((double)(sampleRate/chunkSize));
            }else if((sampleRate <= 32000)&& (sampleRate > 16000)){
                chunkSize = 2048;
                timeIncrement = 1/((double)(sampleRate/chunkSize));
            }else if((sampleRate <= 16000)&& (sampleRate > 8000)){
                chunkSize = 1024;
                timeIncrement = 1/((double)(sampleRate/chunkSize));
            }else{
                chunkSize = 512;
                timeIncrement = 1/((double)(sampleRate/chunkSize));
            }


            double[] finalBuffer = new double[chunkSize];
            int chunkCount = totalSize/chunkSize;
            double[][] arrayWithChunks = new double[chunkCount][chunkSize];
            double[][] highscores = new double[chunkCount][4];
            double[][] points = new double[chunkCount][4];

            for (int i = 0; i < chunkCount; i++) {
                for (int j = 0; j < 4; j++) {
                    highscores[i][j] = 0;
                }
            }

            for (int i = 0; i < chunkCount; i++) {
                for (int j = 0; j < 4; j++) {
                    points[i][j] = 0;
                }
            }

            for(int i = 0; i < chunkCount; i++){
                double[] oneChunk = new double[chunkSize];
                for(int chunks = 0; chunks < chunkSize; chunks++){
                    oneChunk[chunks] = avgBuffer[(i*chunkSize)+chunks];
                }

                arrayWithChunks[i]=oneChunk;
            }
          //System.out.println(Arrays.deepToString(arrayWithChunks));

            for(int arrayWithChunksIndex = 0; arrayWithChunksIndex < arrayWithChunks.length; arrayWithChunksIndex++) {
                count2++;
                System.arraycopy(arrayWithChunks[arrayWithChunksIndex], 0, finalBuffer, 0, arrayWithChunks[arrayWithChunksIndex].length);
                //out1.println(Arrays.toString(finalBuffer));

                //upto this level time domain data chunks
               // double[] finalBufferReal = new double[chunkSize];
                //System.out.println(fin);

                Complex[] complex = new Complex[chunkSize];
                Complex[] fftComplex;
                for(int i = 0;i < finalBuffer.length;i++) {
                    //Put the time domain data into a complex number with imaginary part as 0:
                    complex[i] = new Complex(finalBuffer[i], 0);
                }

                fftComplex = FFT.fft(complex);

                //out1.println(Arrays.toString(fftComplex));
                //System.out.println(fftComplex[LOWER_LIMIT]);
                //CSVWritterExample.exportDataToExcel(csvWriter,fftComplex);

/*

                //Finding mean of the full signal
                meanCount+=MeanCalculation.meanOfCommercial(fftComplex,chunkSize);

                //Finding max of the full signal
                double maxValueofChunk=MaxCalculation.maxOfCommercial(fftComplex,chunkSize);
                if(peak < maxValueofChunk){
                    peak = maxValueofChunk;
                }

                //Finding min of the full signal
                double minValueofChunk=MinCalculation.minOfCommercial(fftComplex,chunkSize);
                if(base > minValueofChunk){
                    base = minValueofChunk;
                }
*/

                double[] getPoints = getTopFreqValuesArray(fftComplex, chunkSize);
                double[] getPoints2 = getTopFreqValuesArray2(fftComplex, chunkSize);
                long hash = hash((long) getPoints[0], (long) getPoints[1], (long) getPoints2[0], (long) getPoints2[1]);
                //System.out.println(hash);
                hashTimeMap.put(second, hash);
                second = roundDoubles(second + timeIncrement);
                //System.out.println("Nestomalt,"+second+","+hash);

                /*

                ForwardDCT01.transform(finalBuffer,finalBufferReal);
                //out1.println(Arrays.toString(finalBufferReal));

                double[] getPoints = getTopFreqValuesArray(finalBufferReal);
                //System.out.println(Arrays.toString(getPoints));
                long hash = hash((long) (getPoints[0]*100), (long) (getPoints[1]*100), (long) (getPoints[2]*100), (long) (getPoints[3]*100));
                //System.out.println(hash);
                hashTimeMap.put(second, hash);


                //putting local hashMap size=1
                streamLHM.put(second, hash);

                second = roundDoubles(second + timeIncrement);
                //System.out.println("Time : "+second+"     Hash : "+hash);*/
            }

            //matching
            //Matching m=new Matching();
            //parsing chunk block to match
           // m.slidingWindowMatchNew(streamLHM,databaseHashesMap);



            //System.out.println("yyyyyyyyyyyyyyyyyyyyyyy");
        }
        in.close();
        return hashTimeMap;
    }

    private double roundDoubles(Double valueToConvert){
        DecimalFormat decimalFormat =  new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        Double d = valueToConvert;
        double valueConverted = Double.parseDouble(decimalFormat.format(d));
        return valueConverted;
    }



    private double[] getTopFreqValuesArray1(double[] chunkArray){
        double[] points = new double[4];
        for(int i=0;i+1<RANGE.length;i++){
            double mag = 0;
            for (int j = RANGE[i]; j < RANGE[i+1]; j++) {
                double temp = chunkArray[j];
                if (temp > mag) {
                    mag=temp;
                }
            }points[i]=roundDoubles(mag);
        }
        return points;
    }

    public int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] <= freq) {
            i++;
        }
        return i;
    }


    public double[] getTopFreqValuesArray(Complex[] chunkComplexArray,int chunkCount){
        double[] highscores = new double[2];
        double[] points = new double[2];
        //System.out.println("complex : "+chunkComplexArray[LOWER_LIMIT].abs()+"      "+chunkComplexArray[UPPER_LIMIT].abs());
        for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT; freq++) {
                double magnitute = Math.log(chunkComplexArray[freq].abs() + 1);
                int index = getIndex(freq);
                if (magnitute > highscores[index - 1]) {
                    highscores[index - 1] = magnitute;
                    points[index - 1] = freq;
                }

        }
        return points;
    }

    public int getIndex2(int freq) {
        int i = 0;
        while (RANGE2[i] <= freq) {
            i++;
        }
        return i;
    }


    public double[] getTopFreqValuesArray2(Complex[] chunkComplexArray,int chunkCount){
        double[] highscores = new double[2];
        double[] points = new double[2];
        //System.out.println("complex : "+chunkComplexArray[LOWER_LIMIT].abs()+"      "+chunkComplexArray[UPPER_LIMIT].abs());
        for (int freq = LOWER_LIMIT2; freq < UPPER_LIMIT2; freq++) {
            double magnitute = Math.log(chunkComplexArray[freq].abs() + 1);
            int index = getIndex2(freq);
            if (magnitute > highscores[index - 1]) {
                highscores[index - 1] = magnitute;
                points[index - 1] = freq;
            }

        }
        return points;
    }

    //Using a little bit of error-correction, damping
    long hash(long p1, long p2, long p3, long p4) {
        return  (p4 - (p4 % FUZ_FACTOR)) * 1000000000 + (p3 - (p3 % FUZ_FACTOR))
                * 1000000 + (p2 - (p2 % FUZ_FACTOR)) * 1000 + (p1 - (p1 % FUZ_FACTOR));
    }

}