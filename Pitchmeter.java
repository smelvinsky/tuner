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

import java.text.DecimalFormat;

enum Octave
{
    SubContra(0), Contra(1), Great(2), Small(3), oneLine(4), twoLine(5), threeLine(6), forLine(7), fiveLine(8), ;

    private int octaveNumber;

    Octave(int octaveNumber)
    {
        this.octaveNumber = octaveNumber;
    }

    public int getOctaveNumber()
    {
        return octaveNumber;
    }
}

enum Note
{
    //European Notation (US B = EU H) !!!
    Ces(-1), C(0), Cis(1), Des(1), D(2), Dis(3), Es(3), E(4), Fes(4), Eis(5), F(5),
    Fis(6), Ges(6), G(7), Gis(8), As(8), A(9), Ais(10), B(10), H(11), His(12);

    private int halfStepsNumber;

    Note(int halfStepsNumber)
    {
        this.halfStepsNumber = halfStepsNumber;
    }

    public int getHalfStepsNumber()
    {
        return halfStepsNumber;
    }
}

class Pitchmeter
{
    private class NoteObject
    {
        private Note note;
        private Octave octave;

        public void setNote(Note note) {
            this.note = note;
        }

        public void setOctave(Octave octave) {
            this.octave = octave;
        }

        public Note getNote() {
            return note;
        }

        public Octave getOctave() {
            return octave;
        }
    }

    //assumes that spectrum equals ca. 27 - 4200 (Hz) - default piano bandwidth
    //which means -> A0 - C8
    private class NoteFreqTable
    {
        private NoteObject[] noteObjects;
        private double[] frequencies;

        NoteFreqTable(Note definedFixedNote, double definedFixedNoteFreq, Octave definedFixedNoteOctave)
        {

        }
    }

    private Note definedFixedNote;
    private double definedFixedNoteFreq;
    private Octave definedFixedNoteOctave;

    private double pow = (double) 1 / (double) 12;
    private final double a = Math.pow(2, pow);

    DecimalFormat dominantFreqFormat = new DecimalFormat("#.##");

    Pitchmeter(Note definedFixedNote, double definedFixedNoteFreq, Octave definedFixedNoteOctave)
    {
        this.definedFixedNote = definedFixedNote;
        this.definedFixedNoteFreq = definedFixedNoteFreq;
        this.definedFixedNoteOctave = definedFixedNoteOctave;
    }

    double calculateFreqFromNote(Note note, Octave octave)
    {
        int halfStepsNumber;

        halfStepsNumber = note.getHalfStepsNumber() - definedFixedNote.getHalfStepsNumber();
        halfStepsNumber = halfStepsNumber + ((octave.getOctaveNumber() - definedFixedNoteOctave.getOctaveNumber()) * 12);

        return (definedFixedNoteFreq * (Math.pow(a, halfStepsNumber)));
    }

    NoteObject calculateNoteFromFreq
    {

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

    void setDominantFreqValue(double dominantFreq, Label indicator)
    {
        indicator.setText(dominantFreqFormat.format(dominantFreq) + "Hz");
    }

    void setIndicatorVisible(boolean visible, Control indicator)
    {
        indicator.setVisible(visible);
    }
}


