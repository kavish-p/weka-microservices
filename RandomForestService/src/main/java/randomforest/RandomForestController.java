package randomforest;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ConfusionMatrix;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.FastVector;
import weka.core.Instances;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

@RestController
public class RandomForestController {
    
    @RequestMapping("/test")
    public String test() 
    {
        return "random forest service is up!";
    }
    
    @RequestMapping("/name")
    public String name() 
    {
        return "Random Forest";
    }
    
    @RequestMapping("/execute")
    public String execute(@RequestParam("datasetname") String datasetName) 
    {
		String url = "http://localhost:9900/dataset?datasetname="+datasetName;
		RestTemplate restTemplate = new RestTemplate();
        String stream = restTemplate.getForObject(url, String.class);
		String accuracy = randomForest(stream);
        return "Random Forest -> "+accuracy;
    }
    
    public String randomForest(String stream)
    {
    	double accuracy = 0.0;
    	String formattedAccuracy = "null";
    	try
    	{
    		Reader streamReader = new StringReader(stream);
    		BufferedReader dataStream = new BufferedReader(streamReader);
        	Instances data = new Instances(dataStream);
        	
        	data.setClassIndex(data.numAttributes() - 1);
            
        	// Choose a type of validation split
            Instances[][] split = crossValidationSplit(data, 10);
            
            // Separate split into training and testing arrays
            Instances[] trainingSplits = split[0];
            Instances[] testingSplits  = split[1];
            
            // Choose classifier
            Classifier model = new RandomForest();
            
            // Run classifier model
            // Collect every group of predictions for current model in a FastVector
            FastVector predictions = new FastVector();
            
            // For each training-testing split pair, train and test the classifier
            for(int i = 0; i < trainingSplits.length; i++) 
            {
                Evaluation validation = simpleClassify(model, trainingSplits[i], testingSplits[i]);
                predictions.appendElements(validation.predictions());
                
//                ThresholdCurve tc = new ThresholdCurve();
//                ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
//                int classIndex = 0;
//                Instances result = tc.getCurve(validation.predictions(), classIndex);
//                
//                vmc.setName(result.relationName());
//                PlotData2D tempd = new PlotData2D(result);
//                tempd.setPlotName(result.relationName());
//                tempd.addInstanceNumberAttribute();
//                // specify which points are connected
//                boolean[] cp = new boolean[result.numInstances()];
//                for (int n = 1; n < cp.length; n++)
//                  cp[n] = true;
//                tempd.setConnectPoints(cp);
//                // add plot
//                vmc.addPlot(tempd);
//
//                // display curve
//                String plotName = vmc.getName(); 
//                final javax.swing.JFrame jf = 
//                  new javax.swing.JFrame("Weka Classifier Visualize: "+plotName);
//                jf.setSize(500,400);
//                jf.getContentPane().setLayout(new BorderLayout());
//                jf.getContentPane().add(vmc, BorderLayout.CENTER);
//                jf.addWindowListener(new java.awt.event.WindowAdapter() {
//                  public void windowClosing(java.awt.event.WindowEvent e) {
//                  jf.dispose();
//                  }
//                });
//                jf.setVisible(true);
                    
            }
            HashSet<String> names = new HashSet();
            
            int num_classes = split[0][0].numClasses();
            for(int i=0; i< num_classes; i++)
            {
            	names.add(split[0][0].classAttribute().value(i));
            }
            String[] test = names.toArray(new String[num_classes]);
            
            ConfusionMatrix cm = new ConfusionMatrix(test);
            cm.addPredictions(predictions);
                
            // Calculate overall accuracy of current classifier on all splits
            
            accuracy = calculateAccuracy(predictions);
            formattedAccuracy = String.format("%.2f%%", accuracy);
            
            return formattedAccuracy + "\n" + cm;
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		return formattedAccuracy;
    	}
    }
    
    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) 
    {
        Instances[][] split = new Instances[2][numberOfFolds];
        
        for (int i = 0; i < numberOfFolds; i++) 
        {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }
        
        return split;
    }
    
    public static Evaluation simpleClassify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception 
    {
        Evaluation validation = new Evaluation(trainingSet);
        
        model.buildClassifier(trainingSet);
        validation.evaluateModel(model, testingSet);
        
        return validation;
    }
    
    public static double calculateAccuracy(FastVector predictions) 
    {
        double correct = 0;
        
        for (int i = 0; i < predictions.size(); i++) 
        {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual()) 
            {
                correct++;
            }
        }
        
        return 100 * correct / predictions.size();
    }
}
