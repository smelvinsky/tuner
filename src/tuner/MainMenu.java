package tuner;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.Stage;

class MainMenu
{
    private Group mainMenuGroup = new Group();
    private Stage mainMenuStage = new Stage();
    private Scene mainMenuScene;

    private Button spectrometerButton = new Button("Spectrometer");
    private Button closeButton = new Button("Close");

    private Label noteIndicator = new Label();
    private Label freqIndicator = new Label();
    private Label sharpNoteIndicator = new Label("#");
    private Label flatNoteIndicator = new Label("b");
    private Label octaveIndicator = new Label();

    private CheckBox showFreqCheckBox = new CheckBox("Show freq.");
    private CheckBox showOctaveCheckBox = new CheckBox("Show octave");

    private Circle[] pitchDeviationIndicator = new Circle[7];

    private Spectrometer spectrometer;
    private Pitchmeter pitchmeter;

    MainMenu(int width, int height, Spectrometer spectrometer, Pitchmeter pitchmeter)
    {
        mainMenuScene = new Scene(mainMenuGroup, width, height, Color.SLATEGRAY);
        mainMenuStage.setTitle("Tuner");
        mainMenuStage.setScene(mainMenuScene);
        mainMenuStage.setResizable(false);

        this.spectrometer = spectrometer;
        this.pitchmeter = pitchmeter;

        spectrometerButton.setLayoutX(20);
        spectrometerButton.setLayoutY(mainMenuScene.getHeight() - 50);
        spectrometerButton.setFont(Font.font("Apple Casual"));
        spectrometerButton.setStyle("-fx-background-color: rgba(53,53,82,0.78)");
        spectrometerButton.setTextFill(Color.ANTIQUEWHITE);

        spectrometerButton.setOnAction((ActionEvent ae) -> this.spectrometer.show());

        closeButton.setLayoutX(mainMenuScene.getWidth() - 75);
        closeButton.setLayoutY(mainMenuScene.getHeight() - 50);
        closeButton.setFont(Font.font("Apple Casual"));
        closeButton.setStyle("-fx-background-color: rgba(53,53,82,0.78)");
        closeButton.setTextFill(Color.ANTIQUEWHITE);

        closeButton.setOnAction((ActionEvent ae) -> System.exit(1));

        noteIndicator.setLayoutX((mainMenuScene.getWidth() / 2) - 35);
        noteIndicator.setLayoutY(150);
        noteIndicator.setFont(Font.font("Apple Casual", 90));
        noteIndicator.setTextFill(Color.ANTIQUEWHITE);

        freqIndicator.setLayoutX((mainMenuScene.getWidth() / 2) - 30);
        freqIndicator.setLayoutY(mainMenuScene.getHeight() - 200);
        freqIndicator.setFont(Font.font("Apple Casual", 15));
        freqIndicator.setTextFill(Color.ANTIQUEWHITE);
        freqIndicator.setVisible(false);

        sharpNoteIndicator.setLayoutX((mainMenuScene.getWidth() / 2) + 25);
        sharpNoteIndicator.setLayoutY(150);
        sharpNoteIndicator.setFont(Font.font("Apple Casual", 50));
        sharpNoteIndicator.setTextFill(Color.ANTIQUEWHITE);
        sharpNoteIndicator.setVisible(false);

        octaveIndicator.setLayoutX(mainMenuScene.getWidth() / 2 - 15);
        octaveIndicator.setLayoutY(240);
        octaveIndicator.setFont(Font.font("Apple Casual", 35));
        octaveIndicator.setTextFill(Color.ANTIQUEWHITE);
        octaveIndicator.setVisible(false);

        flatNoteIndicator.setLayoutX((mainMenuScene.getWidth() / 2) + 30);
        flatNoteIndicator.setLayoutY(195);
        flatNoteIndicator.setFont(Font.font("Apple Casual", 50));
        flatNoteIndicator.setTextFill(Color.ANTIQUEWHITE);
        flatNoteIndicator.setVisible(false);

        showFreqCheckBox.setLayoutX(5);
        showFreqCheckBox.setLayoutY(5);
        showFreqCheckBox.setFont(Font.font("Apple Casual"));
        showFreqCheckBox.setTextFill(Color.ANTIQUEWHITE);

        showFreqCheckBox.setOnAction((ActionEvent ae) -> {
            if(showFreqCheckBox.isSelected())
            {
                freqIndicator.setVisible(true);
            }
            else
            {
                freqIndicator.setVisible(false);
            }
        });

        showOctaveCheckBox.setLayoutX(5);
        showOctaveCheckBox.setLayoutY(30);
        showOctaveCheckBox.setFont(Font.font("Apple Casual"));
        showOctaveCheckBox.setTextFill(Color.ANTIQUEWHITE);

        showOctaveCheckBox.setOnAction((ActionEvent ae) -> {
            if(showOctaveCheckBox.isSelected())
            {
                octaveIndicator.setVisible(true);
            }
            else
            {
                octaveIndicator.setVisible(false);
            }
        });

        for (int i = 0; i < pitchDeviationIndicator.length; i++)
        {
            pitchDeviationIndicator[i] = new Circle(5, Color.rgb(168, 170, 167));
            pitchDeviationIndicator[i].setCenterX(55 + i*30);
            pitchDeviationIndicator[i].setCenterY(120);
        }

                                    add(pitchDeviationIndicator, mainMenuGroup);
        mainMenuGroup.getChildren().add(showOctaveCheckBox);
        mainMenuGroup.getChildren().add(octaveIndicator);
        mainMenuGroup.getChildren().add(showFreqCheckBox);
        mainMenuGroup.getChildren().add(sharpNoteIndicator);
        mainMenuGroup.getChildren().add(flatNoteIndicator);
        mainMenuGroup.getChildren().add(freqIndicator);
        mainMenuGroup.getChildren().add(noteIndicator);
        mainMenuGroup.getChildren().add(spectrometerButton);
        mainMenuGroup.getChildren().add(closeButton);
    }

    void show()
    {
        mainMenuStage.show();
    }

    Label getFreqIndicator()
    {
        return freqIndicator;
    }

    Label getSharpNoteIndicator()
    {
        return sharpNoteIndicator;
    }

    Label getFlatNoteIndicator()
    {
        return flatNoteIndicator;
    }

    Label getNoteIndicator()
    {
        return noteIndicator;
    }

    Label getOctaveIndicator()
    {
        return octaveIndicator;
    }

    Shape[] getPitchDeviationIndicator()
    {
        return pitchDeviationIndicator;
    }

    private void add(Node[] nodes, Group group)
    {
        for (int i = 0; i < nodes.length; i++)
        {
            group.getChildren().add(nodes[i]);
        }
    }
}
