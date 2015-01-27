package com.wmh;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.dialog.Dialogs;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;

import javax.imageio.ImageIO;
import java.io.File;
import java.net.URL;
import java.util.*;


/**
 * Created by Marta on 2014-12-09.
 */
public class MainWindowController extends VBox implements Initializable{

   Stage stage;
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
   @FXML Tab errorTab;

   private List<Row> rowList = new ArrayList<>();
   private NeuralDataSet dataSet;
   private BasicNetwork network = new BasicNetwork();
   private int maxIteration;
   private int iterationNr;
   private double ratio;
   private double moment;
   private double error;
   private int[] neuronNumber=new int[]{1};
   private Integer actualDimension=0;
   private double XOR_STATIC_TEST[][];
   private double XOR_STATIC_IDEAL[][];
   private Task currentTask;
   private XYChart.Series errorSeries = new XYChart.Series();
   private List<XYChart.Data> errorData = new LinkedList<>();
   private int numberOfRowtoTest =1;
   private int numberOfLayers;
   private ContextMenu contextMenu = new ContextMenu();
   private int space = 100;

   public MainWindowController(){}
   public MainWindowController(Stage stage){
      this.stage = stage;
   }
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      errorChart.setCreateSymbols(false);
      dimension.setItems(FXCollections.observableArrayList(Arrays.asList(2,3,4,5,6,7,8,9)));
      layers.setItems(FXCollections.observableArrayList(Arrays.asList(1,2,3,4)));
      dimension.getSelectionModel().select(0);
      layers.getSelectionModel().select(0);
      testData.editableProperty().setValue(true);
      testData.getSelectionModel().cellSelectionEnabledProperty().setValue(true);
      MenuItem saveFile = new MenuItem("Save chart");
      contextMenu.getItems().add(saveFile);

      //Start button action
      bStart.setOnAction(event -> {
         resetParameters();
         setParameters();
         createTrainSet();
         createNetwork();
         generateTestSet();
         drawStructure();
         trainAndDrawErrorChart();
         Encog.getInstance().shutdown();
      });

      bStop.setOnAction(event -> {
         if (currentTask != null) currentTask.cancel();
      });

      addRow.setOnAction(event->{
         addRowToTestSet();
      });

      generateData.setOnAction(event ->{
         generateTestSet();
      });

      startTest.setOnAction(event -> {
         testNetwork();
      });

      saveFile.setOnAction(ev->{
         saveChart();
      });
      errorTab.setContextMenu(contextMenu);

      errorChart.setOnContextMenuRequested(e->{
         contextMenu.show(errorChart, e.getScreenX(), e.getScreenY());
      });

      errorChart.getXAxis().setLabel("Iteration number");
      errorChart.getYAxis().setLabel("Error value");
      errorChart.setLegendVisible(false);

      pane.setOnContextMenuRequested(ev -> {
         ContextMenu cmenu = new ContextMenu();
         MenuItem mItem = new MenuItem("Save structure");
         cmenu.getItems().add(mItem);
         mItem.setOnAction(e -> {
            saveStructure();
         });
         cmenu.show(pane, ev.getScreenX(), ev.getScreenY());
      });
   }

   /**
    * row is a fragment of table view of train set
    */
   public class Row {
      List<Integer> row = new ArrayList<>();
   }

   /**
    * Drawing structure of neural network
    */
   private void drawStructure(){
      Circle circle;
      Text text;
      Text weightText;
      Line line;

      // input neurons
      for(int j=0; j< actualDimension; j++) {

         double x = pane.getLayoutX() + 20+j*space;
         double y = pane.getLayoutY() + 20;

         // drawing input to first layer links
         for(int i=0; i<neuronNumber[0]; i++)
         {
            double endX = pane.getLayoutX()+i*space+20;
            double endY = pane.getLayoutY()+20+space;

            // weight text
//            double weight = Math.round(network.getWeight(0, j, i));
//            weightText = new Text(Double.toString(weight));
//            weightText.setX(x + (endX - x)*0.3 );
//            weightText.setY(y + (endY - y)*0.3 );
//            weightText.setFill(Color.DEEPPINK);

            line = new Line(x, y, endX, endY);
            line.strokeProperty().setValue(Paint.valueOf("black"));
            pane.getChildren().add(line);
//            pane.getChildren().add(weightText);
         }

         circle = new Circle(x, y, 15);
         text = new Text("I:"+j);
         text.setY(pane.getLayoutY()+23);
         text.setX(pane.getLayoutX()+13+j*space);
         circle.fillProperty().setValue(Paint.valueOf("white"));
         circle.strokeProperty().setValue(Paint.valueOf("black"));
         pane.getChildren().add(circle);
         pane.getChildren().add(text);
      }

      // hidden layer neurons
      for(int i=0; i< numberOfLayers; i++) {
         for(int j=0; j<neuronNumber[i]; j++) {
            double X = pane.getLayoutX()+j*space+20;
            double Y = pane.getLayoutY()+i*space+20+space;

            if(neuronNumber.length > i+1) {
               // links to next layer neurons
               for (int k = 0; k < neuronNumber[i + 1]; k++) {
                  double endX = pane.getLayoutX()+k*space+20;
                  double endY = pane.getLayoutY()+(i+1)*space+20+space;

                  line = new Line(X, Y, endX, endY);
                  line.strokeProperty().setValue(Paint.valueOf("black"));
                  pane.getChildren().add(line);

                  //System.out.println("Pobieram wagę dla: " + i + " " + j + " " + k);
//                  double weight = Math.round(network.getWeight(i, j, k));
//                  weightText = new Text(Double.toString(weight));
//                  weightText.setX(X + (endX - X)*0.3 );
//                  weightText.setY(Y + (endY - Y)*0.3 );
//                  weightText.setFill(Color.DEEPPINK);
//
//                  pane.getChildren().add(weightText);
               }
            }
            else
            {
               double endX = pane.getLayoutX() + 20;
               double endY = pane.getLayoutY() + numberOfLayers *space+20+space;

               line = new Line(X, Y, endX, endY);
               line.strokeProperty().setValue(Paint.valueOf("black"));
               pane.getChildren().add(line);
            }

            circle = new Circle(X, Y, 15);
            circle.fillProperty().setValue(Paint.valueOf("white"));
            circle.strokeProperty().setValue(Paint.valueOf("black"));
            text = new Text(i+":"+j);
            text.setX(X-7);
            text.setY(Y+3);
            pane.getChildren().add(circle);
            pane.getChildren().add(text);
         }
      }

      // output neuron
      circle = new Circle(pane.getLayoutX() + 20, pane.getLayoutY() + numberOfLayers *space+20+space, 15);
      text = new Text("O");
      text.setY(pane.getLayoutY() + numberOfLayers *space+23+space);
      text.setX(pane.getLayoutX()+14);
      circle.fillProperty().setValue(Paint.valueOf("white"));
      circle.strokeProperty().setValue(Paint.valueOf("black"));
      pane.getChildren().add(circle);
      pane.getChildren().add(text);
   }

   /**
    * Set actual parameters for neural network
    */
   private void setParameters(){
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
         Dialogs.create().owner(stage).title("Error was occured").masthead("Wrong parameter").message("One of the parameters has a wrong value!\n"+ne.getMessage()).showError();
         return;
      }
   }

   /**
    * Create neural network - layers and neurons
    */
   private void createNetwork(){
      //input layer
      network.addLayer(new BasicLayer(null, true, actualDimension));
      //change for loop and get numbers of neuron from textfield
      numberOfLayers = (Integer)layers.getSelectionModel().getSelectedItem();

      if(neuronNumber.length < numberOfLayers){
         Dialogs.create().owner(stage).title("Error was occured").masthead("Wrong parameters")
                 .message("Please enter valid number of neuron for each of hidden layer\n"+
                         "For example: 1,2,1 for 3 hidden layer").showError();
         return;
      }

      for(int i=0; i< numberOfLayers; i++){
         network.addLayer(new BasicLayer(new ActivationSigmoid(), true, neuronNumber[i]));
      }

      //output layer
      network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
      network.getStructure().finalizeStructure();
      network.reset();
   }

   /**
    * Generate train set
    */
   private void createTrainSet(){
      int trainSetLength = (int) Math.pow(2, (Integer) dimension.getSelectionModel().getSelectedItem());
      actualDimension = (Integer)dimension.getSelectionModel().getSelectedItem();
      double XOR_STATIC[][] = new double[trainSetLength*2][actualDimension];
      double XOR_STATIC_IDEAL[][] = new double[trainSetLength*2][1];

      //generate XOR table
      for(int i=0; i < trainSetLength*2; i++){
         int xor=0;
         for (int j=0; j < actualDimension; j++){
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
         System.out.println("CHECk" + Arrays.toString(XOR_STATIC_IDEAL[i]) + " " + Arrays.toString(XOR_STATIC[i]));
      }

      dataSet = new BasicNeuralDataSet(XOR_STATIC, XOR_STATIC_IDEAL);
   }

   /**
    * Train and draw learning error chart
    */
   private void trainAndDrawErrorChart(){
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

      currentTask.setOnSucceeded(ev -> {
         progressBar.progressProperty().unbind();
         progressBar.progressProperty().setValue(0.0);
         labelProgress.setText("Network trained.");
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
   }

   /**
    * reset neural netwok parameters
    */
   private void resetParameters(){
      //clearing variables
      actualDimension=0;
      XOR_STATIC_TEST = new double[][]{};
      testData.getColumns().clear();
      rowList.clear();
      pane.getChildren().clear();
      errorData.clear();
      network = new BasicNetwork();
      errorChart.getData().clear();
      errorSeries.getData().clear();
      textFlow.getChildren().clear();
      iterationNr=0;
   }

   private void isTrained(){
      if(actualDimension.equals(0)){
         Dialogs.create().owner(stage).title("Error was occured").masthead("Network not trained")
                 .message("Please train network first!").showError();
         return;
      }
   }

   /**
    * Add row to test set
    */
   private void addRowToTestSet(){
     isTrained();
      numberOfRowtoTest++;
      Row newRow = new Row();
      //we must enlarge static table XOR_STATIC_TEST
      double[][] XOR_STATIC_TEST_copy = XOR_STATIC_TEST;
      XOR_STATIC_TEST = new double[numberOfRowtoTest][actualDimension];
      for(int i=0; i< numberOfRowtoTest -1; i++){
         for(int j=0; j< actualDimension; j++){
            XOR_STATIC_TEST[i][j] = XOR_STATIC_TEST_copy[i][j];
         }
      }
      for(int j=0; j< actualDimension; j++) {
         newRow.row.add(0);
         XOR_STATIC_TEST[numberOfRowtoTest -1][j]=newRow.row.get(j);
      }
      rowList.add(newRow);
      Platform.runLater(() -> testData.setItems(FXCollections.observableArrayList(rowList)));
      testData.autosize();
   }

   /**
    * Generate test set
    */
   private void generateTestSet(){
      isTrained();
      rowList.clear();
      testData.getColumns().clear();

      numberOfRowtoTest = (int) Math.pow(2, actualDimension);
      XOR_STATIC_TEST = new double[numberOfRowtoTest][actualDimension];
      XOR_STATIC_IDEAL = new double[numberOfRowtoTest][1];

      for(int i=0; i < numberOfRowtoTest; i++){

         int xorSum=0;
         Row actRow = new Row();
         for (int j = actualDimension-1; j >= 0; j--){
            int bit = ((i & (1 << j)) != 0) ? 1 : 0;
            xorSum += bit;
            XOR_STATIC_TEST[i][(actualDimension-1)-j] = bit;
            actRow.row.add(bit);
         }

         XOR_STATIC_IDEAL[i][0] = (xorSum % 2) == 1 ? 1 : 0;
         rowList.add(actRow);
      }

      createTable();
      testData.setItems(FXCollections.observableArrayList(rowList));
      testData.autosize();
   }

   /**
    * Test neural network for actual test set
    */
   private void testNetwork(){
      //network.reset();
      //network.clearContext();
      isTrained();
      textFlow.getChildren().clear();
      List<String> strings = new ArrayList<String>();
      int correctNo = 0;
      for(int i=0; i< XOR_STATIC_TEST.length; i++) {
         final int finalI = i;
//         Platform.runLater(()->{
            //NeuralData neuralData = network.compute(new BasicNeuralData(XOR_STATIC_TEST[finalI]));
            MLData mlData = network.compute(new BasicNeuralData(XOR_STATIC_TEST[finalI]));
            double output = mlData.getData(0);
            boolean correct = ((int)(output+0.5)) == XOR_STATIC_IDEAL[finalI][0];

            if(correct)
               correctNo++;

            String text = "Input: "+Arrays.toString(XOR_STATIC_TEST[finalI])+"\t Output: "+output+ "\t OutputRounded: " + (int)(output+0.5) + "\t Ideal: " + XOR_STATIC_IDEAL[finalI][0] + " -> " + (correct ? "OK" : "WRONG") +"\n";
            strings.add(text);
            //textFlow.getChildren().add(new Text("Input: "+Arrays.toString(XOR_STATIC_TEST[finalI])+"\t Output: "+output+ "\t OutputRounded: " + (int)(output+0.5) + "\t Ideal: " + XOR_STATIC_IDEAL[finalI][0] + " -> " + (correct ? "OK" : "WRONG") +"\n"));
//         });
      }

      textFlow.getChildren().add(new Text("Overall quality: " + (((double)correctNo*100)/(double)XOR_STATIC_TEST.length) + ", ErrorNo: " + (XOR_STATIC_TEST.length-correctNo) + "\n"));
      for(String s : strings)
      {
         textFlow.getChildren().add(new Text(s));
      }
   }

   /**
    *
    */
   protected void createTable(){
      for (int j=0; j < actualDimension; j++){
         TableColumn<Row, String> col = new TableColumn<>(j+"");
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
   }

   /**
    * Saving chart as png file
    */
   protected void saveChart(){
      final FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialFileName("wykres1.png");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image","png"));
      File file = fileChooser.showSaveDialog(stage);
      Task task = new Task<Void>() {
         @Override
         public Void call() {

            Platform.runLater(
                    new Runnable() {
                       public void run() {
                          try {
                             WritableImage wim = new WritableImage( (int)errorChart.getWidth(), (int)errorChart.getHeight());
                             errorChart.snapshot(new SnapshotParameters(), wim);
                             ImageIO.write(SwingFXUtils.fromFXImage(wim, null), "png", file);
                          } catch (Exception s) {
                          }
                       }
                    });

            return null;
         }
      };

      Thread th = new Thread(task);
      th.start();
   }

   /**
    * Saving chart as png file
    */
   protected void saveStructure(){
      final FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialFileName("structure1.png");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image","png"));
      File file = fileChooser.showSaveDialog(stage);
      Task task = new Task<Void>() {
         @Override
         public Void call() {

            Platform.runLater(
                    new Runnable() {
                       public void run() {
                          try {
                             WritableImage wim = new WritableImage( (int)pane.getWidth(), (int)pane.getHeight());
                            pane.snapshot(new SnapshotParameters(), wim);
                             ImageIO.write(SwingFXUtils.fromFXImage(wim, null), "png", file);
                          } catch (Exception s) {
                          }
                       }
                    });

            return null;
         }
      };

      Thread th = new Thread(task);
      th.start();
   }
}
