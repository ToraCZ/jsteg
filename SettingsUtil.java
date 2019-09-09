package jsteg;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.scene.control.TreeItem;

public class SettingsUtil {

	public static String settingsFileName = "settings.jsteg";

	/**
	 * minimum skip between bits 
	 */
	public static double skipFactor;
	/**
	 * default channels
	 */
	public static String channels;
	/**
	 * bits used for message size
	 */
	public static int sizeBits;
	/**
	 * bits used for a single char
	 */
	public static int charBits;
	/**
	 * default password
	 */
	public static String defPassword;
	/**
	 * default fileSuffix
	 */
	public static String fileSuffix;
	/**
	 * language of the app
	 */
	public static String language;


	/**
	 * Localized Strings
	 */
	public static ResourceBundle localStrings;

	/**
	 * Loads user settings from settings file
	 * @throws IOException if the file can not be loaded
	 */
	public static void loadUserSettings() throws IOException{
		Properties settings = new Properties();
		FileInputStream input;
		try {
			input = new FileInputStream(settingsFileName);
			settings.load(input);
			skipFactor = Double.parseDouble(settings.getProperty("skipFactor"));
			channels = settings.getProperty("channels");
			sizeBits = Integer.parseInt(settings.getProperty("sizeBits"));
			charBits = Integer.parseInt(settings.getProperty("charBits"));
			defPassword = settings.getProperty("defPassword");
			fileSuffix = settings.getProperty("fileSuffix");
			language = settings.getProperty("language");
			localStrings = ResourceBundle.getBundle("jsteg.lang", new Locale(language));
		} catch (Exception e) {
			restoreDefaults();
		}
	}

	/**
	 * saves user settings to file
	 * @param settingsList list of the settings
	 * @throws IOException if the fiel can not be written to
	 */
	public static void saveUserSettings(List<Setting> settingsList) throws IOException {
		Properties settings = new Properties();
		FileOutputStream output;
		settingsList.forEach(setting -> settings.setProperty(setting.getId(), setting.getValue()));
		try {
			output = new FileOutputStream(settingsFileName);
			settings.store(output,null);
		} catch (IOException e) {
			throw new IOException(SettingsUtil.localStrings.getString("error.settingsNotSaved"));
		}
		loadUserSettings();
	}

	/**
	 * Creates a tree with all help items loaded from jsteg.help
	 * @return root node of the help tree
	 */
	public static TreeItem<Help> getHelpItems(){
		ResourceBundle helpBundle = ResourceBundle.getBundle("jsteg.help", new Locale(language));
		TreeItem<Help> root = new TreeItem<>();

		String[] helpIDs = helpBundle.getString("ids").split(" ");
		List<TreeItem<Help>> helpItems = new ArrayList<>();
		for (String id : helpIDs) {
			helpItems.add(new TreeItem<Help>(new Help(id, helpBundle.getString(id+".topic"), helpBundle.getString(id+".text"))));
		}

		for (TreeItem<Help> helpItem : helpItems) {
			if(!helpItem.getValue().id.contains(".")) root.getChildren().add(helpItem);
			else{
				String parentId = helpItem.getValue().id.replaceAll("\\.\\w+$", "");
				for (TreeItem<Help> parentItem : helpItems) {
					if(parentItem.getValue().id.equals(parentId)){
						parentItem.getChildren().add(helpItem);
						break;
					}				
				}
			}
		}

		return root;
	}

	/**
	 * returns a list of Setting objects loaded from settings file
	 * @return Setting objects list
	 */
	public static List<Setting> getSettings(){
		Properties settings = new Properties();
		FileInputStream input;
		try {
			input = new FileInputStream(settingsFileName);
			settings.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<Setting> settingList = new ArrayList<>();
		settings.forEach( (name,value) -> settingList.add(new Setting(name.toString(), value.toString())));
		return settingList;
	}

	/**
	 * restores default settings
	 * @throws IOException if settigns file can't be accessed
	 */
	public static void restoreDefaults() throws IOException{
		Properties settings = new Properties();
		FileOutputStream output;
		try {
			output = new FileOutputStream(settingsFileName);
		} catch (IOException e) {
			throw new IOException(SettingsUtil.localStrings.getString("error.settingsNotRestored"));
		}
		skipFactor = 0.5;
		settings.setProperty("skipFactor", skipFactor+"");
		channels = "RGB";
		settings.setProperty("channels", channels);
		sizeBits = 16;
		settings.setProperty("sizeBits", sizeBits+"");
		charBits = 8;
		settings.setProperty("charBits", charBits+"");
		defPassword = "";
		settings.setProperty("defPassword", defPassword);
		fileSuffix = "_enc";
		settings.setProperty("fileSuffix", fileSuffix);
		if(language == null) language = Locale.getDefault().getLanguage().toUpperCase();
		settings.setProperty("language", language);
		localStrings = ResourceBundle.getBundle("jsteg.lang", new Locale(language));
		settings.store(output, null);
	}



}
