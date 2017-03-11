import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

public class FingerprintSample {

    PrintWriter out1 = new PrintWriter("filename.txt");
    static final int MIN_MAP_SIZE = 108;//accoding to the chunk size we use for matching this can be changed 10s
    static final int MAX_MAP_SIZE = 968;//accoding to the chunk size we use for matching this can be changed 90s
    static LinkedHashMap<String, LinkedHashMap<Double, Long>> databaseHashesMap = new LinkedHashMap<String, LinkedHashMap<Double, Long>>();

    public FingerprintSample() throws FileNotFoundException {
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        //Read from DB and load to LHM
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader("audio/timehashMap.txt"));
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
        File fileIn = new File("audio/maggi.wav");
        LinkedHashMap<Double,Long> audioMap = new FingerprintSample().generateFingerprint(fileIn);
        if(MIN_MAP_SIZE <= audioMap.size() && MAX_MAP_SIZE >= audioMap.size()){
            //Matching match=new Matching();
        }
    }


    public final int[] RANGE = new int[] { 0, 1023, 2047, 3071, 4095 };
    long FUZ_FACTOR = 2;
    int LOWER_LIMIT = 1;
    int UPPER_LIMIT = 4096;

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
                System.arraycopy(arrayWithChunks[arrayWithChunksIndex], 0, finalBuffer, 0, arrayWithChunks[arrayWithChunksIndex].length);
                //out1.println(Arrays.toString(finalBuffer));

                //upto this level time domain data chunks
                double[] finalBufferReal = new double[chunkSize];
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
                //System.out.println("Time : "+second+"     Hash : "+hash);
            }

            //matching
            Matching m=new Matching();
            //parsing chunk block to match
            m.slidingWindowMatchNew(streamLHM,databaseHashesMap);



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

    private double[] getTopFreqValuesArray(double[] chunkArray){
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

    long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100 + (p1 - (p1 % FUZ_FACTOR));
    }

}