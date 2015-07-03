package sena;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/*Plugin to automatically measure data from CLSM
 * Author: Guilherme Sena
 */

public class Sena_Plugin implements PlugInFilter {	
	public void run(ImageProcessor ip) {
		IJ.log("Starting Sena Plugin...");
		ImagePlus img = IJ.getImage();
		
		int type = (new Image_Parser(img)).parseImage();
				   (new Image_Filter(img, type)).initFilters();
		           (new Image_Segmenter(ip)).segmentImage();
		           
		
		//IJ.log("Proceeding to WEKA classifier...");
		//(new Image_Trainer()).testing();
	}
	
	public int setup(String args, ImagePlus ip) {
		// TODO Auto-generated method stub
		return DOES_ALL;
	}
}