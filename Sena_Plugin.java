import java.awt.*;

import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.*;



public class Sena_Plugin implements PlugInFilter {
	public void run(ImageProcessor ip) {
		IJ.showMessage("Teste");
		// TODO Auto-generated method stub
		int w = ip.getWidth();
		int h = ip.getHeight();
		for(int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				if(ip.getPixel(i, j) == 255)
					ip.putPixel(i, j, 128);
			}
		}
	}

	public int setup(String args, ImagePlus ip) {
		// TODO Auto-generated method stub
		return DOES_8G;
	}
}