package jsteg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import javax.imageio.ImageIO;

public class Encoder {
	
	/**
	 * original image used for encoding
	 */
	private BufferedImage originalImage;
	/**
	 * image encoded with a message
	 */
	private BufferedImage encodedImage;
	/**
	 * image with highlighted encoded pixels
	 */
	private BufferedImage encodedHighlight;
	/**
	 * password used for encoding
	 */
	private String password;
	/**
	 * channels used for encoding. combination of R/G/B chars
	 */
	private String channels;

	/**
	 * Constructor for image encoder
	 * @param imageFile file containing the image to encode to
	 * @throws Exception if the image can't be read or is not an image
	 */
	public Encoder(File imageFile) throws Exception{
		String imageFormat = Files.probeContentType(imageFile.toPath());
		if(!imageFormat.startsWith("image/")) throw new Exception(SettingsUtil.localStrings.getString("error.notAnImage"));
		try {
			originalImage = ImageIO.read(imageFile);
		} catch (IOException e) {
			throw new IOException(SettingsUtil.localStrings.getString("error.readFail"));
		}
	}
	
	/**
	 * Calculates the number of available characters to encode to image for this encoder.
	 * @return number of available characters
	 */
	public int getCharsAvailable(){
		int bitsAvailable = originalImage.getWidth() * originalImage.getHeight() * channels.length();
		return Math.max(((bitsAvailable - 3 - SettingsUtil.sizeBits) / SettingsUtil.charBits) / 3,0);
	}


	/**
	 * Updates the encoded image of this encoder with the given message.
	 * @param msg the message to be encoded
	 * @throws Exception if the given message is too big for this encoder's image
	 */
	public void updateEncoded(String msg) throws Exception{
		
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		int bitsAvailable = width * height * channels.length();
		int skipMax =  bitsAvailable / (msg.length() * SettingsUtil.charBits + SettingsUtil.sizeBits + 3);
		if(skipMax < 2) throw new Exception(SettingsUtil.localStrings.getString("error.fileTooSmall"));
		
		encodedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		encodedImage.createGraphics().drawImage(originalImage, 0, 0, null);
		
		encodedHighlight = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		encodedHighlight.createGraphics().drawImage(originalImage, 0, 0, null);
		
		writeInfo(msg.length());	
		writeMsg(msg, skipMax);
		
	}
	
	private int updateBit(int v, int ch){
		v &= 254;
		v |= (ch & 1);
		return v;
	}
	
	/**
	 * Writes the message into encoded image.
	 * @param msg message to be written
	 * @param skipMax maximum pixels to skip between writes.
	 */
	private void writeMsg(String msg, int skipMax){
		Random rng = new Random(password.hashCode());
		int row = 0;
		int col = SettingsUtil.sizeBits/channels.length() + 1;
		int width = this.originalImage.getWidth();
		for (int i = 0; i < msg.length(); i++) {
			char ch = msg.charAt(i);
			int k = SettingsUtil.charBits;
			int pixel, a, r, g, b;
			while(k > 0){
				pixel = encodedImage.getRGB(col, row);
				a = (pixel >> 24) & 0xFF;
				r = (pixel >> 16) & 0xFF;
				g = (pixel >> 8) & 0xFF;
				b = pixel & 0xFF;
				for (int j = 0; j < channels.length() & k > 0; j++) {
					switch (channels.charAt(j)) {
					case 'R':
						r = updateBit(r,ch);
						break;
					case 'G':
						g = updateBit(g,ch);
						break;
					case 'B':
						b = updateBit(b,ch);
						break;
					default:
						break;
					}
					//move to the next char bit
					ch = (char) (ch >> 1);
					k--;
				}
				encodedImage.setRGB(col, row, (a << 24) + (r << 16) + (g << 8) + b);
				encodedHighlight.setRGB(col, row, Color.GREEN.getRGB());
				col += ( (skipMax * SettingsUtil.skipFactor) + rng.nextInt((int)(skipMax-skipMax * SettingsUtil.skipFactor)) );
				while(col >= width){
					col -= width;
					row++;
				}
			}
		}
	}
	
	/**
	 * Writes info about the encoding (msg length and channels used) into the image.
	 * @param size length of the message to be encoded.
	 */
	private void writeInfo(int size) {
		int row = 0;
		int col = 0;
		int pixel, r, g, b;
		pixel =  originalImage.getRGB(col, row);
		r = (pixel >> 16) & 0xFE;
		g = (pixel >> 8) & 0xFE;
		b = pixel & 0xFE;
		for (int j = 0; j < channels.length(); j++) {
			switch (channels.charAt(j)) {
			case 'R':
				r |=  1;
				break;
			case 'G':
				g |=  1;;
				break;
			case 'B':
				b |=  1;
				break;
			default:
				break;
			}
		}
		encodedImage.setRGB(col, row, (0xFF << 24) + (r << 16) + (g << 8) + b);
		col++;
		int i = SettingsUtil.sizeBits;
		while (i > 0) {
			pixel =  originalImage.getRGB(col, row);
			r = (pixel >> 16) & 0xFF;
			g = (pixel >> 8) & 0xFF;
			b = pixel & 0xFF;
			for (int j = 0; j < channels.length(); j++) {
				switch (channels.charAt(j)) {
				case 'R':
					r = updateBit(r,size);
					break;
				case 'G':
					g = updateBit(g,size);
					break;
				case 'B':
					b = updateBit(b,size);
					break;
				default:
					break;
				}
				size = size >> 1;
				i--;
				if(i == 0)break;
			}
			encodedImage.setRGB(col, row, (0xFF << 24) + (r << 16) + (g << 8) + b);
			col++;
		}
	}

	/**
	 * Saves the encoded iamge into a file.
	 * @param outputFile fiel to write the encoded image to
	 * @param imageFormat format of the output image
	 * @throws IOException when an IO error of any sort occurs
	 */
	public void saveEncoded(File outputFile, String imageFormat) throws IOException{
		ImageIO.write(encodedImage, imageFormat, outputFile);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public BufferedImage getOriginalImage() {
		return originalImage;
	}

	public BufferedImage getHighlightedImage() {
		return encodedHighlight;
	}


	public String getChannels() {
		return channels;
	}

	public void setChannels(String channels) {
		this.channels = channels;
	}


}
