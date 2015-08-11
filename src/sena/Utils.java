package sena;

import ij.ImagePlus;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import weka.core.DenseInstance;
import weka.core.Instances;

public class Utils {
	public static Mat imagePlusToOpenCV(ImagePlus imp) {
		
		boolean grayscale = imp.getBytesPerPixel() == 1;
		
		byte [] pixels;
		BufferedImage image = imp.getBufferedImage();
		if (grayscale) {
			pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		}
		else {
			int[] pixelsInt;
			pixelsInt = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		    pixels = new byte[pixelsInt.length*3];
			
			for(int i = 0; i < pixelsInt.length; i++) {
				pixels[3*i] = (byte) (pixelsInt[i]);
				pixels[3*i+1] = (byte) (pixelsInt[i] >> 8);
				pixels[3*i+2] = (byte) (pixelsInt[i] >> 16);
			}
		}
		
		Mat ans = new Mat(image.getWidth(), image.getHeight(), grayscale ? CvType.CV_8UC1 : CvType.CV_8UC3);
		ans.put(0,  0,  pixels);
		return ans;
	}
	
	public static Mat makeSubImage(Mat image, List<MatOfPoint> contours) {		
		int width = image.width();
		int height = image.height();
		
		//Black image as canvas for mask
		Mat mask = new Mat(width, height, CvType.CV_8UC1, new Scalar(0,0,0));
		for(int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
			Imgproc.drawContours(mask, contours, contourIdx, new Scalar(255, 255, 255), -1);
		}
		
		//Mask with white pixels as pixels inside contours
		byte [] maskPixels = new byte [width*height];
		mask.get(0, 0, maskPixels);
		
		int numChannels = image.channels();
		
		//RGB = 3 channels. Grayscale = 1 channel
		byte [] originalPixels = new byte [numChannels*width*height];
		image.get(0,  0, originalPixels);
		
		//Paint as original color if mask is white or black if mask is black
		byte [] output = new byte [numChannels*width*height];
		for(int i = 0; i < width*height; i++) {
			if(maskPixels[i] == 0) {
				for(int j = 0; j < numChannels; j++) 
					output[numChannels*i+j] = 0;
			}
			else {
				for(int j = 0; j < numChannels; j++) 
					output[numChannels*i+j] = originalPixels[numChannels*i+j];
			}		
		}
		
		//Create output image
		Mat ans = new Mat(width, height, image.type());
		ans.put(0,  0,  output);
		return ans;
	}

	
	public static String getParentFolder(String path) {
		return path.substring(0,path.lastIndexOf("\\"));
	}
	public static String getFileName(String path) {
		return path.substring(path.lastIndexOf("\\"));
	}
	
	public static String join(String[] list, String delim) {

	    StringBuilder sb = new StringBuilder();
	    String loopDelim = "";

	    for(String s : list) {
	        sb.append(loopDelim);
	        sb.append(s);            
	        loopDelim = delim;
	    }

	    return sb.toString();
	}
	
	public static String join(List<String> list, String delim) {

	    StringBuilder sb = new StringBuilder();
	    String loopDelim = "";

	    for(String s : list) {
	        sb.append(loopDelim);
	        sb.append(s);         
	        loopDelim = delim;
	    }

	    return sb.toString();
	}
	
	
	public static Instances instanceFromImageData(Image_Data data, List<String> classes) {
		double [] ret = new double [Image_Data.headers.length];
		ret[0] = data.Area;
		ret[1] = data.I1;
		ret[2] = data.I2;
		ret[3] = data.I3;
		ret[4] = data.I4;
		ret[5] = data.I5;
		ret[6] = data.I6;
		ret[7] = data.I7;

		Instances dataRaw = data.makeInstances(classes);
		dataRaw.add(new DenseInstance(1.0, ret));
		
		return dataRaw;
	}
	
	public static void resetFolder(String path) {
		File f = new File(path);
		if (f.exists() && f.isDirectory()) {
			for(String s: f.list()){
			    File currentFile = new File(f.getPath(),s);
			    currentFile.delete();
			}
		}
		else {
			f.mkdirs();	
		}
	}
}
