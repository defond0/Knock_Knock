package com.example.knock_knock;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class Driver {
    public static void main(String[] args) throws Exception{
    	//Jeff -We have to determine what this is for our problem, this is a skeleton for a weka 
    	//driver for a classifier. Some of the classes in this example were deprecated, so I attempted
    	//to replace them with current classes (names I replaced fastVector with ArrayList<e> as per a
    	//a link)
    	
    	System.out.println("Contact");
    	
        // Declare two numeric attributes
        Attribute Attribute1 = new Attribute("firstNumeric");
        Attribute Attribute2 = new Attribute("secondNumeric");
         
        // Declare a nominal attribute along with its values
        ArrayList<String> fvNominalVal = new ArrayList<String>(3);
        fvNominalVal.add("blue");
        fvNominalVal.add("gray");
        fvNominalVal.add("black");
        Attribute Attribute3 = new Attribute("aNominal", fvNominalVal);
         
        // Declare the class attribute along with its values  //again this is just example code
        ArrayList<String> fvClassVal = new ArrayList<String>(2);
        fvClassVal.add("positive");
        fvClassVal.add("negative");
        Attribute ClassAttribute = new Attribute("theClass", fvClassVal);
         
        // Declare the feature vector 
        ArrayList fvWekaAttributes = new ArrayList(4);
        fvWekaAttributes.add(Attribute1);    
        fvWekaAttributes.add(Attribute2);    
        fvWekaAttributes.add(Attribute3);    
        fvWekaAttributes.add(ClassAttribute);
         
        // Create an empty training set //Jeff - I think we can use this for adding new sounds,
        //grabbing audio of an instance.
        Instances isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);       
         
        // Set class index
        isTrainingSet.setClassIndex(3);
        
        
        // Create the instance this is added to the training set
        weka.core.Instance iExample = new DenseInstance(4);
        iExample.setValue((Attribute)fvWekaAttributes.get(0), 1.0);      
        iExample.setValue((Attribute)fvWekaAttributes.get(1), 0.5);      
        iExample.setValue((Attribute)fvWekaAttributes.get(2), "gray");
        iExample.setValue((Attribute)fvWekaAttributes.get(3), "positive");
         
        // add the instance
        isTrainingSet.add(iExample);
        Classifier cModel = (Classifier)new NaiveBayes();   
        cModel.buildClassifier(isTrainingSet);

        // Test the model
        Evaluation eTest = new Evaluation(isTrainingSet);
        eTest.evaluateModel(cModel, isTrainingSet);
         
        // Print the result a la Weka explorer:
        String strSummary = eTest.toSummaryString();
        System.out.println(strSummary);
         
        // Get the confusion matrix
        double[][] cmMatrix = eTest.confusionMatrix();
        for(int row_i=0; row_i<cmMatrix.length; row_i++){
            for(int col_i=0; col_i<cmMatrix.length; col_i++){
                System.out.print(cmMatrix[row_i][col_i]);
                System.out.print("|");
            }
            System.out.println();
        }
   }
}
