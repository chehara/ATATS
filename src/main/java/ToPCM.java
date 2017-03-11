import javax.sound.sampled.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


/**
 * Created by hp on 5/2/2017.
 */
public class ToPCM {

    public ToPCM() throws FileNotFoundException {
    }

    private AudioFormat getFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 1;          //mono
        boolean signed = true;     //Indicates whether the data is signed or unsigned
        boolean bigEndian = true;  //Indicates whether the audio data is stored in big-endian or little-endian order
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public static void main(String[] args) {

    int totalFramesRead = 0;
    File fileIn = new File("audio/maggi.wav");

    try {
        AudioInputStream audioInputStream =
                AudioSystem.getAudioInputStream(fileIn);
        AudioFileFormat format = AudioSystem.getAudioFileFormat(fileIn);


        AudioFormat outDataFormat = new AudioFormat((float) 44100.0, (int) 16, (int) 1, true, false);
        System.out.println(outDataFormat.getEncoding());
        AudioInputStream lowResAIS;
        //if (AudioSystem.isConversionSupported(outDataFormat,audioInputStream.getFormat())) {
            lowResAIS = AudioSystem.getAudioInputStream(outDataFormat, audioInputStream);
            System.out.println("okayyy");
            System.out.println(lowResAIS.getFrameLength());
            //AudioInputStream decompressedStream = AudioSystem.getAudioInputStream(outDataFormat.getEncoding(), audioInputStream);
            //System.out.println(decompressedStream.getFrameLength());


        long durations = (long) (lowResAIS.getFrameLength() / lowResAIS.getFormat().getFrameRate());
        System.out.println("time duration : "+durations);
       // }

        ToPCM tt=new ToPCM();
        final AudioFormat format2 = tt.getFormat(); //Fill AudioFormat with the settings

        int bytesPerFrame =
                audioInputStream.getFormat().getFrameSize();

        if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
            // some audio formats may have unspecified frame size
            // in that case we may read any amount of bytes
            bytesPerFrame = 1;
        }
        // Set an arbitrary buffer size of 1024 frames.
        int numBytes = 4096 * bytesPerFrame;
        byte[] audioBytes = new byte[numBytes];

       // System.out.println(Arrays.deepToString(lowResAIS));
        //WaveFloatFileReader reader=new WaveFloatFileReader();
        //WavDat wav;
        int samplesPerChannel = (int) Math.rint(outDataFormat.getFrameRate());
        double[] avgBuffer = new double[samplesPerChannel];
        try {
             PrintWriter out2 = new PrintWriter("filenameIntPCM.txt");
            int numBytesRead = 0;
            int numFramesRead = 0;
            // Try to read numBytes bytes from the file.
            int count = 0;
            while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
                count++;
                // Calculate the number of frames actually read.
                numFramesRead = numBytesRead / bytesPerFrame;
                totalFramesRead += numFramesRead;
                //System.out.println(count);

                out2.println(Arrays.toString(audioBytes));

                //Arrays.sort(Arrays.toString(audioBytes));
                //int max = myArray[myArray.length - 1];
            }
            System.out.println(totalFramesRead);
        } catch (Exception ex) {
            // Handle the error...
        }


} catch (UnsupportedAudioFileException e1) {
        e1.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }}
