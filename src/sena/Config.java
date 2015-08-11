package sena;

import org.opencv.core.Scalar;

public class Config {
	public static final String PLUGIN_NAME="Sena Plugin";
	public static final String PLUGIN_DESCRIPTION =
			"This plugin will automatically separate imagesfrom a Test set given some training \n"
			+ "sets and run COMSTAT analysis in each one of them. \n"
			+ "Training set must contain only image folders (one for each class). \n"
			+ "Test sets must contain only images \n";
	
	public static final String TRAIN_CSV_DATA = "training_data.csv";
	public static final String TRAIN_ARFF_DATA = "training_data.arff";
	
	public static final String DEFAULT_TRAINING_SET = "C:\\Sets\\Training_Sets\\exemplo_de_treinamento";
	public static final String DEFAULT_TEST_SET = "C:\\Sets\\Test_Set\\exemplo_de_teste";
	
	public static final String FILTER_FOLDER = "Filters";
	public static final String CONTOURS_FOLDER = "Contours";
	public static final String SEGMENTED_FOLDER = "Segmentation";
	public static final String BIOFORMATS_FOLDER = "Final_Results";
	
	public static final String[] THRESHOLD_METHODS = {"Triangle","Otsu"};
	public static final double MIN_AREA = 6.5;
	
	public static final Scalar [] COLORS_RGB = {
		new Scalar(0,255,255), //yellow
		new Scalar(255,0,255), //pink
		new Scalar(255,0,0), //blue
		new Scalar(0,0,255), //purple
		new Scalar(0,255,0), //red
		new Scalar(128,0,128), //green
		new Scalar(128,128,128), //gray
		new Scalar(0,0,0), //black
		new Scalar(0,140,255) //orange
	};
}
