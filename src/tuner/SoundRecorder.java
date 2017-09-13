package tuner;

import javax.sound.sampled.*;


public class SoundRecorder
{
    private AudioFormat inputFormat;
    private TargetDataLine inputLine;

    SoundRecorder()
    {
        inputFormat = new AudioFormat(10000, 16, 1, true, true);
        inputLine = init();
    }

    SoundRecorder(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian)
    {
        inputFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        inputLine = init();
    }

    private TargetDataLine init()
    {
        System.out.println("Input line initialization...");
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, inputFormat);

        if(!AudioSystem.isLineSupported(targetInfo))
        {
            System.out.println("Input line is not supported");
            //input line error handle
        }
        try
        {
            inputLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            inputLine.open(inputFormat);
            System.out.println("Input line opened.");
            System.out.println("Input line initialization completed.");
            return inputLine;
        }
        catch (LineUnavailableException lue)
        {
            System.out.println("Input line is not available:");
            System.err.println(lue);
        }
        return null;
    }

    void start()
    {
        inputLine.start();
    }

    public int getBufferSize()
    {
        return inputLine.getBufferSize();
    }

    int record(byte[] byteArray, int offset, int length)
    {
            return inputLine.read(byteArray, offset, length);
    }

    float getSampleRate()
    {
        return inputFormat.getSampleRate();
    }
}
