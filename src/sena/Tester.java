package sena;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import weka.classifiers.functions.Logistic;
import weka.core.Instances;

public class Tester extends Logger {
	private String arffPath;
	private String path;
	private Instances data;
	private Logistic rf;
	private Parameters params;
	
	private List<String> classes;
	
	public Tester (String arffPath, String testPath, Parameters params) {
		this.arffPath = arffPath;
		this.path = testPath;
		this.params = params;
		Utils.resetFolder(testPath+"\\"+Config.FILTER_FOLDER);
		Utils.resetFolder(testPath+"\\"+Config.CONTOURS_FOLDER);
		Utils.resetFolder(testPath+"\\"+Config.SEGMENTED_FOLDER);
		Utils.resetFolder(testPath+"\\"+Config.BIOFORMATS_FOLDER);
		
		buildClassifier();
	}

	public void buildClassifier() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(arffPath));
			data = new Instances(reader);
			reader.close();
			data.setClassIndex(data.numAttributes() - 1);
			
			classes = new ArrayList<String>();
			Enumeration<Object> classValues = data.classAttribute().enumerateValues();
			while(classValues.hasMoreElements()) {
				String newClass = (String) classValues.nextElement();
				classes.add(newClass);
			}

			rf = new Logistic();
			rf.buildClassifier(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test() {
		File dir = new File(path);
		Map<String, List<String>> segmentedImages = new HashMap<String, List<String>>();
		for(String className: classes)
			segmentedImages.put(className, new ArrayList<String>());
		
		for(File f: dir.listFiles()) {
			if(!f.isFile())
				continue;

			String imagePath = path+"\\"+f.getName();
			Image_Trainer trainer = new Image_Trainer(imagePath, params, "");
			
			List<MatOfPoint> contours = trainer.contours;
			Map<String, List <MatOfPoint>> contoursByClass = new HashMap<String, List<MatOfPoint>>();
			
			for(String className : classes) {
				contoursByClass.put(className, new ArrayList<MatOfPoint>());
			}
			
			log("Classifying contours for "+f.getName());
			for(MatOfPoint contour: contours) {
				Image_Data imageData = trainer.dataFromContour(contour);
				Instances dataInstance = Utils.instanceFromImageData(imageData, classes);
				
				try {
					double instanceClass = rf.classifyInstance(dataInstance.firstInstance());
					String className = data.classAttribute().value((int) Math.floor(instanceClass));
					
					List<MatOfPoint> temp = contoursByClass.get(className);
					temp.add(contour);
					contoursByClass.put(className, temp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log("Unable to classify [???]");
					e.printStackTrace();
				}
			}
			
			Mat originalImage = Utils.imagePlusToOpenCV(trainer.img);
			for(String className : classes) {
				Mat mat = Utils.makeSubImage(originalImage, contoursByClass.get(className));
				
				String segmentedPath = path+"\\"+Config.SEGMENTED_FOLDER+"\\"+className+" - "+f.getName();
				log("Saving segmented image...");
				Saver.saveOpenCv(segmentedPath, mat);
				
				List<String> currentImages = segmentedImages.get(className);
				currentImages.add(segmentedPath);
				segmentedImages.put(className, currentImages);
			}
		}
		
		for(String className : classes) {
			log("Creating video for "+className+"...");
			try {
				String mergedTiffPath = path+"\\"+Config.BIOFORMATS_FOLDER+"\\"+className+".tif";
				Saver.mergeTiffs(segmentedImages.get(className), mergedTiffPath);
			}
			catch (Exception e) {
				log("Something terrible happened!");
				e.printStackTrace();
			}
		}
	}
}
