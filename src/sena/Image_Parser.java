package sena;

import ij.IJ;
import ij.ImagePlus;

public class Image_Parser extends Logger {
	private static final String PROCEDURE_NAME = "Image Parser";
	public static final int IS_IMAGE = 0;
	public static final int IS_STACK = 1;
	
	private ImagePlus imp;
	
	public Image_Parser(ImagePlus imp) {
		super(PROCEDURE_NAME);
		
		this.imp = imp;

	}
	public int parseImage() {
		log("Verifying if stack is correctly parsed...");
		//Converts images to 8-bit if necessary
		int size = imp.getStack().getSize();
		for(int i = 0; i < size; i++) {
			IJ.setSlice(i);
			if(IJ.getImage().getType() != ImagePlus.GRAY8) {
				log("Image "+i+" is not grayscale. Running 8-bit for conversion...");
				IJ.run("8-bit");
				break;
			}
		}
		
		if(size == 1) {
			log("Found file to be IMAGE");
			return Image_Parser.IS_IMAGE;
		}
		
		log("Found file to be STACK");
		return Image_Parser.IS_STACK;
	}

}
