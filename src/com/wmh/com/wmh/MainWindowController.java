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

import java.net.URL;
import java.util.*;


/**
 * Created by Marta on 2014-12-09.
 */
public class MainWindowController extends VBox implements Initializable{

   @FXML ChoiceBox functions;
   @FXML TextField ratioValue; //radius or learning ratio
   @FXML Button bStart;

   private ObservableList<String> functionList = FXCollections.emptyObservableList();
   private ArrayList<String> functionValueList = new ArrayList<String>();

   public MainWindowController(){}

   @Override
   public void initialize(URL location, ResourceBundle resources) {
      functionValueList.addAll(Arrays.asList("BackPropagation", "HebbianTraining", "CompetitiveTraining"));
      functionList = FXCollections.observableArrayList(functionValueList);
      functions.setItems(functionList);
   }
}
