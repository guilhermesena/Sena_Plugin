import java.awt.*;

import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.*;

class Slice {
	
}

public class Sena_Plugin implements PlugInFilter {
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;
	private int [][] visited;
	
	private boolean parseImage(ImageProcessor ip) {
		
		IJ.showMessage("Verifying if stack is correctly parsed");
		
		ImagePlus imp = IJ.getImage();
		if (imp.getStackSize()==1) {
			IJ.error("Stack required"); 
			return false;
		}
		
		
		int size = imp.getStack().getSize();
		for(int i = 0; i < size; i++) {
			IJ.setSlice(i);
			if(IJ.getImage().getType() != ImagePlus.GRAY8) {
				IJ.error("Image number "+i+" is not 8-bit grayscale");
				return false;
			}
		}
		
		return true;
	}
	
	private void initFilters(ImageProcessor ip) {
		IJ.showMessage("Adding Filters");
		
		IJ.showMessage("Adding Kalman filter");
		Kalman_Stack_Filter ksf = new Kalman_Stack_Filter();
		ksf.run("");
		
		IJ.showMessage("Auto Thresholding...");
		ip.autoThreshold();
		
		IJ.showMessage("Eroding...");
		ip.erode();
		
	}
	
	private void segmentImage(ImageProcessor ip) {
		IJ.showMessage("Starting image segmentation");
		int numParticles = 0;
		int MIN_SIZE = 50;
		
		int SMALLEST = Integer.MAX_VALUE;
		int LARGEST = -1;
		
		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for(int j = 0; j < IMAGE_HEIGHT; j++) {
				if(ip.getPixel(i, j) == 255 && visited[i][j] == 0) {
					int sz = dfs(i, j, ip);
					if(sz > MIN_SIZE)
						numParticles++;
					
					if(sz < SMALLEST) SMALLEST = sz;
					if(sz > LARGEST) LARGEST = sz;
				}
			}
		}
		
		IJ.showMessage("Found " +numParticles+" particles. Smallest: "+SMALLEST+". Largest: "+LARGEST);
	}
		
	public void run(ImageProcessor ip) {
		IJ.showMessage("Starting Plugin");
		
		if(!parseImage(ip)) {
			return;
		}
		
		initFilters(ip);
		segmentImage(ip);
	}
	
	private int dfs(int x, int y, ImageProcessor ip){
		if(x < 0 || y < 0 || x >= IMAGE_WIDTH || y >= IMAGE_HEIGHT || ip.getPixel(x, y) != 255 || visited[x][y] == 1) {
			return 0;
		}
		
		visited[x][y] = 1;
		ip.putPixel(x, y, 128);
		
		int ans = 0;
		
		for(int u = -1; u <= 1; u++) {
			for(int v = -1; v <= 1; v++) {
				if(u == 0 && v == 0)
					continue;
				ans += dfs(x+u,y+v,ip);
			}
		}
		return ans+1;
	}

	public int setup(String args, ImagePlus ip) {
		// TODO Auto-generated method stub
		IMAGE_WIDTH = ip.getWidth();
		IMAGE_HEIGHT = ip.getHeight();
		visited = new int[IMAGE_WIDTH][IMAGE_HEIGHT];
		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for(int j = 0; j < IMAGE_HEIGHT; j++) {
				visited[i][j] = 0;
			}
		}
		return DOES_8G;
	}
}