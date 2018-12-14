package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FXMLApp extends Application{
	
	public static void main(String[] args) {
        launch(args);
    }
	
	@Override
    public void start(Stage stage) throws Exception 
	{
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("application/main-ui.fxml"));
		Parent root = loader.load();
		FXMLController controller = loader.getController();
		
		
		//Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("application/main-ui.fxml"));
        Scene scene = new Scene(root);
    
        stage.setTitle("REST Client Application");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("logo.png")));
        stage.show();
    }

}
