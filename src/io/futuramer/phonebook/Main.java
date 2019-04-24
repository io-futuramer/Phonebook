package io.futuramer.phonebook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Phonebook.fxml")); // reading fxml template from disk, parsing and loading the FX environment of the project
        primaryStage.setTitle("Phonebook Editor by Futuramer"); // setting title of the application window
        primaryStage.setScene(new Scene(root, 320, 350)); // setting size of the window
        primaryStage.setResizable(false); // it should not be resizable in order to avoid UI glitches
        primaryStage.show(); // showing the stage, making it visible for user
    }

    public static void main(String[] args) {
        launch(args);
    }
}
