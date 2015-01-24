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
            // create a neural network, without using a factory
          /*  BasicNetwork network = new BasicNetwork();
            network.addLayer(new BasicLayer(null, true, 2));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 3));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
            network.getStructure().finalizeStructure();
            network.reset();

            // create training data
           // MLDataSet trainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);
            NeuralDataSet dataSet = new BasicNeuralDataSet(XOR_INPUT, XOR_IDEAL);

            // backpropagation the neural network
            //final ResilientPropagation backpropagation = new ResilientPropagation(network, trainingSet);

            //back propagation
            final Backpropagation backpropagation = new Backpropagation(network, dataSet);
            int epoch = 1;
            //ONE
            do {
                backpropagation.iteration();
                System.out.println("Epoch #" + epoch + " Error:" + backpropagation.getError());
                epoch++;
            } while (backpropagation.getError() > 0.01);
            backpropagation.finishTraining();

            // test the neural network
            System.out.println("Neural Network Results:");
            for (NeuralDataPair pair : dataSet) {
                final NeuralData output = network.compute(pair.getInput());
                System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                        + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
            }
            network.reset();
            //TWO
            //hebbian training
            final HebbianTraining hebbianTraining = new HebbianTraining(network, dataSet, false, 0.01);
            do {
                hebbianTraining.iteration();
                System.out.println("Epoch #" + epoch + " Error:" + hebbianTraining.getError());
                epoch++;
            } while (hebbianTraining.getError() > 0.01);
            hebbianTraining.finishTraining();

            // test the neural network
            System.out.println("Neural Network Results:");
            for (NeuralDataPair pair : dataSet) {
                final NeuralData output = network.compute(pair.getInput());
                System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                        + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
            }
            network.reset();
            //THREE
            //WTA
            NeighborhoodFunction neighborhoodFunction = new NeighborhoodSingle();
            final CompetitiveTraining competitiveTraining = new CompetitiveTraining(network,0.01,  dataSet, neighborhoodFunction);
            do {
                backpropagation.iteration();
                System.out.println("Epoch #" + epoch + " Error:" + backpropagation.getError());
                epoch++;
            } while (backpropagation.getError() > 0.01);
            backpropagation.finishTraining();

            // test the neural network
            System.out.println("Neural Network Results:");
            for (NeuralDataPair pair : dataSet) {
                final NeuralData output = network.compute(pair.getInput());
                System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                        + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
            }


            Encog.getInstance().shutdown();*/
        }
}
