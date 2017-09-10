package tuner;

import javax.sound.sampled.*;
/**
 * Created by smelvinsky on 26.07.17.
 *
 * @AudioFormat - is the class that specifies a particular arrangement of data in a sound stream.
 * By examining the information stored in the audio format, you can discover how to interpret
 * the bits in the binary sound data.
 *
 * @TargetDataLine is a type of DataLine from which audio data can be read. The most common example
 * is a data line that gets its data from an audio capture device. (The device is implemented as a
 * mixer that writes to the target data line.) Note that the naming convention for this interface
 * reflects the relationship between the line and its mixer. From the perspective of an application,
 * a target data line may act as a source for audio data.
 * The target data line can be obtained from a mixer by invoking
 * the getLine method of Mixer with an appropriate DataLine.Info object.
 *
 * @DataLine.Info Besides the class information inherited from its superclass, DataLine.Info
 * provides additional information specific to data lines. This information includes:
 *      - the audio formats supported by the data line
 *      - the minimum and maximum sizes of its internal buffer
 * Because a Line.Info knows the class of the line its describes, a DataLine.Info object can describe
 * DataLine subinterfaces such as SourceDataLine, TargetDataLine, and Clip. You can query a mixer for
 * lines of any of these types, passing an appropriate instance of DataLine.Info as the argument to a
 * method such as Mixer.getLine(Line.Info).
 *
 * @AudioSystem class acts as the entry point to the sampled-audio system resources. This class
 * lets you query and access the mixers that are installed on the system. AudioSystem includes a
 * number of methods for converting audio data between different formats, and for translating between
 * audio files and streams. It also provides a method for obtaining a Line directly from the AudioSystem
 * without dealing explicitly with mixers.
 *
 * @AudioSystem.getLine() obtains a line that matches the description in the specified Line.Info object.
 * If a DataLine is requested, and info is an instance of DataLine.Info specifying at least one fully
 * qualified audio format, the last one will be used as the default format of the returned DataLine.
 *
 * @Line.open() opens the line, indicating that it should acquire any required system resources and
 * become operational. If this operation succeeds, the line is marked as open, and an OPEN event
 * is dispatched to the line's listeners. Note that some lines, once closed, cannot be reopened.
 * Attempts to reopen such a line will always result in an LineUnavailableException.
 * Some types of lines have configurable properties that may affect resource allocation.
 * For example, a DataLine must be opened with a particular format and buffer size. Such lines should
 * provide a mechanism for configuring these properties, such as an additional open method or methods
 * which allow an application to specify the desired settings.
 * This method takes no arguments, and opens the line with the current settings.
 * For SourceDataLine and TargetDataLine objects, this means that the line is opened with default settings.
 *
 */

public class SoundRecorder
{
    private AudioFormat inputFormat;
    private TargetDataLine inputLine;

    SoundRecorder()
    {
        inputFormat = new AudioFormat(44100, 16, 1, true, true);
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
