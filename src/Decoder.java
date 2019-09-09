package jsteg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import javax.imageio.ImageIO;

public class Decoder {

	/**
	 * image encoded with a message
	 */
	private BufferedImage encodedImage;
	/**
	 * password used for decoding
	 */
	private String password;
	/**
	 * channels used for encoding. combination of R/G/B chars
	 */
	private String channels = "";

	/**
	 * Constructor for image decoder
	 * @param imageFile file containing the image to decode
	 * @param password password to use for encoding
	 * @throws Exception if the image can't be read or is not an image
	 */
	public Decoder(File imageFile,String password) throws Exception{
		if(!Files.probeContentType(imageFile.toPath()).startsWith("image/")) throw new Exception(SettingsUtil.localStrings.getString("error.notAnImage"));
		try {
			BufferedImage toEncode = ImageIO.read(imageFile);
			this.encodedImage = new BufferedImage(toEncode.getWidth(), toEncode.getHeight(), BufferedImage.TYPE_INT_RGB);
			this.encodedImage.createGraphics().drawImage(toEncode, 0, 0, null); 
		} catch (IOException e) {
			throw new IOException(SettingsUtil.localStrings.getString("error.readFail"));
		}
		this.password = password;
	}


	/**
	 * Decodes message from this encoder's image
	 * @return the encoded message
	 * @throws Exception if there is certainly not an image encoded
	 */
	public String decode() throws Exception{
		int width = encodedImage.getWidth();
		int height = encodedImage.getHeight();
		int msgLen = readInfo();
		int bitsAvailable = width * height * channels.length() ;
		if( (msgLen * SettingsUtil.charBits > bitsAvailable) | msgLen == 0) throw new Exception(SettingsUtil.localStrings.getString("error.notEncoded"));
		int skipMax = bitsAvailable / (msgLen * SettingsUtil.charBits + SettingsUtil.sizeBits + 3);
		return readMsg(skipMax,msgLen);

	}

	/**
	 * Reads the encoded info about this image. Sets channel String and returns message length.
	 * @return length of the encoded message
	 */
	private int readInfo() {
		int row = 0;
		int col = 0;
		int size = 0;
		int pixel, r, g, b;
		pixel =  encodedImage.getRGB(col, row);
		r = (pixel >> 16) & 0xFF;
		g = (pixel >> 8) & 0xFF;
		b = pixel & 0xFF;
		channels = "";
		if((r & 0b1) == 1) channels += "R";
		if((g & 0b1) == 1) channels += "G";
		if((b & 0b1) == 1) channels += "B";
		col++;
		int i = SettingsUtil.sizeBits;
		while (i > 0) {
			pixel =  encodedImage.getRGB(col, row);
			r = (pixel >> 16) & 0xFF;
			g = (pixel >> 8) & 0xFF;
			b = pixel & 0xFF;
			for (int j = 0; j < channels.length(); j++) {
				switch (channels.charAt(j)) {
				case 'R':
					size += (r & 0b1) << SettingsUtil.sizeBits;
					break;
				case 'G':
					size += (g & 0b1) << SettingsUtil.sizeBits;
					break;
				case 'B':
					size += (b & 0b1) << SettingsUtil.sizeBits;
					break;
				default:
					break;
				}
				size = size >> 1;
					i--;
					if(i == 0)break;
			}
			col++;
		}
		return size;
	}
	
	/**
	 * Reads the encoded message.
	 * @param skipMax maximum pixels to skip between writes.
	 * @param msgLen length of the encoded message.
	 * @return the encoded message
	 */
	private String readMsg(int skipMax,int msgLen){
		Random rng = new Random(password.hashCode());

		String msg="";

		int row = 0;
		int col = SettingsUtil.sizeBits / channels.length() + 1;
		int width = encodedImage.getWidth();
		char ch = 0;
		for (int i = 0; i < msgLen; i++) {
			ch = 0;
			int k = SettingsUtil.charBits;
			int pixel, r, g, b;
			while(k > 0){
				pixel =  encodedImage.getRGB(col, row);
				r = (pixel >> 16) & 0xFF;
				g = (pixel >> 8) & 0xFF;
				b = pixel & 0xFF;
				for (int j = 0; j < channels.length(); j++) {
					//System.out.println((int)ch);
					switch (channels.charAt(j)) {
					case 'R':
						ch |=  (r & 0b1) << SettingsUtil.charBits;
						break;
					case 'G':
						ch |=  (g & 0b1) << SettingsUtil.charBits;
						break;
					case 'B':
						ch |=  (b & 0b1) << SettingsUtil.charBits;
						break;
					default:
						break;
					}
					ch = (char) (ch >> 1);
					k--;
					if(k == 0) break;
				}
				col += ( (skipMax * SettingsUtil.skipFactor) + rng.nextInt((int) (skipMax * (1-SettingsUtil.skipFactor))) );
				while(col >= width){
					col -= width;
					row++;
				}
			}
			msg += ch;
		}
		return msg;
	}


	public void setPassword(String password) {
		this.password = password;
	}




}
