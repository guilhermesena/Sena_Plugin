import java.awt.*;

import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

/*Plugin to automatically measure data from CLSM
 * Author: Guilherme Sena
 */

public class Sena_Plugin implements PlugInFilter {
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;
	private int [][] visited;
	
	private boolean parseImage(ImageProcessor ip) {
		
		IJ.log("Verifying if stack is correctly parsed");
		
		ImagePlus imp = IJ.getImage();
		/*if (imp.getStackSize()==1) {
			IJ.error("Stack required"); 
			return false;
		}*/
		
		
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
		IJ.log("Adding Filters");
		
		IJ.log("Adding Kalman filter");
		Kalman_Stack_Filter ksf = new Kalman_Stack_Filter();
		ksf.run("");
		
		IJ.log("Auto Thresholding...");
		ip.autoThreshold();
		
		IJ.log("Eroding...");
		ip.erode();
		
	}
	
	private void segmentImage(ImageProcessor ip) {
		IJ.log("Starting image segmentation");
		int numParticles = 0;
		int MIN_SIZE = 80;
		
		int SMALLEST = Integer.MAX_VALUE;
		int LARGEST = -1;
		
		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for(int j = 0; j < IMAGE_HEIGHT; j++) {
				if(ip.getPixel(i, j) == 255 && visited[i][j] == 0) {
					
					List<Pair> points = new ArrayList<Pair>();
					IJ.log("Starting dfs at point "+i+" "+j);
					bfs(i, j, ip, points);
					if(points.size() < MIN_SIZE)
						continue;
					
					numParticles++;
					IJ.getImage().setRoi(Utils.makeRoi(points));
					IJ.getProcessor().crop();
					IJ.showMessage("Can you see the ROI in the image?");
				}
			}
		}
		
		IJ.log("Found " +numParticles+" particles. Smallest: "+SMALLEST+". Largest: "+LARGEST);
	}
		
	public void run(ImageProcessor ip) {
		IJ.log("Starting Plugin");
		
		if(!parseImage(ip)) 
			return;
		
		//initFilters(ip);
		segmentImage(ip);
	}
	
	private boolean isValidAndUnvisited(int x, int y, ImageProcessor ip) {
		if(x < 0 || y < 0 || x >= IMAGE_WIDTH || y >= IMAGE_HEIGHT || ip.getPixel(x, y) != 255 || visited[x][y] == 1)
			return false;
		
		return true;
	}
	
	private void bfs(int x, int y, ImageProcessor ip, List<Pair> points){
		
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(x+IMAGE_WIDTH*y);
		
		visited[x][y] = 1;
		ip.putPixel(x,y,128);
		while(!queue.isEmpty()) {
			int cur = queue.remove();
			int cx = cur%IMAGE_WIDTH;
			int cy = cur/IMAGE_WIDTH;
					
			for(int u = -1; u <= 1; u++) {
				for(int v = -1; v <= 1; v++) {
					if(u == 0 && v == 0)
						continue;
					
					if(isValidAndUnvisited(cx+u, cy+v, ip)) {
						visited[cx+u][cy+v] = 1;
						ip.putPixel(cx+u, cy+v, 128);
						points.add(new Pair(cx+u, cy+v));
						queue.add(cx+u+IMAGE_WIDTH*(cy+v));
					}
				}
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