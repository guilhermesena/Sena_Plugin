package sena;
import java.io.File;
import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageConverter;

//Per image filter applier
public class Filter extends Logger {
	
	private String path;
	private String folderPath;
	private String thresholdMethod;
	private double sigma;
	
	public ImagePlus rawImage;
	public ImagePlus filteredImage;
	
	public Filter(String path, String thresholdMethod, double sigma) {
		super();
		this.thresholdMethod = thresholdMethod;
		this.path = path;
		
		this.folderPath = Utils.getParentFolder(path);
		this.sigma = sigma;
		filteredImage = null;
	}
	
	//Add IJ pre-filters here
	private void applyIJFilters(ImagePlus imp) {
		log("Adding gaussian blur with sigma = "+sigma);
		IJ.run(imp, "Gaussian Blur...", "sigma="+sigma);
		
		log("Thresholding. Method = "+thresholdMethod);
		IJ.run(imp, "Auto Threshold", "method="+thresholdMethod+" white");
		
		log("Binarizing image...");
		IJ.run(imp, "Make Binary", "method="+thresholdMethod+" background=Default calculate");
		
		log("Applying median filter...");
		IJ.run(imp, "Despeckle","");
		
		//log("Watershed segmenting...");
		//IJ.run(imp, "Watershed","");
	}
	
	public boolean filter (boolean saveImage) {
		File f = new File(path);	
		Opener op = new Opener();

		ImagePlus original = op.openImage(path);
		
		if(original == null) {
			log("Impossible to open file as image: "+path+". Aborting...");
		}
		
		this.rawImage = original;
		
		ImagePlus filtered = op.openImage(path);
		
		log("Converting "+f.getName()+" to grayscale...");
		ImageConverter ic = new ImageConverter(filtered);
		ic.convertToGray8();

		applyIJFilters(filtered);
		this.filteredImage = filtered;
		
		if(saveImage) {
			log("Saving filtered image for "+f.getName());
			Saver.saveImagePlus(filtered, folderPath+"\\"+Config.FILTER_FOLDER+"\\filtered - "+f.getName());
		}
		
		log("Done filtering!");
		return true;
	}
}
