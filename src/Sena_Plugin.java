import java.awt.*;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.*;
import ij.plugin.frame.*;



public class Sena_Plugin implements PlugIn {
	    
    public void run(String arg) {
    		IJ.showMessage("Oi Débora!");
    	
    		ImagePlus img = IJ.getImage();
    		
    	    IJ.log("Verifying if image is 8-bit grayscale");
    	    if(!(img.getType() == ImagePlus.GRAY8)) {
    	    	IJ.showMessage("Current stack is not 8-bit grayscale! Mode is "+img.getType()+". Please run Image > Type > 8-bit to convert");   
    	    	return; 	    	
    	    }
    	   
    	    IJ.log("Current image is grayscale, ok!");
	        IJ.log("Setting otsu treshold & inverting...");
	        IJ.run (img, "Threshold", "");
	        IJ.run (img, "Invert", "");
    	    IJ.log("Starting median filter...");
	        IJ.run (img, "Despeckle", "");
	        
    }
}