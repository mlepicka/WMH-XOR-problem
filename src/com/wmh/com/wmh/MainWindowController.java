package com.wmh;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.competitive.CompetitiveTraining;
import org.encog.neural.networks.training.competitive.neighborhood.NeighborhoodFunction;
import org.encog.neural.networks.training.competitive.neighborhood.NeighborhoodSingle;
import org.encog.neural.networks.training.hebbian.HebbianTraining;
import org.encog.neural.networks.training.propagation.back.Backpropagation;

import java.net.URL;
import java.util.*;


/**
 * Created by Marta on 2014-12-09.
 */
public class MainWindowController extends VBox implements Initializable{

   @FXML ChoiceBox functions;
   @FXML TextField ratioValue; //radius or learning ratio
   @FXML Button bStart;
   @FXML Button bStop;
   @FXML TextFlow textFlow;
   /**
    * The input necessary for XOR.
    */
   public static double XOR_INPUT[][] = { { 0.0, 0.0 }, { 1.0, 0.0 },
           { 0.0, 1.0 }, { 1.0, 1.0 } };

   /**
    * The ideal data necessary for XOR.
    */
   public static double XOR_IDEAL[][] = { { 0.0 }, { 1.0 }, { 1.0 }, { 0.0 } };

   private ObservableList<String> functionList = FXCollections.emptyObservableList();
   private ArrayList<String> functionValueList = new ArrayList<String>();
   private Task currentTask;

   public MainWindowController(){}

   @Override
   public void initialize(URL location, ResourceBundle resources) {
      functionValueList.addAll(Arrays.asList("BackPropagation", "HebbianTraining", "CompetitiveTraining"));
      functionList = FXCollections.observableArrayList(functionValueList);
      functions.setItems(functionList);
      functions.getSelectionModel().select(0); //default value

      bStart.setOnAction(event->{
         BasicNetwork network = new BasicNetwork();
         network.addLayer(new BasicLayer(null, true, 2));
         network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 3));
         network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
         network.getStructure().finalizeStructure();
         network.reset();

         // create training data
         // MLDataSet trainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);
         NeuralDataSet dataSet = new BasicNeuralDataSet(XOR_INPUT, XOR_IDEAL);
         final int[] epoch = {1};
         if(functions.getSelectionModel().getSelectedItem().equals("BackPropagation")){
         //back propagation
            network.reset();
            final Backpropagation backpropagation = new Backpropagation(network, dataSet);
            textFlow.getChildren().clear();
            Thread thread = new Thread(
               currentTask=new Task() {
                  @Override
                  protected Object call ()throws Exception {
                     //ONE
                     do {
                        if (!this.isCancelled()) {
                           backpropagation.iteration();
                           System.out.println("Epoch #" + epoch[0] + " Error:" + backpropagation.getError());
                           epoch[0]++;
                        }
                     } while (backpropagation.getError() > 0.01);
                     backpropagation.finishTraining();
                     return null;
                  }
               });

            thread.setDaemon(true);
            thread.start();

            currentTask.setOnSucceeded(ev->{
               // test the neural network
               System.out.println("Neural Network Results:");
               for (NeuralDataPair pair : dataSet) {
                  final NeuralData output = network.compute(pair.getInput());
                  Text result = new Text("\t"+pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                          + ",\n  actual=" + output.getData(0) + ",\t ideal=" + pair.getIdeal().getData(0));
                  textFlow.getChildren().add(result);
               }
               network.reset();
            });
         }
         else if(functions.getSelectionModel().getSelectedItem().equals("HebbianTraining")){
            //TWO
            //hebbian training
            /**
             * It's something wrong with hebbianTraining - the error is always 0.00 in 1 iteration which isn't true at all
             */
            network.reset();
            final HebbianTraining hebbianTraining = new HebbianTraining(network, dataSet, false, 3.00);

            textFlow.getChildren().clear();
            Thread thread = new Thread(
                    currentTask=new Task() {
                       @Override
                       protected Object call ()throws Exception {
                           do {
                              hebbianTraining.iteration();
                              System.out.println("Epoch #" + epoch[0] + " Error:" + hebbianTraining.getError());
                              epoch[0]++;
                           } while (hebbianTraining.getError() > 0.01);
                           hebbianTraining.finishTraining();
                          return null;
                       }
                    });

            thread.setDaemon(true);
            thread.start();
            // test the neural network
            currentTask.setOnSucceeded(ev -> {
               System.out.println("Neural Network Results:");
               for (NeuralDataPair pair : dataSet) {
                  final NeuralData output = network.compute(pair.getInput());
                  Text result = new Text("\t" + pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                          + ",\n actual=" + output.getData(0) + ",\tideal=" + pair.getIdeal().getData(0));
                  textFlow.getChildren().add(result);
               }
               network.reset();
            });
         }
         else if(functions.getSelectionModel().getSelectedItem().equals("CompetitiveTraining")) {
            //THREE
            //WTA
            NeighborhoodFunction neighborhoodFunction = new NeighborhoodSingle();
            network.reset();
            final CompetitiveTraining competitiveTraining = new CompetitiveTraining(network, 1.0,  dataSet, neighborhoodFunction);
            textFlow.getChildren().clear();
            Thread thread = new Thread(
                    currentTask=new Task() {
                       @Override
                       protected Object call ()throws Exception {
                           do {
                              competitiveTraining.iteration();
                              System.out.println("Epoch #" + epoch[0] + " Error:" + competitiveTraining.getError());
                              epoch[0]++;
                           } while (competitiveTraining.getError() > 0.01);
                           competitiveTraining.finishTraining();
                          return null;
                       }
                    });

            thread.setDaemon(true);
            thread.start();
            // test the neural network
            currentTask.setOnSucceeded(ev -> {
               System.out.println("Neural Network Results:");
               for (NeuralDataPair pair : dataSet) {
                  final NeuralData output = network.compute(pair.getInput());
                  Text result = new Text("\t" + pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                          + ",\n actual=" + output.getData(0) + ",\tideal=" + pair.getIdeal().getData(0));
                  textFlow.getChildren().add(result);
               }
               network.reset();
            });
         }
         Encog.getInstance().shutdown();
      });

      bStop.setOnAction(event->{if(currentTask!=null)currentTask.cancel();});
   }
}
