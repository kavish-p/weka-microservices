package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TextViewerController 
{
	
	
	@FXML private Button exitButton;
	@FXML private TextArea textArea;
	
    public void initialize() 
    {
        System.out.println("Displaying Text Viewer window!");
    }
    
    @FXML
    private void test(ActionEvent event)
    {
    	Stage stage = (Stage) exitButton.getScene().getWindow();
    	stage.close();
    }
    
    public void setText(String text)
    {
    	textArea.setText(text);
    }
    

}
