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
import org.encog.neural.networks.training.competitive.CompetitiveTraining;
import org.encog.neural.networks.training.competitive.neighborhood.NeighborhoodFunction;
import org.encog.neural.networks.training.competitive.neighborhood.NeighborhoodSingle;
import org.encog.neural.networks.training.hebbian.HebbianTraining;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import sun.java2d.loops.XORComposite;

import java.net.URL;
import java.util.*;


/**
 * Created by Marta on 2014-12-09.
 */
public class MainWindowController extends VBox implements Initializable{

   @FXML ChoiceBox functions;
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

   private Task currentTask;
   private ObservableList<XYChart.Series> chartData = FXCollections.emptyObservableList();
   private ArrayList<XYChart.Series> errorSeries = new ArrayList<>();
   private ObservableList<XYChart.Data> observableErrorData = FXCollections.emptyObservableList();
   private ArrayList<XYChart.Data> errorData = new ArrayList<>();

   public MainWindowController(){}

   @Override
   public void initialize(URL location, ResourceBundle resources) {
      //chartProgress.
      chartData = FXCollections.observableArrayList(errorSeries);
      Bindings.bindContent(errorSeries, chartData);
      Bindings.bindContent(errorData, observableErrorData);
      errorChart.setCreateSymbols(false);
      BasicNetwork network = new BasicNetwork();
      functions.setItems(FXCollections.observableArrayList(Arrays.asList("BackPropagation", "HebbianTraining", "CompetitiveTraining")));
      dimension.setItems(FXCollections.observableArrayList(Arrays.asList(2,3,4,5,6,7,8,9)));
      layers.setItems(FXCollections.observableArrayList(Arrays.asList(1,2,3,4)));
      functions.getSelectionModel().select(0); //default value
      dimension.getSelectionModel().select(0);
      layers.getSelectionModel().select(0);
      bStart.setOnAction(event -> {
         network.clearContext();
         errorChart.getData().clear();
         //TODO
         //dynamic creating of data set for other dimensions
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
         network.addLayer(new BasicLayer(null, true, (Integer)dimension.getSelectionModel().getSelectedItem()));
         network.addLayer(new BasicLayer(new ActivationSigmoid(), true, (Integer)layers.getSelectionModel().getSelectedItem()));
         network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
         network.getStructure().finalizeStructure();
         network.reset();

         final int[] epoch = {1};
         if (functions.getSelectionModel().getSelectedItem().equals("BackPropagation")) {
            //back propagation
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
                                errorData.add(new XYChart.Data<>(epoch[0],backpropagation.getError()));
                                //System.out.println("Epoch #" + epoch[0] + " Error:" + backpropagation.getError());
                                epoch[0]++;
                             }
                          } while ((backpropagation.getError() > 0.01)&&epoch[0]<100000);
                          backpropagation.finishTraining();
                          return null;
                       }
                    });
            thread.setDaemon(true);
            thread.start();
         } else if (functions.getSelectionModel().getSelectedItem().equals("HebbianTraining")) {
            //hebbian training
            /**
             * It's something wrong with hebbianTraining - the error is always 0.00 in 1 iteration which isn't true at all
             */
            network.reset();
            final HebbianTraining hebbianTraining = new HebbianTraining(network, dataSet, false, 3.00);
            textFlow.getChildren().clear();
            Thread thread = new Thread(
                    currentTask = new Task() {
                       @Override
                       protected Object call() throws Exception {
                          do {
                             hebbianTraining.iteration();
                             System.out.println("Epoch #" + epoch[0] + " Error:" + hebbianTraining.getError());
                             errorData.add(new XYChart.Data<>(epoch[0],hebbianTraining.getError()));
                             epoch[0]++;
                          } while (hebbianTraining.getError() > 0.01);
                          hebbianTraining.finishTraining();
                          return null;
                       }
                    });
            thread.setDaemon(true);
            thread.start();
         } else if (functions.getSelectionModel().getSelectedItem().equals("CompetitiveTraining")) {
            //WTA
            NeighborhoodFunction neighborhoodFunction = new NeighborhoodSingle();
            network.reset();
            final CompetitiveTraining competitiveTraining = new CompetitiveTraining(network, 1.0, dataSet, neighborhoodFunction);
            textFlow.getChildren().clear();
            Thread thread = new Thread(
                    currentTask = new Task() {
                       @Override
                       protected Object call() throws Exception {
                          do {
                             competitiveTraining.iteration();
                             System.out.println("Epoch #" + epoch[0] + " Error:" + competitiveTraining.getError());
                             errorData.add(new XYChart.Data<>(epoch[0], competitiveTraining.getError()));
                             epoch[0]++;
                          } while (competitiveTraining.getError() > 0.01);
                          competitiveTraining.finishTraining();
                          return null;
                       }
                    });
            thread.setDaemon(true);
            thread.start();
         }

         final NeuralDataSet finalDataSet = dataSet;
         currentTask.setOnSucceeded(ev -> {
            progressBar.progressProperty().unbind();
            labelProgress.setText("Network trained.");
            // test the neural network
            for (NeuralDataPair pair : finalDataSet) {
               final NeuralData output = network.compute(pair.getInput());
               Text result = new Text("\t" + pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                       + ",\n  actual=" + output.getData(0) + ",\t ideal=" + pair.getIdeal().getData(0));
               textFlow.getChildren().add(result);
            }
            Task task;
            Thread thread1 = new Thread(
                    task = new Task<Object>() {
                       @Override
                       protected Object call() throws Exception {
                          errorSeries.add(new XYChart.Series("Error", FXCollections.observableArrayList(errorData)));
                          return null;
                       }
                    });
            task.setOnSucceeded(eve -> {
               errorChart.setData(FXCollections.observableArrayList(errorSeries));
               labelProgress.setText("Nothing in progress");
               progressBar.progressProperty().unbind();
               progressBar.progressProperty().setValue(0.0);
            });
            task.setOnRunning(e -> {
               //TODO
               //sth kind of progress bar or drawing chart in real time
               progressBar.progressProperty().bind(task.progressProperty());
               labelProgress.setText("Generating chart ...");
            });

            thread1.setDaemon(true);
            thread1.start();
            network.reset();
         });
         currentTask.setOnRunning(e->{
            progressBar.progressProperty().bind(currentTask.progressProperty());
            labelProgress.setText("Generating network and training...");
         });

         Encog.getInstance().shutdown();
      });

      bStop.setOnAction(event -> {
         if (currentTask != null) currentTask.cancel();
      });
   }
}
