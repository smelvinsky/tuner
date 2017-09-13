package tuner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.Semaphore;

public class Tuner extends Application
{
    //GUI components
    private Pitchmeter pitchmeter = new Pitchmeter(Note.A, 440, Octave.oneLine); // a razkreślne
    private Spectrometer spectrometer = new Spectrometer(800, 200, Color.BLACK, 8);
    private MainMenu mainMenu = new MainMenu(300, 500, spectrometer, pitchmeter);

    private SoundRecorder soundRecorder = new SoundRecorder();

    private double[] rawMicData = new double[8192];
    private double[] postFFTsignal;
    private double[] postProcessingData;
    private float sampleRate = soundRecorder.getSampleRate();

    private Semaphore semaphore1 = new Semaphore(2);
    private Semaphore mutex1 = new Semaphore(1);  // klasa sun.awt.Mutex nie działa......... :/


    public void start(Stage primaryStage) throws Exception
    {
        mainMenu.show();

        Task microphoneDataTask = new Task()
        {
            protected Object call() throws Exception
            {
                int numBytesRead;
                byte[] inputSignal = new byte[8192];    /* const array size (power of 2) for FFT algorithm  */

                soundRecorder.start();

                System.out.println("Audio capture started...");
                while (true)
                {
                    try
                    {
                        semaphore1.acquire();
                        numBytesRead = soundRecorder.record(inputSignal, 0, inputSignal.length);
                        if (numBytesRead == -1)
                        {
                            System.err.println("SoundRecorder.record(...) problem");
                            System.exit(999);
                        }
                        for (int i = 0; i < inputSignal.length; i++)
                        {
                            rawMicData[i] = inputSignal[i];
                        }
                        mutex1.release();
                    }
                    catch (Exception e)
                    {
                        System.exit(20);
                    }
                }
            }
        };

        Task soundProcessingTask = new Task()
        {
            protected Object call() throws Exception
            {
                while (true)
                {
                    try {
                        mutex1.acquire();
                        postFFTsignal = SoundProcessing.fft(rawMicData);

                        if ((spectrometer.getLowFreq() >= 0) && (spectrometer.getHiFreq() >= 0)) {
                            double[] postFilterSignal = SoundProcessing.bandwidthFilter(postFFTsignal, sampleRate, spectrometer.getLowFreq(), spectrometer.getHiFreq());
                            postFilterSignal = SoundProcessing.fftDisplayFormat(postFilterSignal);
                            postProcessingData = postFilterSignal;
                        } else {
                            double[] postFilterSignal;
                            postFilterSignal = SoundProcessing.fftDisplayFormat(postFFTsignal);
                            postProcessingData = postFilterSignal;
                        }

                        double freq = pitchmeter.findDominantFreq(27, 4200, postFFTsignal, sampleRate, rawMicData.length);

                        Platform.runLater(() -> pitchmeter.setDominantFreqIndicator(freq, mainMenu.getFreqIndicator()));
                        Platform.runLater(() -> spectrometer.setData(postProcessingData));  //lambda expr. ----> passes Runnable object to runlater func.

                        semaphore1.release(2);
                    }
                    catch (Exception e)
                    {
                        System.exit(21);
                    }
                }
            }
        };

        Task pitchmeterTask = new Task()
        {
            protected Object call() throws Exception
            {

                while (true)
                {
                    try
                    {
                        double dominantFreq = pitchmeter.findDominantFreq(100, 4000, postFFTsignal, sampleRate, rawMicData.length);

                        Platform.runLater(() -> {
                            pitchmeter.setDominantFreqIndicator(dominantFreq, mainMenu.getFreqIndicator());
                            NoteObject noteObject = pitchmeter.getNoteByFreq(dominantFreq);
                            pitchmeter.setNoteIndicatorWithNoteObject(noteObject, mainMenu.getNoteIndicator(), mainMenu.getSharpNoteIndicator());
                        });
                    }
                    catch (Exception e)
                    {
                        System.exit(22);
                    }
                }
            }
        };

        semaphore1.acquire();
        mutex1.acquire();

        Thread microphoneDataThread = new Thread(microphoneDataTask);
        microphoneDataThread.start();

        Thread soundProcessingThread = new Thread(soundProcessingTask);
        soundProcessingThread.start();

    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
