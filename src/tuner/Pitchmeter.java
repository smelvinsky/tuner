package tuner;

/*

fn = f0 * (a)^n

a = (2)^(1/12) ~= 1.059463094359...
fn - the frequency of the note n half steps away.
f0 - the frequency of one fixed note which must be defined.
     A common choice is setting the A above middle C (A4) at f0 = 440 Hz.
n - the number of half steps away from the fixed note you are.
    If you are at a higher note, n is positive. If you are on a lower note, n is negative.

 */

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;

import java.text.DecimalFormat;

class Pitchmeter
{
    //assumes that spectrum equals ca. 27 - 4200 (Hz) - default piano bandwidth
    //which means -> A0 - C8
    private class NoteFreqTable
    {
        private Note definedFixedNote;
        private Octave definedFixedNoteOctave;
        private double definedFixedNoteFreq;

        private NoteObject[] noteObjects = new NoteObject[Note.values().length * Octave.values().length];
        private double[] frequencies = new double[noteObjects.length];

        private double pow = (double) 1 / (double) 12;
        private final double a = Math.pow(2, pow);

        private NoteFreqTable(Note definedFixedNote, Octave definedFixedNoteOctave, double definedFixedNoteFreq)
        {
            this.definedFixedNote = definedFixedNote;
            this.definedFixedNoteOctave = definedFixedNoteOctave;
            this.definedFixedNoteFreq = definedFixedNoteFreq;

            Note[] notes = Note.values();
            Octave[] octaves = Octave.values();

            for (int i = 0; i < octaves.length; i++)
            {
                for (int j = 0; j < notes.length; j++)
                {
                    noteObjects[(i * notes.length) + j] = new NoteObject(notes[j], octaves[i]);
                    frequencies[(i * notes.length) + j] = calculateFrequencyFromNote(notes[j], octaves[i]);
                }
            }
        }

        private double calculateFrequencyFromNote(Note note, Octave octave)
        {
            int halfStepsNumber;

            halfStepsNumber = note.getHalfStepsNumber() - definedFixedNote.getHalfStepsNumber();
            halfStepsNumber = halfStepsNumber + ((octave.getOctaveNumber() - definedFixedNoteOctave.getOctaveNumber()) * 12);

            return (definedFixedNoteFreq * (Math.pow(a, halfStepsNumber)));
        }

        private double getFreqByNote(NoteObject noteObject)
        {
            int i = 0;

            while(i < frequencies.length)
            {
                if ((noteObject.getNote() == noteObjects[i].getNote()) && noteObject.getOctave() == noteObjects[i].getOctave())
                {
                    break;
                }
                i++;
            }

            return frequencies[i];
        }

        private NoteObject getNoteByFreq(double freq)
        {
            if (freq > frequencies[frequencies.length - 1] || freq < frequencies[0])
            {
                throw new IllegalArgumentException();
            }

            int i;
            double leftRange = 0, rightRange = frequencies[frequencies.length - 1];

            for (i = 0; i < frequencies.length; i++)
            {
                if (frequencies[i] < freq)
                {
                    leftRange = frequencies[i];
                    continue;
                }
                rightRange = frequencies[i];
                break;
            }

            if (Math.abs(leftRange - freq) >= Math.abs(rightRange - freq))
            {
                return noteObjects[i];
            }
            else
            {
                return noteObjects[i - 1];
            }
        }
    }

    private NoteFreqTable noteFreqTable;

    private DecimalFormat dominantFreqFormat = new DecimalFormat("#.##");

    Pitchmeter(Note definedFixedNote, double definedFixedNoteFreq, Octave definedFixedNoteOctave)
    {
        this.noteFreqTable = new NoteFreqTable(definedFixedNote, definedFixedNoteOctave, definedFixedNoteFreq);
    }

    double findDominantFreq(int lowFreq, int hiFreq, double[] postFFTsignal, float sampleRate, int rawDataLenght)
    {
        double[] pichmeterData = new double[postFFTsignal.length];
        System.arraycopy(postFFTsignal, postFFTsignal.length / 2, pichmeterData, 0, postFFTsignal.length / 2);

        if(lowFreq >= hiFreq)
        {
            throw new IllegalArgumentException("lowFreq parameter must have lower value than hiFreq");
        }

        double deltaF = 1.0 / (rawDataLenght * (1.0 / ((double) sampleRate - 1.0)));
        double highestValue = 0;
        int lFreq = lowFreq / 2;
        int hFreq = hiFreq / 2;

        int lowFreqSample = 0;
        int hiFreqSample = 0;
        int dominantSample;

        for (int i = 0; i < pichmeterData.length; i++)
        {
            if (i * deltaF < lFreq)
            {
                lowFreqSample = i + 1;
            }

            if (i * deltaF > hFreq)
            {
                hiFreqSample = i;
                break;
            }
        }

        dominantSample = lowFreqSample;

        for (int i = lowFreqSample; i <= hiFreqSample; i++)
        {
            if(Math.abs(pichmeterData[i]) > highestValue)
            {
                highestValue = Math.abs(pichmeterData[i]);
                dominantSample = i * 2;
            }
        }

        return dominantSample * deltaF;
    }

    void setDominantFreqIndicator(double dominantFreq, Label indicator)
    {
        indicator.setText(dominantFreqFormat.format(dominantFreq) + "Hz");
    }

    void setIndicatorVisible(boolean visible, Control indicator)
    {
        indicator.setVisible(visible);
    }

    double getFreqByNote(NoteObject noteObject)
    {
        return noteFreqTable.getFreqByNote(noteObject);
    }

    NoteObject getNoteByFreq(double freq)
    {
        return noteFreqTable.getNoteByFreq(freq);
    }

    void setNoteIndicatorWithNoteObject(NoteObject noteObject, Labeled noteIndicator, Control sharpNoteIndicator)
    {
        if(noteObject.getNote().getHalfStepsNumber() == 0
                || noteObject.getNote().getHalfStepsNumber() == 2
                || noteObject.getNote().getHalfStepsNumber() == 4
                || noteObject.getNote().getHalfStepsNumber() == 5
                || noteObject.getNote().getHalfStepsNumber() == 7
                || noteObject.getNote().getHalfStepsNumber() == 9
                || noteObject.getNote().getHalfStepsNumber() == 11)
        {
            noteIndicator.setText(noteObject.getNote().toString());
            sharpNoteIndicator.setVisible(false);
        }
        else
        {
            noteIndicator.setText(new NoteObject(Note.getNoteFromHalfStepsNumber(noteObject.getNote().getHalfStepsNumber() - 1), noteObject.getOctave()).getNote().toString());
            sharpNoteIndicator.setVisible(true);
        }
    }
}


