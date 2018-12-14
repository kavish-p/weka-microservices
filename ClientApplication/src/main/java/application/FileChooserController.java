package application;

import java.io.File;
import java.nio.charset.Charset;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FileChooserController {
	
	static String dataHandlerService = "http://localhost:9900";
	static FXMLController mainController;
	
	@FXML private TextField name;
	@FXML private Button exitButton;
	@FXML private Button uploadButton;
	@FXML private TextArea fileLocation;
	@FXML private Label uploadLabel;
	
	@FXML
	public void uploadAction()
	{
		if(name.getText().isEmpty())
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.initStyle(StageStyle.UTILITY);
			alert.setTitle("Error");
			alert.setHeaderText("Dataset name not set!");
			alert.setContentText("Please enter the dataset name and try again.");
			alert.showAndWait();
		}
		else if(fileLocation.getText().isEmpty())
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.initStyle(StageStyle.UTILITY);
			alert.setTitle("Error");
			alert.setHeaderText("File location not set!");
			alert.setContentText("Please browse to the file location and try again.");
			alert.showAndWait();
		}
		else
		{
			try
			{
				System.out.println("Value is " + uploadButton);
				uploadButton.setDisable(true);
				uploadLabel.setVisible(true);
				new Thread(new Runnable() 
				{
			        @Override
			        public void run() 
			        {
			        	String url = dataHandlerService+"/save";
						RestTemplate restTemplate = new RestTemplate();
						restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
						UriComponentsBuilder builder = UriComponentsBuilder
							    .fromUriString(url)
							    // Add query parameter
							    .queryParam("location", fileLocation.getText())
							    .queryParam("name", name.getText());
						String response = restTemplate.getForObject(builder.toUriString(), String.class);
						mainController.updateDatasetList();
				        uploadButton.setDisable(false);
				        uploadLabel.setVisible(false);
			        }
			    }).start();
				
				
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@FXML
    private void browserFile(ActionEvent event)
    {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Select Dataset");
    	//File file = fileChooser.showOpenDialog(mainStage);
    	Button source = (Button) event.getSource();
    	File file = fileChooser.showOpenDialog((Stage) source.getScene().getWindow());
    	if(file != null)
    	{
    		fileLocation.setText(file.getAbsolutePath());
    	}
    }
	
	@FXML
    private void close(ActionEvent event)
    {
    	Stage stage = (Stage) exitButton.getScene().getWindow();
    	stage.close();
    }
	
	public void setMainController(FXMLController controller)
	{
		mainController = controller;
	}

}
