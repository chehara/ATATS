import org.apache.commons.math3.complex.Complex;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by hp on 12/2/2017.
 */
public class Xcorr {
    //public static void main(String[] args) throws IOException, UnsupportedAudioFileException {

       // DSP.calculateXcorr(Xcorr.generateSignalArray(new File("audio/maggi.wav")),Xcorr.generateSignalArray(new File("audio/vidMaggi.wav")));
    //}

    public static double[] generateSignalArray(File sampleFile) throws IOException, UnsupportedAudioFileException {

        AudioInputStream in = AudioSystem.getAudioInputStream(sampleFile);
        AudioFormat baseFormat = in.getFormat();
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        int samplesPerChannel = (int) Math.rint(baseFormat.getFrameRate());
        int channelCount = baseFormat.getChannels();
        int size = samplesPerChannel * channelCount * 2;
        AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
        byte[] buffer = new byte[size];
        double[][] channels = new double[samplesPerChannel][];
        double[] avgBuffer = new double[samplesPerChannel];
        int chunkSize;

        for (int i = 0; i < channelCount; i++) {
            channels[i] = new double[samplesPerChannel];
        }
        ArrayList<Double> signal1ArrayList=new ArrayList<Double>();

        while (din.read(buffer, 0, size) > -1) {
            for (int ch = 0; ch < channelCount; ch++) {
                for (int i = 0; i < samplesPerChannel; i++) {
                    channels[ch][i] = (buffer[channelCount * 2 * i + 2 * ch] + 256.0 * buffer[channelCount * 2 * i + 2 * ch + 1]) / 32768.0;
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
            chunkSize = 4096;
            double[] finalBuffer = new double[chunkSize];
            int chunkCount = totalSize/chunkSize;
            double[][] arrayWithChunks = new double[chunkCount][chunkSize];

            for(int i = 0; i < chunkCount; i++){
                double[] oneChunk = new double[chunkSize];
                for(int chunks = 0; chunks < chunkSize; chunks++){
                    oneChunk[chunks] = avgBuffer[(i*chunkSize)+chunks];
                }

                arrayWithChunks[i]=oneChunk;
            }


            for(int arrayWithChunksIndex = 0; arrayWithChunksIndex < arrayWithChunks.length; arrayWithChunksIndex++) {
                System.arraycopy(arrayWithChunks[arrayWithChunksIndex], 0, finalBuffer, 0, arrayWithChunks[arrayWithChunksIndex].length);
                Complex[] complex = new Complex[chunkSize];
                Complex[] fftComplex;

                for (int i = 0; i < finalBuffer.length; i++) {
                    complex[i] = new Complex(finalBuffer[i], 0);
                }

                fftComplex = FFT.fft(complex);
                for (int j = 0; j < chunkSize; j++){
                    signal1ArrayList.add(Math.log(fftComplex[j].abs() + 1));
                }
            }
        }
        // Double ArrayList to double[]
        double[] target = new double[signal1ArrayList.size()];
        for (int i = 0; i < target.length; i++) {
            target[i] = signal1ArrayList.get(i);
            // java 1.5+ style (outboxing)
           // System.out.println(target[i]);
        }

        return target;
    }


}
