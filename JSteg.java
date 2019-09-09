package jsteg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;

public class JSteg extends Application{


	@Override
	public void start(Stage primaryStage) throws Exception {

		primaryStage.setTitle("JSteg");	
		File f = new File(SettingsUtil.settingsFileName);
		if(!f.isFile()){
			String langStr=langSelect();
			if(langStr == null) return;
			else SettingsUtil.language = langStr;
		}
		FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
		SettingsUtil.loadUserSettings();

		loader.setResources((SettingsUtil.localStrings));
		primaryStage.setScene(new Scene(loader.load(),900,600));
		primaryStage.setResizable(false);


		primaryStage.show();
	}

	/**
	 *  Method for selecting default language on first run.
	 * @return Language code String
	 */
	private String langSelect(){
		List<String> languages = new ArrayList<String>();
		languages.add("English");
		languages.add("Èeština");
		ChoiceDialog<String> dialog = new ChoiceDialog<String>("English",languages);
		dialog.setTitle("Language selection");
		dialog.setHeaderText("Please select a language:");
		Optional<String> result = dialog.showAndWait();
		if(result.isPresent()){
			if(result.get().equals("English")) return "EN";
			if(result.get().equals("Èeština")) return "CZ";
		}
		return null;

	}

	/**
	 * Main method.
	 * @param args commandline arguments
	 */
	public static void main(String[] args) {
		launch();
	}

}

