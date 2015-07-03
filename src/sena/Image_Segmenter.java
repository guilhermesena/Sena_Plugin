package sena;

import ij.IJ;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Image_Segmenter extends Logger{
	private static final String PROCEDURE_NAME="Image Segmenter";
	
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;
	private int [][] visited;
	private ImageProcessor ip;
	
	private final int VALID_PIXEL = 192;
	private final int INVALID_PIXEL = 64;
	private final int NEIGHBOR_RADIUS = 1;
	private final int MIN_SIZE = 80;
	
	
	public Image_Segmenter (ImageProcessor ip) {
		super(PROCEDURE_NAME);
		this.ip = ip;
		this.IMAGE_WIDTH = ip.getWidth();
		this.IMAGE_HEIGHT = ip.getHeight();
		visited = new int[IMAGE_WIDTH][IMAGE_HEIGHT];
		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for(int j = 0; j < IMAGE_HEIGHT; j++) {
				visited[i][j] = 0;
			}
		}
	}
	
	public List<Roi> segmentImage() {
		log("Starting image segmentation...");
		int numParticles = 0;
		
		List<Roi> ans = new ArrayList<Roi>();

		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for(int j = 0; j < IMAGE_HEIGHT; j++) {
				if(ip.getPixel(i, j) > 0 && visited[i][j] == 0) {
					List<Pair> points = bfs(i, j);
					if(points.size() < MIN_SIZE) {
						for (Pair p: points) {
							ip.putPixel(p.x, p.y, INVALID_PIXEL);
						}
						continue;
					}

					numParticles++;
					
					Roi roi = makeRoi(points);
					IJ.getImage().setRoi(roi);
					ans.add(roi);
					log("Showing ROI "+numParticles+"...");
				}
			}
		}
		log("Found "+numParticles+" particles.");
		return ans;
	}

	private boolean isValidAndUnvisited(int x, int y) {
		if(x < 0 || y < 0 || x >= IMAGE_WIDTH || y >= IMAGE_HEIGHT || ip.getPixel(x, y) == 0 || visited[x][y] == 1)
			return false;

		return true;
	}

	private List<Pair> bfs(int x, int y){
		
		List<Pair> ans = new ArrayList<Pair>();
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(x+IMAGE_WIDTH*y);

		visited[x][y] = 1;
		ip.putPixel(x, y, VALID_PIXEL);
		while(!queue.isEmpty()) {
			int cur = queue.remove();
			int cx = cur%IMAGE_WIDTH;
			int cy = cur/IMAGE_WIDTH;

			for(int u = -NEIGHBOR_RADIUS; u <= NEIGHBOR_RADIUS; u++) {
				for(int v = -NEIGHBOR_RADIUS; v <= NEIGHBOR_RADIUS; v++) {
					if(u == 0 && v == 0)
						continue;

					if(isValidAndUnvisited(cx+u, cy+v)) {
						visited[cx+u][cy+v] = 1;
						ip.putPixel(cx+u, cy+v, VALID_PIXEL);
						ans.add(new Pair(cx+u, cy+v));
						queue.add(cx+u+IMAGE_WIDTH*(cy+v));
					}
				}
			}
		}

		return ans;
	}

	private Roi makeRoi(List<Pair> points) {
		int minX = Integer.MAX_VALUE;
		int maxX = -1;
		int minY = Integer.MAX_VALUE;
		int maxY = -1;

		for(Pair p: points) {
			if (p.x < minX) minX = p.x;
			if (p.y < minY) minY = p.y;
			if (p.x > maxX) maxX = p.x;
			if (p.y > maxY) maxY = p.y;
		}

		return new Roi(minX, minY, maxX-minX+1, maxY-minY+1);

	}
}
