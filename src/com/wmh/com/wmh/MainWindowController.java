package com.wmh;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
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
import org.encog.neural.networks.structure.NeuralStructure;
import org.encog.neural.networks.training.competitive.CompetitiveTraining;
import org.encog.neural.networks.training.competitive.neighborhood.NeighborhoodFunction;
import org.encog.neural.networks.training.competitive.neighborhood.NeighborhoodSingle;
import org.encog.neural.networks.training.hebbian.HebbianTraining;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.util.simple.EncogUtility;
import sun.java2d.loops.XORComposite;

import java.net.URL;
import java.util.*;


/**
 * Created by Marta on 2014-12-09.
 */
public class MainWindowController extends VBox implements Initializable{

   @FXML ChoiceBox dimension;
   @FXML ChoiceBox layers;
   @FXML TextField neuronNumbers;
   @FXML TextField ratioValue; //radius or learning ratio
   @FXML Button bStart;
   @FXML Button bStop;
   @FXML TextFlow textFlow;
   @FXML LineChart errorChart;
   @FXML ProgressBar progressBar;
   @FXML Label labelProgress;
   int iterationNr = 0;


   private Task currentTask;
   private XYChart.Series errorSeries = new XYChart.Series();
   private List<XYChart.Data> errorData = new LinkedList<>();
   public MainWindowController(){}

   @Override
   public void initialize(URL location, ResourceBundle resources) {
      //chartProgress.

      errorChart.setCreateSymbols(false);
      BasicNetwork network = new BasicNetwork();
     // functions.setItems(FXCollections.observableArrayList(Arrays.asList("BackPropagation", "HebbianTraining", "CompetitiveTraining")));
      dimension.setItems(FXCollections.observableArrayList(Arrays.asList(2,3,4,5,6,7,8,9)));
      layers.setItems(FXCollections.observableArrayList(Arrays.asList(1,2,3,4)));
      //functions.getSelectionModel().select(0); //default value
      dimension.getSelectionModel().select(0);
      layers.getSelectionModel().select(0);
      bStart.setOnAction(event -> {

         errorData.clear();
         network.clearContext();
         errorChart.getData().clear();
         errorSeries.getData().clear();
         NeuralDataSet dataSet;

         int actualDimension = (int)Math.pow(2,(Integer)dimension.getSelectionModel().getSelectedItem());

         double XOR_STATIC[][] = new double[actualDimension][(Integer)dimension.getSelectionModel().getSelectedItem()];
         double XOR_STATIC_IDEAL[][] = new double[actualDimension][1];
         for(int i=0; i < actualDimension ; i++){
            int xor=0;
            for (int j=0; j < (Integer)dimension.getSelectionModel().getSelectedItem(); j++){
               Random random = new Random();
               Double value = random.nextDouble();
               if(value<0.5) value=0.0;
               else value = 1.0;
               XOR_STATIC[i][j] = value;
               if(j==0)xor=value.intValue();
               if(j!=0)
                  xor=xor^value.intValue();
            }
            XOR_STATIC_IDEAL[i][0] = xor;
         }

         dataSet = new BasicNeuralDataSet(XOR_STATIC, XOR_STATIC_IDEAL);
         //input layer
         network.addLayer(new BasicLayer(null, true, (Integer)dimension.getSelectionModel().getSelectedItem()));
         //change for loop and get numbers of neuron from textfield
         for(int i=0; i< (Integer)layers.getSelectionModel().getSelectedItem(); i++)
            network.addLayer(new BasicLayer(new ActivationSigmoid(), true, (Integer)layers.getSelectionModel().getSelectedItem()));
         //output layer
         network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
         network.getStructure().finalizeStructure();
         network.reset();

         errorSeries.setName("Error");
         errorChart.getData().add(errorSeries);


            network.reset();
            final Backpropagation backpropagation = new Backpropagation(network, dataSet);
            textFlow.getChildren().clear();
            Thread thread = new Thread(
                    currentTask = new Task() {
                       @Override
                       protected Object call() throws Exception{
                          do {
                             if (!this.isCancelled()) {
                                backpropagation.iteration();
                                Platform.runLater(()->{
                                   errorData.add(new XYChart.Data<>(iterationNr,backpropagation.getError()));
                                   errorSeries.getData().add(new XYChart.Data(iterationNr, backpropagation.getError()));
                                   iterationNr++;
                                });
                             }
                          } while ((backpropagation.getError() > 0.01)&&iterationNr<1000);
                          backpropagation.finishTraining();
                          return null;
                       }
                    });
         thread.setDaemon(true);
            thread.start();

         final NeuralDataSet finalDataSet = dataSet;
         currentTask.setOnSucceeded(ev -> {
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().setValue(0.0);
            labelProgress.setText("Network trained.");
            // test the neural network
            for (NeuralDataPair pair : finalDataSet) {
               final NeuralData output = network.compute(pair.getInput());
               double[] input = pair.getInput().getData();
               Text result = new Text( "Input array: "+Arrays.toString(input)
                       + ",\t  actual=" + output.getData(0) + ",\t ideal=" + pair.getIdeal().getData(0)+"\n");

               textFlow.getChildren().add(result);
            }
         });
         currentTask.setOnRunning(e -> {
            progressBar.progressProperty().bind(currentTask.progressProperty());
            labelProgress.setText("Generating network and training...");
         });
         currentTask.setOnCancelled(e->{
            labelProgress.setText("Nothing in progress");
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().setValue(0.0);
         });
         Encog.getInstance().shutdown();
      });

         bStop.setOnAction(event -> {
            if (currentTask != null) currentTask.cancel();
         });
   }
}
