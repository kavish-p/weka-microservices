package application;

import java.io.File;
import java.util.ArrayList;
import java.util.Currency;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.client.RestTemplate;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FXMLController 
{
	static String dataHandlerService = "http://localhost:9900";
	static String edgeService = "http://localhost:9000";
	static Stage textViewerStage = new Stage();
	static TextViewerController textViewController;
	
	static Stage serviceStatusStage = new Stage();
	static ServiceStatusController serviceStatusController;
	
	static Stage fileChooserStage = new Stage();
	static FileChooserController fileChooserController;
	
	static Stage mainStage;
	
	@FXML private Button executeButton;
	@FXML private Button exitButton;
	@FXML private Label executeLabel;
	@FXML private TextArea resultsArea;
	@FXML private TableView<DatasetTableEntry> tableView;
	@FXML private TableColumn<DatasetTableEntry, String> name;
    @FXML private TableColumn<DatasetTableEntry, String> uploadDate;
	
    public void initialize() 
    {
        System.out.println("Controller is up!");
        
        Parent root = null;
        Parent ssRoot = null;
        Parent fcRoot = null;
        FXMLLoader loader = null;
        FXMLLoader ssLoader = null;
        FXMLLoader fcLoader = null;
    	try
    	{
    		
    		
    		loader = new FXMLLoader(getClass().getClassLoader().getResource("application/text-viewer.fxml"));
    		root = loader.load();
    		Scene scene = new Scene(root);
    		textViewerStage.setScene(scene);
    		textViewerStage.getIcons().add(new Image(getClass().getResourceAsStream("logo.png")));
    		textViewerStage.initModality(Modality.APPLICATION_MODAL);
    		textViewerStage.setResizable(false);
    		textViewController = loader.getController();
    		
    		ssLoader = new FXMLLoader(getClass().getClassLoader().getResource("application/serviceStatus.fxml"));
    		ssRoot = ssLoader.load();
    		Scene ssScene = new Scene(ssRoot);
    		serviceStatusStage.setScene(ssScene);
    		serviceStatusStage.getIcons().add(new Image(getClass().getResourceAsStream("logo.png")));
    		serviceStatusStage.initModality(Modality.APPLICATION_MODAL);
    		serviceStatusStage.setResizable(false);
    		serviceStatusController = ssLoader.getController();
    		
    		fcLoader = new FXMLLoader(getClass().getClassLoader().getResource("application/file-chooser.fxml"));
    		fcRoot = fcLoader.load();
    		Scene fcScene = new Scene(fcRoot);
    		fileChooserStage.setScene(fcScene);
    		fileChooserStage.getIcons().add(new Image(getClass().getResourceAsStream("logo.png")));
    		fileChooserStage.initModality(Modality.APPLICATION_MODAL);
    		fileChooserStage.setResizable(false);
    		fileChooserController = fcLoader.getController();
    		fileChooserController.setMainController(this);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
        
        name.setCellValueFactory(new PropertyValueFactory<DatasetTableEntry, String>("name"));
        uploadDate.setCellValueFactory(new PropertyValueFactory<DatasetTableEntry, String>("uploadDate"));
        updateDatasetList();
        
        tableView.setRowFactory( tv -> {
            TableRow<DatasetTableEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) 
                {
                	DatasetTableEntry rowData = row.getItem();
                	textViewController.setText(getDataStream(rowData.getName()));
                    viewDataset(rowData.getName());
                }
            });
            return row ;
        });
    }
    
    @FXML
    private void test(ActionEvent event)
    {
        System.exit(0);
    }
    
    public void updateDatasetList()
    {
        tableView.setItems(getDatasetList());
    }
    
    private void viewDataset(String title)
    {
    	textViewerStage.setTitle(title);
    	textViewerStage.showAndWait();
    }
    
    @FXML
    private void viewServiceStatus(ActionEvent event)
    {
    	serviceStatusStage.setTitle("Service Status Dashboard");
    	serviceStatusStage.showAndWait();
    }
    
    @FXML
    private void viewFileChooser(ActionEvent event)
    {
    	fileChooserStage.setTitle("Select Dataset");
    	fileChooserStage.showAndWait();
    }
    
    private ObservableList<DatasetTableEntry> getDatasetList() 
    {
    	ArrayList<DatasetTableEntry> entries = new ArrayList<DatasetTableEntry>();
    	try
		{
			RestTemplate restTemplate = new RestTemplate();
	        String response = restTemplate.getForObject(dataHandlerService+"/list", String.class);
	        String[] split = response.split("\\|");
	        for(int i = 0; i<split.length; i+=2)
	        {
	        	entries.add(new DatasetTableEntry(split[i],split[i+1]));
	        }
	        
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
        ObservableList<DatasetTableEntry> list = FXCollections.observableArrayList(entries);
        return list;
    }
    
    private String getDataStream(String name)
    {
    	String response = "";
    	try
		{
			RestTemplate restTemplate = new RestTemplate();
	        response = restTemplate.getForObject(dataHandlerService+"/dataset?datasetname="+name, String.class);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    	return response;
    }
    
    @FXML
    private void getResults(ActionEvent event)
    {
    	DatasetTableEntry entry = tableView.getSelectionModel().getSelectedItem();
    	if(entry == null)
    	{
    		Alert alert = new Alert(AlertType.WARNING);
			alert.initStyle(StageStyle.UTILITY);
			alert.setTitle("Warning");
			alert.setHeaderText("No dataset selected !");
			alert.setContentText("Please select a dataset on the list.");
			alert.showAndWait();
    	}
    	else
    	{
			System.out.println("value is " + executeLabel);
			executeLabel.setVisible(true);
			executeButton.setDisable(true);
			new Thread(new Runnable() 
			{
		        @Override
		        public void run() 
		        {
		        	try
		        	{
    		        	RestTemplate restTemplate = new RestTemplate();
    	                String response = restTemplate.getForObject(edgeService+"/compute?datasetname="+entry.getName(), String.class);
    	                resultsArea.setText(response);
    	                executeLabel.setVisible(false);
    	                executeButton.setDisable(false);
		        	}
		        	catch(Exception e)
		        	{
		        		Platform.runLater(new Runnable()
		                {
		                    @Override
		                    public void run()
		                    {
		                    	Alert alert = new Alert(AlertType.ERROR);
				    			alert.initStyle(StageStyle.UTILITY);
				    			alert.setTitle("Error");
				    			alert.setHeaderText("The Edge Service is down!");
				    			alert.setContentText("Start the Edge Service and restart the client application");
				    			alert.showAndWait();
				    			executeLabel.setVisible(false);
		    	                executeButton.setDisable(false);
				    			e.printStackTrace();
		                    }
		                });
		        	}
		        }
		    }).start();
    	}
    }
    
    @FXML
    private void highlightTest(ActionEvent event)
    {
        String whole = resultsArea.getText();
        String pattern = "R(.+?)%";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(whole);
        if(m.find())
        	System.out.println(m.group(0));
        
        Text t = new Text();
        t.setText("This is a text sample");
    }

    public void setStage(Stage stage)
    {
    	mainStage = stage;
    }
}
