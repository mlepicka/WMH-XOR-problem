package com.wmh;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
         int epoch = 1;
         if(functions.selectionModelProperty().getValue().equals("BackPropagation")){
//back propagation
            final Backpropagation backpropagation = new Backpropagation(network, dataSet);

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
         }
         else if(functions.selectionModelProperty().getValue().equals("HebbianTraining")){
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
         }
         else {
            //THREE
            //WTA
            NeighborhoodFunction neighborhoodFunction = new NeighborhoodSingle();
            final CompetitiveTraining competitiveTraining = new CompetitiveTraining(network,0.01,  dataSet, neighborhoodFunction);
            do {
               competitiveTraining.iteration();
               System.out.println("Epoch #" + epoch + " Error:" + competitiveTraining.getError());
               epoch++;
            } while (competitiveTraining.getError() > 0.01);
            competitiveTraining.finishTraining();

            // test the neural network
            System.out.println("Neural Network Results:");
            for (NeuralDataPair pair : dataSet) {
               final NeuralData output = network.compute(pair.getInput());
               System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                       + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
            }
            network.reset();
         }
         Encog.getInstance().shutdown();
      });
   }
}
