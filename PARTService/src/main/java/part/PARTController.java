package part;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ConfusionMatrix;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instances;

@RestController
public class PARTController {
    
    @RequestMapping("/test")
    public String test() 
    {
        return "PART service is up!";
    }
    
    @RequestMapping("/name")
    public String name() 
    {
        return "PART";
    }
    
    @RequestMapping("/execute")
    public String execute(@RequestParam("datasetname") String datasetName) 
    {
		String url = "http://localhost:9900/dataset?datasetname="+datasetName;
		RestTemplate restTemplate = new RestTemplate();
		String stream = restTemplate.getForObject(url, String.class);
		String accuracy = part(stream);
        return "PART -> "+accuracy;
    }
    
    public String part(String stream)
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
            Classifier model = new PART();
            
            // Run classifier model
            // Collect every group of predictions for current model in a FastVector
            FastVector predictions = new FastVector();
            
            // For each training-testing split pair, train and test the classifier
            for(int i = 0; i < trainingSplits.length; i++) 
            {
                Evaluation validation = simpleClassify(model, trainingSplits[i], testingSplits[i]);
                predictions.appendElements(validation.predictions());
                
                // Uncomment to see the summary for each training-testing pair.
                //System.out.println(models[j].toString());
                    
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
