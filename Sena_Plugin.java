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
	
	private void initFilters(ImageProcessor ip) {
		IJ.showMessage("Adding Filters");
		ip.autoThreshold();
		ip.erode();
	}
	
	private void segmentImage(ImageProcessor ip) {
		IJ.showMessage("Starting image segmentation");
		int numParticles = 0;
		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for(int j = 0; j < IMAGE_HEIGHT; j++) {
				if(ip.getPixel(i, j) == 255 && visited[i][j] == 0) {
					numParticles++;
					dfs(i, j, ip);
				}
			}
		}
		
		IJ.showMessage("Found " +numParticles+" particles");
	}
	
	public void run(ImageProcessor ip) {
		IJ.showMessage("Starting Plugin");
		initFilters(ip);
		
		segmentImage(ip);
	}
	
	private void dfs(int x, int y, ImageProcessor ip){
		if(x < 0 || y < 0 || x >= IMAGE_WIDTH || y >= IMAGE_HEIGHT || ip.getPixel(x, y) != 255 || visited[x][y] == 1) {
			return;
		}
		
		visited[x][y] = 1;
		ip.putPixel(x, y, 128);
		for(int u = -1; u <= 1; u++) {
			for(int v = -1; v <= 1; v++) {
				if(u == 0 && v == 0)
					continue;
				dfs(x+u,y+v,ip);
			}
		}
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