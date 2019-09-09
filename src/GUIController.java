package jsteg;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;

public class GUIController implements Initializable {


	private Encoder encoder;
	//encode GUI elements
	@FXML private TextField encodeFilePath;
	@FXML private ImageView encodeImage;

	@FXML private TextArea encodeText;

	@FXML private TextField encodePassword;

	@FXML private Label encodeCharLimit;

	@FXML private CheckBox encodeChannelR;
	@FXML private CheckBox encodeChannelG;
	@FXML private CheckBox encodeChannelB;

	@FXML private Button encodeUpdateBtn;
	@FXML private Button encodeSaveBtn;

	private Decoder decoder;
	//decode GUI elements
	@FXML private TextField decodeFilePath;
	@FXML private Button decodeBtn;
	@FXML private ImageView decodeImage;

	@FXML private TextField decodePassword;

	@FXML private TextArea decodeText;

	//settigns GUI elements
	@FXML private TableView<Setting> settingsTable;
	@FXML private TableColumn<Setting,String> settingsTableName;
	@FXML private TableColumn<Setting,String> settingsTableValue;

	//help GUI elemnts
	@FXML private TreeView<Help> helpTree;
	@FXML private Label helpLabel;
	@FXML private Text helpText;



	@Override
	public void initialize(URL location, ResourceBundle resources) {
		populateSettingsTable();

		helpTree.setRoot(SettingsUtil.getHelpItems());
		helpTree.getSelectionModel().selectedItemProperty().addListener((obs,oldVal,newVal) ->{
			helpLabel.setText(newVal.getValue().topic);
			helpText.setText(newVal.getValue().text);		
		});
		helpTree.getSelectionModel().select(0);

		encodePassword.setText(SettingsUtil.defPassword);	
		decodePassword.setText(SettingsUtil.defPassword);

		if(SettingsUtil.channels.contains("R")) encodeChannelR.setSelected(true);
		if(SettingsUtil.channels.contains("G")) encodeChannelG.setSelected(true);
		if(SettingsUtil.channels.contains("B")) encodeChannelB.setSelected(true);

		encodeText.textProperty().addListener( (obs,oldVal,newVal) ->{
			encodeCharLimit.setText(encodeCharLimit.getText().replaceAll("\\d+/", newVal.length()+"/"));
		});


		updateEncodeCharLimit();
	}


	/**
	 * populates settings tabel with settings from SettingsUtil
	 */
	private void populateSettingsTable(){
		settingsTable.getItems().removeAll(settingsTable.getItems());
		settingsTableName.setCellValueFactory(new PropertyValueFactory<Setting, String>("name"));
		settingsTableValue.setCellValueFactory(new PropertyValueFactory<Setting, String>("value"));
		settingsTableValue.setCellFactory(TextFieldTableCell.forTableColumn());
		settingsTable.getItems().addAll(SettingsUtil.getSettings());
		settingsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	}

	/**
	 * Gets channel Strign accordign to the selected checkboxes
	 * @return a combination of R/G/B representing selected channels
	 */
	private String getChannelStr(){
		String chanStr = "";
		if(encodeChannelR.isSelected()) chanStr +="R";
		if(encodeChannelG.isSelected()) chanStr +="G";
		if(encodeChannelB.isSelected()) chanStr +="B";
		return chanStr;

	}

	/**
	 * shows 1:1 preview image
	 * @param event
	 */
	@FXML void previewPopup(MouseEvent event){
		if(encoder == null || encoder.getHighlightedImage() == null) return;
		Popup preview = new Popup();
		preview.getContent().add(new ImageView(SwingFXUtils.toFXImage(encoder.getHighlightedImage(),null)));
		preview.setAutoHide(true);
		preview.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> preview.hide());
		preview.show(encodeImage.getScene().getWindow());
	}

	/**
	 * Changes cursor to hand on hover over preview image
	 * @param event
	 */
	@FXML void previewMouseIn(MouseEvent event){
		if(encoder != null && encoder.getHighlightedImage() != null)	encodeImage.getScene().setCursor(Cursor.HAND);
	}

	/**
	 * Changes cursor back when moving from preview
	 * @param event
	 */
	@FXML void previewMouseOut(MouseEvent event){
		encodeImage.getScene().setCursor(Cursor.DEFAULT);
	}

	/**
	 * opens a filechooser to selecs the image for encodimg and creates its preview
	 * @param event
	 */
	@FXML void encodeSelectImage(MouseEvent event){
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(null);
		fc.getExtensionFilters().add(new ExtensionFilter(SettingsUtil.localStrings.getString("filechooser.imageFile"), "*.bmp","*.png","*.jpg","*.gif"));
		File imageFile = fc.showOpenDialog(encodeImage.getScene().getWindow());
		if(imageFile != null){ 
			Image image = new Image(imageFile.toURI().toString());
			encodeFilePath.setText(imageFile.getName());
			encodeImage.setImage(image);
			encodeImage.setX(0);
			encodeImage.setY(0);
			if(image.getWidth() > image.getHeight()){
				double offset = (encodeImage.getFitHeight() - encodeImage.getFitWidth() / image.getWidth() * image.getHeight()) / 2;
				encodeImage.setY(offset);
			}else{
				double offset = (encodeImage.getFitWidth() - encodeImage.getFitHeight() / image.getHeight() * image.getWidth()) / 2;
				encodeImage.setX(offset);
			}
			try {
				encoder = new Encoder(imageFile);
			} catch (Exception e) {
				Alert a = new Alert(AlertType.ERROR,e.getMessage());
				a.showAndWait();
			}
			encoder.setChannels(getChannelStr());
			encodeUpdateBtn.setDisable(false);
			updateEncodeCharLimit();
		}
	}

	/**
	 * changes encoder channels when checkbox is selected
	 * @param event
	 */
	@FXML void encodeChangeChannels(ActionEvent event){
		if(encoder != null){
			encoder.setChannels(getChannelStr());
			updateEncodeCharLimit();
		}
	}


	/**
	 * changes the neumber of available characters for encoding
	 */
	private void updateEncodeCharLimit(){
		int charsAvailable;
		if(encoder != null && encoder.getOriginalImage() != null) charsAvailable = encoder.getCharsAvailable();
		else charsAvailable = 0;
		encodeCharLimit.setText(encodeText.getLength()+"/"+ charsAvailable);
		if(encodeText.getLength() > charsAvailable) encodeText.deleteText(charsAvailable, encodeText.getLength());
		encodeText.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= charsAvailable ? change : null));
	}

	/**
	 * triggers update on the encoded image of encoder
	 * @param event
	 */
	@FXML void updateEncoded(ActionEvent event){
		try {
			encoder.setChannels(getChannelStr());
			encoder.setPassword(encodePassword.getText());
			encoder.updateEncoded(encodeText.getText());
			encodeImage.setImage(SwingFXUtils.toFXImage(encoder.getHighlightedImage(),null));
		} catch (Exception e) {
			Alert a = new Alert(AlertType.ERROR,e.getMessage());
			a.showAndWait();
		}
		encodeSaveBtn.setDisable(false);
	}

	/**
	 * saves the encoded image
	 * @param event
	 */
	@FXML void saveImage(ActionEvent event){
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(null);
		fc.setInitialFileName(encodeFilePath.getText().replaceAll("\\..*$", "")+SettingsUtil.fileSuffix);
		fc.getExtensionFilters().addAll(new ExtensionFilter("PNG", "*.png"),new ExtensionFilter("BMP","*.bmp"));
		File output = fc.showSaveDialog(encodeImage.getScene().getWindow());
		if(output != null){ 
			try {
				//TODO select output format
				encoder.saveEncoded(output, "png");
			} catch (Exception e) {
				Alert a = new Alert(AlertType.ERROR,e.getMessage());
				a.showAndWait();
			}
		}
	}

	/**
	 * opens a filechooser to selecs the image for decodimg and creates its preview
	 * @param event
	 */
	@FXML void decodeSelectImage(MouseEvent event){
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(null);
		fc.getExtensionFilters().add(new ExtensionFilter(SettingsUtil.localStrings.getString("filechooser.imageFile"), "*.bmp","*.png","*.jpg","*.gif"));
		File imageFile = fc.showOpenDialog(decodeImage.getScene().getWindow());
		if(imageFile != null){ 
			decodeFilePath.setText(imageFile.getName());
			Image image = new Image(imageFile.toURI().toString());
			decodeImage.setImage(image);
			decodeImage.setX(0);
			decodeImage.setY(0);
			if(image.getWidth() > image.getHeight()){
				double offset = (decodeImage.getFitHeight() - decodeImage.getFitWidth() / image.getWidth() * image.getHeight()) / 2;
				decodeImage.setY(offset);
			}else{
				double offset = (decodeImage.getFitWidth() - decodeImage.getFitHeight() / image.getHeight() * image.getWidth()) / 2;
				decodeImage.setX(offset);
			}
			try {
				decoder = new Decoder(imageFile, decodePassword.getText());
				decodeBtn.setDisable(false);
			} catch (Exception e) {
				Alert a = new Alert(AlertType.ERROR,e.getMessage());
				a.showAndWait();
			}
		}
	}

	/**
	 * decodes message from the image
	 * @param event
	 */
	@FXML void decode(ActionEvent event){
		try {
			decoder.setPassword(decodePassword.getText());
			decodeText.setText(decoder.decode());
			//encodeImage.setImage(SwingFXUtils.toFXImage(encoder.getHighlightedImage(),null));
		} catch (Exception e) {
			Alert a = new Alert(AlertType.ERROR,e.getMessage());
			a.showAndWait();
		}
	}

	/**
	 * commits settings edit change
	 * @param event
	 */
	@FXML void commitSettingEdit(CellEditEvent<Setting, String> event){
		event.getRowValue().setValue(event.getNewValue());
	}

	/**
	 * saves user settings and prompts them to restart
	 * @param event
	 */
	@FXML void saveSettings(ActionEvent event){
		try {
			SettingsUtil.saveUserSettings(settingsTable.getItems());
		} catch (IOException e) {
			Alert a = new Alert(AlertType.ERROR,e.getMessage());
			a.showAndWait();
		}
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(SettingsUtil.localStrings.getString("settings.restart.title"));
		alert.setHeaderText(SettingsUtil.localStrings.getString("settings.restart.text"));
		alert.showAndWait();
	}

	/**
	 * restores defautl user settings
	 * @param event
	 */
	@FXML void restoreDefaultSettings(ActionEvent event){
		try {
			SettingsUtil.restoreDefaults();
		} catch (IOException e) {
			Alert a = new Alert(AlertType.ERROR,e.getMessage());
			a.showAndWait();
		}
		populateSettingsTable();
	}


}
