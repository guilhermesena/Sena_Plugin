package sena;

import ij.ImagePlus;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

//Per image trainer
public class Image_Trainer  extends Logger {
	private String className;
	public String path;
	public String folderPath;
	public String filename;
	public Scalar classColor;

	public ImagePlus img;
	public ImagePlus threshold;

	public List <MatOfPoint> contours;

	public Image_Trainer(String path, Parameters params, String className) {
		super();
		this.className = className;
		this.path = path;
		this.filename = Utils.getFileName(path);
		this.folderPath = Utils.getParentFolder(path);
				
		Filter filter = new Filter(path, params.thresholdMethod, params.sigma);
		if(!filter.filter(params.saveImages))
			log("Invalid filter, aborting training...");

		this.img = filter.rawImage;
		this.threshold = filter.filteredImage;

		calculateContours();
	}
		
	public Image_Data dataFromContour(Mat contour) {
		Image_Data contourData = new Image_Data();
		contourData.fillHuMoments(getHuMoments(contour));
		contourData.fillDataFromMoments(getMoments(contour));
		
		return contourData;
	}

	public List<Image_Data> getData() {
		log("Acquiring data for image "+filename+"...");
		List<Image_Data> ans = new ArrayList<Image_Data>();		
		for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
			Image_Data contourData = dataFromContour(contours.get(contourIdx));			
			if(contourData.Area < Config.MIN_AREA)
				continue;
			
			contourData.className = this.className;
			ans.add(contourData);
		}
		return ans;
	}

	private void calculateContours() {
		log("Finding contours for image "+filename+"...");
		Mat thresh_ocv = Utils.imagePlusToOpenCV(this.threshold);
		contours = new ArrayList<MatOfPoint>();    

		Imgproc.findContours(thresh_ocv, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
		
	}	
	
	public Moments getMoments(Mat contour) {
		return Imgproc.moments(contour, false);
	}

	private double[] getHuMoments(Mat contour) {
		double [] hu_values = new double[7];
		Moments mom = getMoments(contour);
		Mat hm = new Mat();
		Imgproc.HuMoments(mom, hm);
		for(int i = 0; i < 7; i++) {
			double hu = hm.get(i,0)[0];
			hu_values[i] = Math.signum(-1*hu)*1.0f/Math.log10(Math.abs(hu));
			
			if(Double.isNaN(hu_values[i]))
				hu_values[i] = 0;
		}
		
		return hu_values;
	}
}
