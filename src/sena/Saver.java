package sena;

import ij.ImagePlus;
import ij.io.FileSaver;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFEncodeParam;

public class Saver {
	public static void saveImagePlus (ImagePlus imp, String path) {
		FileSaver fs = new FileSaver(imp);
		fs.saveAsJpeg(path);
	}

	public static void saveOpenCv(String path, Mat mat) {
		Highgui.imwrite(path, mat);
	}

	public static void saveTrainerContoursImage(Image_Trainer trainer, String contoursPath, Scalar classColor) {
		Mat img_ocv = Utils.imagePlusToOpenCV(trainer.img);

		//Force convert to RGB (to paint contours)
		if(img_ocv.channels() == 1)
			Imgproc.cvtColor(img_ocv, img_ocv, Imgproc.COLOR_GRAY2BGR);

		for (int contourIdx = 0; contourIdx < trainer.contours.size(); contourIdx++) {
			if(trainer.getMoments(trainer.contours.get(contourIdx)).get_m00() < Config.MIN_AREA)
				continue;			
			Imgproc.drawContours(img_ocv, trainer.contours, contourIdx, classColor, 3);
		}
		Highgui.imwrite(contoursPath, img_ocv);		
	}


	public static boolean makeCsvData(List<Image_Data> info, String csvPath) {
		File output = new File(csvPath);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(output));
			writer.write(Image_Data.csvHeaders());
			for(Image_Data d : info) {
				writer.write(d.toCsvRow());
			}

		} catch (IOException e) {
			//Finally will be executed even after return
			return false;
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	public static boolean makeArffData(List<Image_Data> info, String arffPath, List<String> classes) {
		File output = new File(arffPath);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(output));

			//Write headers
			writer.write("@relation training_set\n\n");
			for(String attr : Image_Data.headers) {
				if(attr.equals(Image_Data.CLASS_HEADER)) 
					writer.write("@attribute "+attr+" {"+Utils.join(classes, ",")+"}\n");
				else
					writer.write("@attribute "+attr+" numeric\n");

			}
			writer.write("\n@data\n");

			for(Image_Data d : info) {
				writer.write(d.toCsvRow());
			}

		} catch (IOException e) {
			return false;
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	public static void mergeTiffs(List<String> inputImages, String outputPath) {
		List<BufferedImage> images = new ArrayList<BufferedImage>();

		try
		{
			for (String image: inputImages)
			{
				SeekableStream ss = new FileSeekableStream(new File(image));
				ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);

				int numPages = decoder.getNumPages();
				for(int j = 0; j < numPages; j++)
				{
					PlanarImage op = new NullOpImage(decoder.decodeAsRenderedImage(j), null, null, OpImage.OP_IO_BOUND);
					images.add(op.getAsBufferedImage());
				}
			}

			TIFFEncodeParam params = new TIFFEncodeParam();
			OutputStream out = new FileOutputStream(outputPath); 
			ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);
			List<BufferedImage> imageList = new ArrayList<BufferedImage>();   
			for (int i = 1; i < images.size(); i++)
				imageList.add(images.get(i)); 

			params.setExtraImages(imageList.iterator()); 
			encoder.encode(images.get(0));
			out.close();
		}
		catch (Exception e)
		{
			System.out.println("Exception " + e);
		}
	}
}
