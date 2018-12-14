package application;

import java.io.File;

import org.springframework.web.client.RestTemplate;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ServiceStatusController {
	
	@FXML private Button exitButton;
	@FXML private ImageView img;
	@FXML private ImageView edge;
	@FXML private ImageView j48;
	@FXML private ImageView randomForest;
	@FXML private ImageView part;
	@FXML private ImageView dataHandler;
	@FXML private ImageView mongoDb;
	
	@FXML private Label label;
	
	static String[] services = {
			"http://localhost:9000", //edge
			"http://localhost:9100", //J48
			"http://localhost:9200", //Random Forest
			"http://localhost:9300", //PART
			"http://localhost:9900", //Data Handler
			"http://localhost:27017"  //mongo db
			};
	
	public void initialize() 
    {
		serviceCheck();
    }
	
	@FXML
    private void close(ActionEvent event)
    {
    	Stage stage = (Stage) exitButton.getScene().getWindow();
    	stage.close();
    }
	
	@FXML
    private void test(ActionEvent event)
    {
    	if(img.isVisible())
    		img.setVisible(false);
    	else
    		img.setVisible(true); 	
    }
	
	@FXML 
	private void serviceCheck()
	{
		toggleLabel();
		new Thread(new Runnable() 
		{
	        @Override
	        public void run() 
	        {
	    		boolean[] statuses = checkAll();
	    		changeStatus(edge,statuses[0]);
	    		changeStatus(j48,statuses[1]);
	    		changeStatus(randomForest,statuses[2]);
	    		changeStatus(part,statuses[3]);
	    		changeStatus(dataHandler,statuses[4]);
	    		changeStatus(mongoDb,statuses[5]);
	    		toggleLabel();
	        }
	    }).start();
		
	}
	
	public boolean[] checkAll()
	{
		boolean[] status = new boolean[6];
		for(int i=0; i<5; i++)
		{
			try
			{
				RestTemplate restTemplate = new RestTemplate();
		        String response = restTemplate.getForObject(services[i]+"/test", String.class);
		        status[i] = true;
			}
			catch(Exception e)
			{
				status[i] = false;
			}
		}
		
		try
		{
			MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
			MongoDatabase database = mongoClient.getDatabase("testdb");
	        status[5] = true;
		}
		catch(Exception e)
		{
			status[5] = false;
		}
		
		return status;
	}
	
	public void changeStatus(ImageView iv, boolean status)
	{
		if(status)
		{
			iv.setImage(new Image(getClass().getResourceAsStream("up.png")));
		}
		else
			iv.setImage(new Image(getClass().getResourceAsStream("down.png")));
	}
	
	public void toggleLabel() 
	{
		if(label.isVisible())
			label.setVisible(false);
		else
			label.setVisible(true);
	}

}
