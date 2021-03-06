package com.hungpham.UI.controllers;

import com.hungpham.Controller.SerialPortController;
import com.hungpham.FunctionsWrapper;
import com.hungpham.UI.graphs.AcceGraph;
import com.hungpham.UI.graphs.BaroGraph;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.hungpham.Controller.AlgorithmsController.algorithmsQueue;
import static com.hungpham.Controller.AlgorithmsController.voting;
import static com.hungpham.Main.firstTimeOpen;
import static com.hungpham.UI.MainScene.operatingDevicesNumber;

public class GraphStage extends Application {
    public static volatile String command = "";
    public static volatile int mode = 0;

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        RunBackground runBackground = new RunBackground();
        runBackground.start();

        SoundPlay soundPlay = new SoundPlay();
        soundPlay.start();

        Button backToMainPage = new Button("Back to Main Page");
        backToMainPage.setMinSize(200, 50);

        backToMainPage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                command = "stop";
                firstTimeOpen = false;
                SerialPortController.mode = 0;
                URL url = null;
                Parent root = null;
                for (int i = 0; i < operatingDevicesNumber; i++) {
                    algorithmsQueue[i].clear();
                }
                try {
                    url = new File("resources/fxmls/MainStage.fxml").toURL();
                    root = FXMLLoader.load(url);
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                Stage stage = (Stage) backToMainPage.getScene().getWindow();
                stage.setScene(new Scene(root, 800, 600));
                stage.setMinHeight(600);
                stage.setMinWidth(800);
                stage.show();
            }
        });

        AcceGraph[] acceGraph = new AcceGraph[operatingDevicesNumber];
        BaroGraph[] baroGraph = new BaroGraph[operatingDevicesNumber];
        LineChart<Number, Number>[] lc = new LineChart[operatingDevicesNumber];
        LineChart<Number, Number>[] lc1 = new LineChart[operatingDevicesNumber];
        FlowPane graphs = new FlowPane();
        GridPane[] graph = new GridPane[operatingDevicesNumber];

        ScrollPane root = new ScrollPane();

        GridPane gridPane = new GridPane();
        gridPane.add(backToMainPage, 0, 0);

        for (int i = 0; i < operatingDevicesNumber; i++) {
            acceGraph[i] = new AcceGraph(i);
            baroGraph[i] = new BaroGraph(i);

            lc[i] = acceGraph[i].getLineChart();
            lc1[i] = baroGraph[i].getLineChart();

            acceGraph[i].executeGraph();
            baroGraph[i].executeGraph();

            graph[i] = new GridPane();
            graph[i].add(lc[i], 0, 0);
            graph[i].add(lc1[i], 1, 0);

            if (i == 0) graphs.getChildren().add(gridPane);
            graphs.getChildren().add(graph[i]);
        }

        root.setFitToHeight(true);
        root.setFitToHeight(true);

        root.setContent(graphs);

        Scene scene = new Scene(root, 1280, 900);

        primaryStage.setTitle("SSNS Project");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    static class RunBackground extends Thread {
        @Override
        public void run() {
            FunctionsWrapper.startEverything();
        }
    }

    static class SoundPlay extends Thread {
        @Override
        public void run() {
            while (true) {
                if (voting[1] == 2 || voting[0] == 2) {
                    try {
                        String bip = "fall1.m4a";
                        Media hit = new Media(new File(bip).toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(hit);
                        mediaPlayer.play();
                    } catch (Exception e) {

                    }
                    break;
                }
            }
        }
    }
}
