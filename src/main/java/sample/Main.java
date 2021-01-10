package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {

    private IController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("FragmentMaker");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        controller = loader.getController();
        controller.setMainClass(this);
        controller.setStage(primaryStage);
    }

    int left = 0, top = 0, right = 0, bottom = 0, width = 0, height = 0;

    public void openSelectArea() {
        Stage stage = new Stage();
        stage.setMaximized(true);
        stage.setOpacity(0.4);
        stage.initStyle(StageStyle.UNDECORATED);

        Canvas canvas = new Canvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.AQUA);

        Group root = new Group();
        Scene scene = new Scene(root);
        scene.setCursor(Cursor.CROSSHAIR);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
                controller.keyEscPressed();
            }
        });
        scene.setOnMouseReleased(event -> {
            stage.close();
        });

        scene.setOnMousePressed(event -> {
            top = (int) event.getY();
            left = (int) event.getX();
        });
        scene.setOnMouseDragged(event -> {
         gc.clearRect(0,0,canvas.getWidth(), canvas.getHeight());
            bottom = (int) event.getY();
            right = (int) event.getX();
            width = right - left;
            height = bottom - top;
            gc.fillRect(Math.min(right, left), Math.min(bottom, top), (width > 0) ? width : width * -1, (height > 0) ? height : height * -1);
        });
        stage.setScene(scene);

        Pane pane = new Pane();
        pane.setStyle("-fx-background-color:rgba(0,0,0,128);");
        Label label = new Label("Press ESC to cancel");
        label.setStyle("-fx-text-fill:rgb(255,255,255);");
        StackPane vb = new StackPane();
        vb.getChildren().add(pane);
        vb.getChildren().add(label);
        StackPane.setAlignment(label, Pos.CENTER);
        pane.getChildren().add(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        StackPane.setAlignment(canvas, Pos.CENTER);
        scene.setRoot(vb);
        stage.show();
    }
    public static void doSomething(int a) {
    }
    public static void doSomething(int a, int b) {
    }
    public static void main(String[] args) {
        launch(args);
    }
}
