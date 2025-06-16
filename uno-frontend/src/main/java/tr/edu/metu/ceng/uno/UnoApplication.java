package tr.edu.metu.ceng.uno;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UnoApplication {

	public static void main(String[] args) {
		Application.launch(JavaFXApplication.class, args);
	}
}