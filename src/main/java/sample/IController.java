package sample;

import javafx.stage.Stage;

public interface IController {
    void setStage(Stage stage);
    void setMainClass(Main main);
    void keyEscPressed();
}
