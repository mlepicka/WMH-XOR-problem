package com.wmh;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.dialog.Dialogs;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.Network;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;

import java.net.URL;
import java.util.*;


/**
 * Created by Marta on 2014-12-09.
 */
public class MainWindowController extends VBox implements Initializable{

   @FXML ChoiceBox dimension;
   @FXML ChoiceBox layers;
   @FXML TextField neuronNumbers;
   @FXML TextField momentum;
   @FXML TextField errorValue;
   @FXML TextField ratioValue; //radius or learning ratio
   @FXML ScrollPane scrollpane;
   @FXML Button bStart;
   @FXML Button bStop;
   @FXML TextFlow textFlow;
   @FXML LineChart errorChart;
   @FXML ProgressBar progressBar;
   @FXML Label labelProgress;
   @FXML TextField maxIterations;
   @FXML Tab structure;
   @FXML Pane pane;
   @FXML TableView testData;
   @FXML Button generateData;
   @FXML Button addRow;
   @FXML Button startTest;

   private List<Row> tableList = new ArrayList<>();

   int maxIteration;
   int iterationNr;
   double ratio;
   double moment;
   double error;
   int[] neuronNumber=new int[]{1};
   int actualDimension;
   Integer actualDimensionChosen;
   double XOR_STATIC_TEST[][];
   private Task currentTask;
   private XYChart.Series errorSeries = new XYChart.Series();
   private List<XYChart.Data> errorData = new LinkedList<>();
   public MainWindowController(){}
   int rowNr=1;
   NeuralDataSet dataSet;

   @Override
   public void initialize(URL location, ResourceBundle resources) {
      errorChart.setCreateSymbols(false);
      BasicNetwork network = new BasicNetwork();
      dimension.setItems(FXCollections.observableArrayList(Arrays.asList(2,3,4,5,6,7,8,9)));
      layers.setItems(FXCollections.observableArrayList(Arrays.asList(1,2,3,4)));
      dimension.getSelectionModel().select(0);
      layers.getSelectionModel().select(0);
      testData.editableProperty().setValue(true);
      testData.getSelectionModel().cellSelectionEnabledProperty().setValue(true);

      //Start button action
      bStart.setOnAction(event -> {
         //clearing variables
         testData.getColumns().clear();
         tableList.clear();
         pane.getChildren().clear();
         errorData.clear();
         network.clearContext();
         errorChart.getData().clear();
         errorSeries.getData().clear();
         textFlow.getChildren().clear();

         iterationNr=0;

         try {
            maxIteration = Integer.parseInt(maxIterations.getText());
            ratio = Double.parseDouble(ratioValue.getText());
            moment = Double.parseDouble(momentum.getText());
            error = Double.parseDouble(errorValue.getText());
            if(neuronNumbers.getText()!=null && !neuronNumbers.getText().equals("")) {
               String[] neurons = neuronNumbers.getText().split(",");
               neuronNumber = new int[neurons.length];
               for (int i = 0; i < neurons.length; i++)
                  neuronNumber[i] = Integer.parseInt(neurons[i]);
            }
         }
         catch(NumberFormatException ne){
            Dialogs.create().owner(new Stage()).title("Error was occured").masthead("Wrong parameter").message("One of the parameters has a wrong value!\n"+ne.getMessage()).showError();
            return;
         }

         actualDimension = (int)Math.pow(2,(Integer)dimension.getSelectionModel().getSelectedItem());
         actualDimensionChosen = (Integer)dimension.getSelectionModel().getSelectedItem();
         double XOR_STATIC[][] = new double[actualDimension][actualDimensionChosen];
         double XOR_STATIC_IDEAL[][] = new double[actualDimension][1];

         //generate XOR table
         for(int i=0; i < actualDimension ; i++){
            int xor=0;
            for (int j=0; j < actualDimensionChosen; j++){
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
         network.addLayer(new BasicLayer(null, true, actualDimensionChosen));
         //change for loop and get numbers of neuron from textfield
         int size = (Integer)layers.getSelectionModel().getSelectedItem();

         if(neuronNumber.length < size){
            Dialogs.create().owner(new Stage()).title("Error was occured").masthead("Wrong parameters")
                    .message("Please enter valid number of neuron for each of hidden layer\n"+
                    "For example: 1,2,1 for 3 hidden layer").showError();
            return;
         }

         //draw structure of neural network
         for(int i=0; i< size; i++) {
            network.addLayer(new BasicLayer(new ActivationSigmoid(), true, neuronNumber[i]));
            for(int j=0; j<neuronNumber[i]; j++) {
               double X =pane.getLayoutX() + pane.getWidth()/(neuronNumber[i]+2)+j*50;
               double Y = pane.getLayoutY()+ pane.getHeight()/size+i*50+50;
               Circle circle = new Circle(X, Y, 10);
               circle.fillProperty().setValue(Paint.valueOf("white"));
               Text text = new Text(""+j);
               text.setX(X-3);
               text.setY(Y + 3);
               pane.getChildren().add(circle);
               pane.getChildren().add(text);
            }
         }

         //output layer
         network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
         network.getStructure().finalizeStructure();
         network.reset();

         errorSeries.setName("Error");
         errorChart.getData().add(errorSeries);

         //Back propagation learning
         final Backpropagation backpropagation = new Backpropagation(network, dataSet, ratio, moment);

         //Train and draw learning error chart
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
                       } while ((backpropagation.getError() > error)&&iterationNr<maxIteration);
                       backpropagation.finishTraining();
                       return null;
                    }
                 });
         thread.setDaemon(true);
         thread.start();

         //
         currentTask.setOnSucceeded(ev -> {
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().setValue(0.0);
            labelProgress.setText("Network trained.");

           /* //TODO
            // test the neural for inputs network - I'm thinking to delete this
            for (NeuralDataPair pair : dataSet) {
               final NeuralData output = network.compute(pair.getInput());
               double[] input = pair.getInput().getData();
               Text result = new Text( "Input array: "+Arrays.toString(input)
                       + ",\t  actual=" + output.getData(0) + ",\t ideal=" + pair.getIdeal().getData(0)+"\n");

               Platform.runLater(() -> textFlow.getChildren().add(result));
            }*/
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

      addRow.setOnAction(event->{
         rowNr++;
         Row newRow = new Row();

         //we must enlarge static table XOR_STATIC_TEST
         double[][] XOR_STATIC_TEST_copy = XOR_STATIC_TEST;
         XOR_STATIC_TEST = new double[rowNr][actualDimensionChosen];
         for(int i=0; i< rowNr-1; i++){
            for(int j=0; j< actualDimensionChosen; j++){
               XOR_STATIC_TEST[i][j] = XOR_STATIC_TEST_copy[i][j];
            }
         }
         for(int j=0; j< actualDimensionChosen; j++) {
            newRow.row.add(0);
            XOR_STATIC_TEST[rowNr-1][j]=newRow.row.get(j);
         }
         tableList.add(newRow);
         Platform.runLater(() -> testData.setItems(FXCollections.observableArrayList(tableList)));
         testData.autosize();
      });

      generateData.setOnAction(event ->{
         tableList.clear();
         testData.getColumns().clear();
         XOR_STATIC_TEST = new double[rowNr][actualDimensionChosen];
         double XOR_STATIC_IDEAL[][] = new double[rowNr][1];
         for(int i=0; i < rowNr ; i++){
            int xor=0;
            Row actRow = new Row();
            for (int j=0; j < actualDimensionChosen; j++){
               Random random = new Random();
               Double value = random.nextDouble();
               if(value<0.5) value=0.0;
               else value = 1.0;
               XOR_STATIC_TEST[i][j] = value;
               if(j==0)xor=value.intValue();
               if(j!=0)
                  xor=xor^value.intValue();
               actRow.row.add(value.intValue());
            }
            tableList.add(actRow);
            XOR_STATIC_IDEAL[i][0] = xor;
         }
         for (int j=0; j < actualDimensionChosen; j++){
            TableColumn<Row, String> col = new TableColumn(j+"");
            final int finalJ = j;
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Row,String>,ObservableValue<String>>(){
               @Override
               public ObservableValue<String> call(TableColumn.CellDataFeatures<Row, String> param) {
                  return new SimpleStringProperty(param.getValue().row.get(finalJ).toString());
               }
            });
            col.setCellFactory(TextFieldTableCell.<Row>forTableColumn());
            col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Row, String>>() {
               @Override
               public void handle(TableColumn.CellEditEvent<Row, String> event) {
                  XOR_STATIC_TEST[event.getTablePosition().getRow()][event.getTablePosition().getColumn()]=Integer.parseInt(event.getNewValue());
               }
            });

            testData.getColumns().addAll(col);
         }
         testData.setItems(FXCollections.observableArrayList(tableList));
         testData.autosize();
      });

      startTest.setOnAction(event -> {
         textFlow.getChildren().clear();
         for(int i=0; i< XOR_STATIC_TEST.length; i++) {
            NeuralData neuralData = network.compute(new BasicNeuralData(XOR_STATIC_TEST[i]));
           textFlow.getChildren().add(new Text("Input: "+Arrays.toString(XOR_STATIC_TEST[i])+"\t Output: "+neuralData.getData(0)+"\n"));
         }
      });
   }

   public class Row {
      List<Integer> row = new ArrayList<>();
   }
}
