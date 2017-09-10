package tuner;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

class Spectrometer
{
    private float rectangleWidth;
    private float rectangleSpace;
    private Paint plotColor;
    private int numOfRectangles;
    private int lowFreq = -1;
    private int hiFreq = -1;

    private Group spectrometerGroup = new Group();
    private Stage spectrometerStage = new Stage();
    private Scene spectrometerScene;
    private volatile List<Rectangle> rectangles = new ArrayList<>();

    Spectrometer(int width, int height, Paint backgroundColor, Paint plotColor, float rectangleWidth)
    {
        spectrometerScene = new Scene(spectrometerGroup, width, height, backgroundColor);
        spectrometerStage.setTitle("Spectrometer");
        spectrometerStage.setScene(spectrometerScene);

        this.rectangleWidth = rectangleWidth;
        this.plotColor = plotColor;

        rectangleSpace = rectangleWidth / 5;

        numOfRectangles = (int) (width / (rectangleWidth + rectangleSpace));

        for (int i = 0; i < numOfRectangles; i++)
        {
            addRectangle(i);
        }
    }

    Spectrometer(int width, int height, Paint backgroundColor, Paint plotColor, float rectangleWidth, int lowFreq, int hiFreq)
    {
        this(width, height, backgroundColor, plotColor, rectangleWidth);
        this.lowFreq = lowFreq;
        this.hiFreq = hiFreq;
    }

    Spectrometer(int width, int height, Paint backgroundColor, float rectangleWidth)
    {
        this(width, height, backgroundColor, Color.WHITE, rectangleWidth);

        Color[] colors = new Color[numOfRectangles];

        double rgbStep = 1.0 / (numOfRectangles / 5.0);

        for (int i = 0; i < numOfRectangles; i++)
        {
            try
            {
                if (i * rgbStep <= 1.0)
                {
                    colors[i] = new Color(i * rgbStep, 0, 0, 1.0);
                }
                else if ((i * rgbStep > 1.0) & (i * rgbStep <= 2.0))
                {
                    colors[i] = new Color(1.0, (i * rgbStep) - 1.0, 0, 1.0);
                }
                else if ((i * rgbStep > 2.0) & (i * rgbStep <= 3.0))
                {
                    colors[i] = new Color(3.0 - (i * rgbStep), 1.0, 0, 1.0);
                }
                else if ((i * rgbStep > 3.0) & (i * rgbStep <= 4.0))
                {
                    colors[i] = new Color(0, 1.0, (i * rgbStep) - 3, 1.0);
                }
                else
                {
                    colors[i] = new Color(0, 5.0 - (i * rgbStep),1.0, 1.0);
                }
            }
            catch (IllegalArgumentException iae)
            {
                colors[i] = new Color(1.0, 1.0, 1.0, 1.0);
            }
            rectangles.get(i).setFill(colors[i]);
        }

    }

    void setData(double[] data)
    {
        double[] dataToSet = SoundProcessing.normalize(data, spectrometerScene.getHeight());
        //double[] dataToSet = data;

        for (int i = 0; i < dataToSet.length; i++)
        {
            dataToSet[i] = Math.abs(dataToSet[i]);
        }

        dataToSet = SoundProcessing.logScale(dataToSet);

        if(dataToSet.length <= numOfRectangles)
        {
            for (int i = 0; i < dataToSet.length; i++)
            {
                setRectangleHeight(i, dataToSet[i]);
            }
        }
        else
        {
            double avg;

            for (int i = 0; i < numOfRectangles; i++)
            {
                avg = 0;
                for (int j = 0; j < dataToSet.length / numOfRectangles; j++)
                {
                    avg = avg + dataToSet[(dataToSet.length / numOfRectangles) * i + j];
                }
                avg = avg / (dataToSet.length / numOfRectangles);
                setRectangleHeight(i, avg);
            }
        }
    }

    private void addRectangle(int rectangleIndex)
    {
        rectangles.add(new Rectangle(rectangleWidth, 1, plotColor));
        setRectangleHeight(rectangleIndex, 1);
        rectangles.get(rectangleIndex).setRotate(180);
        spectrometerGroup.getChildren().add(rectangles.get(rectangleIndex));
        rectangles.get(rectangleIndex).setX(rectangleIndex * (rectangleWidth + rectangleSpace));
    }

    private void setRectangleHeight(int rectangleIndex, double rectangleHeight)
    {
        rectangles.get(rectangleIndex).setHeight(rectangleHeight);
        rectangles.get(rectangleIndex).setY(spectrometerScene.getHeight() - rectangleHeight);
    }

    int getNumOfRectangles()
    {
        return numOfRectangles;
    }

    int getLowFreq()
    {
        return this.lowFreq;
    }

    int getHiFreq()
    {
        return this.hiFreq;
    }

    void show()
    {
        spectrometerStage.show();
    }
}
