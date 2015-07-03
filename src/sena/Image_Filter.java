package sena;
import ij.IJ;
import ij.ImagePlus;

public class Image_Filter extends Logger {
	public static final String PROCEDURE_NAME = "Image Filter";
	private static final String THRESHOLD_METHOD="Otsu";
	private ImagePlus img;
	private int type;
	
	public Image_Filter(ImagePlus img, int type) {
		super(PROCEDURE_NAME);
		this.img = img;
		this.type = type;
	}
	
	public void initFilters() {
		IJ.setAutoThreshold(img, THRESHOLD_METHOD);
		if (type == Image_Parser.IS_IMAGE) {
			log("Image detected, adding image filters...");
			initFiltersImage();
		} else {
			log("Stack detected, adding stack filters...");
			initFiltersStack();
		}
	}
	
	private void initFiltersImage() {		
		
	}
	
	private void initFiltersStack () {
		log("Adding Kalman filter...");
		Kalman_Stack_Filter ksf = new Kalman_Stack_Filter();
		ksf.run("acquisition_noise=0.05 bias=0.80");
		
	}
}
