package jsteg;

import javafx.beans.property.SimpleStringProperty;

public class Setting {

	/**
	 * ID of the setting - for loading from settings file
	 */
	private final String id;
	/**
	 * full name of the setting
	 */
	private final SimpleStringProperty name = new SimpleStringProperty();
	/**
	 * value of the setting
	 */
	private final SimpleStringProperty value = new SimpleStringProperty();

	public Setting(String id,String value){
		this.id = id;
		setName(SettingsUtil.localStrings.getString("settings.names."+id));
		setValue(value);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}
	
	public String getValue() {
		return value.get();
	}

	public void setValue(String value) {
		this.value.set(value);
	}

	public String getId() {
		return id;
	}
}
