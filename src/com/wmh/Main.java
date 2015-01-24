package com.wmh;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application{

    MainWindowController mainWindowController = new MainWindowController();

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../res/MainWindow.fxml"));
        fxmlLoader.setRoot(mainWindowController);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        stage.setScene(new Scene(mainWindowController));
        stage.setTitle("XOR problem solved by neural network");
        stage.setWidth(300);
        stage.setHeight(200);
        stage.show();
    }
        /**
         * The main method.
         * @param args No arguments are used.
         */
        public static void main(final String args[]) {
            launch(args);

        }
}
