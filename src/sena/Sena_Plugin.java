package sena;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import org.opencv.core.Core;

/*Plugin to automatically measure data from CLSM
 * Author: Guilherme Sena
 */

public class Sena_Plugin implements PlugInFilter {

	private GenericDialog makeDialog() {
		//Plugin dialog for parameter configuration
		GenericDialog d = new GenericDialog(Config.PLUGIN_NAME);
		d.addMessage(Config.PLUGIN_DESCRIPTION);
		d.addStringField("Training set folder: ", Config.DEFAULT_TRAINING_SET);
		d.addStringField("Test set folder: ", Config.DEFAULT_TEST_SET);
		d.addNumericField("Gaussian Blur Sigma", 2, 0);
		d.addChoice("Threshold Method", Config.THRESHOLD_METHODS, "Triangle");
		d.addCheckbox("Save thresholds and contours", true);
		d.addCheckbox("Skip training (if you already ran the plugin for your training set and have training_data.arff)", false);

		return d;
	}

	public void run(ImageProcessor ip) {

		GenericDialog d = makeDialog();
		d.showDialog();
		if(d.wasCanceled()) 
			return;

		//Let's rock!
		IJ.log("Starting sena plugin...");

		//Data from dialog
		String trainingPath = d.getNextString();
		String testPath = d.getNextString();
		String thresholdMethod = d.getNextChoice();
		double sigma = d.getNextNumber();
		boolean saveImages = d.getNextBoolean();
		boolean skipTraining = d.getNextBoolean();

		//Parameters from data
		Parameters plugInParams = new Parameters (sigma, thresholdMethod, saveImages);

		IJ.log("Threshold Method: "+thresholdMethod+"\nSigma = "+sigma);

		if(!skipTraining) {
			//Trains image
			Trainer trainer = new Trainer(trainingPath, plugInParams);
			trainer.train();
			trainer.writeDataFiles();
		}
		
		//Test image
		String arffPath = trainingPath+"\\"+Config.TRAIN_ARFF_DATA;
		Tester tester = new Tester(arffPath, testPath, plugInParams);
		tester.test();

		IJ.log("Sena Plugin analysis complete!");

	}

	public int setup(String args, ImagePlus ip) {
		//Load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		return NO_IMAGE_REQUIRED;
	}
}