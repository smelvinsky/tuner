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
    private double[] postProcessingData;
    private float sampleRate = soundRecorder.getSampleRate();

    private Semaphore mutex1 = new Semaphore(1);
    private Semaphore mutex2 = new Semaphore(1);  // klasa sun.awt.Mutex nie działa......... :/


    class PitchmeterDisplayTask extends Task
    {
        private double[] postFFTdata;

        PitchmeterDisplayTask(double[] postFFTdata)
        {
            postFFTdata = new double[postFFTdata.length];
            this.postFFTdata = postFFTdata;
        }

        protected Object call()
        {
            double dominantFreq =  pitchmeter.findDominantFreq(100, 4000, postFFTdata, sampleRate, rawMicData.length);
            Platform.runLater(() -> {
                pitchmeter.setDominantFreqIndicator(dominantFreq, mainMenu.getFreqIndicator());

            });

            return null;
        }
    }

    public void start(Stage primaryStage) throws Exception
    {
        NoteObject noteObject = new NoteObject(Note.C, Octave.twoLine);
        System.out.println(pitchmeter.getFreqByNote(noteObject));
        System.out.println(pitchmeter.getNoteByFreq(455).getNote());

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
                    mutex1.acquire();
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
                    mutex2.release();
                }
            }
        };

        Task soundProcessingTask = new Task()
        {
            protected Object call() throws Exception
            {
                double[] postFFTsignal;

                while (true)
                {
                    mutex2.acquire();
                    postFFTsignal = SoundProcessing.fft(rawMicData);

                    if ((spectrometer.getLowFreq() >= 0) && (spectrometer.getHiFreq() >= 0))
                    {
                        double[] postFilterSignal = SoundProcessing.bandwidthFilter(postFFTsignal, sampleRate, spectrometer.getLowFreq(), spectrometer.getHiFreq());
                        postFilterSignal = SoundProcessing.fftDisplayFormat(postFilterSignal);
                        postProcessingData = postFilterSignal;
                    }
                    else
                    {
                        double[] postFilterSignal;
                        postFilterSignal = SoundProcessing.fftDisplayFormat(postFFTsignal);
                        postProcessingData = postFilterSignal;
                    }

                    double freq = pitchmeter.findDominantFreq(27, 4200, postFFTsignal, sampleRate, rawMicData.length);

                    Platform.runLater(() -> pitchmeter.setDominantFreqIndicator(freq, mainMenu.getFreqIndicator()));
                    Platform.runLater(() -> spectrometer.setData(postProcessingData));  //lambda expr. ----> passes Runnable object to runlater func.

                    mutex1.release();
                }
            }
        };

        mutex2.acquire();

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
