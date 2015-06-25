import ij.*;
import ij.process.ImageProcessor;

import java.lang.Math;

/* Auxiliary function to calculate Hu's seven invariant moments, rotate image, etc
 * Author: Guilherme Sena
 */

public class Hu_Moments {
	
	private ImageProcessor imp;
	
	//Max values of p and q. TODO: Throw exception when input >= MAX_VALUE
	private final int MAX_VALUE = 10;
	
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;

	
	private double [][] A; //TODO: Convert to bits for optimization
	private double area;
	private double xc, yc;
	private double angle;
	private double [] I;
	
	
	//Memorize calculations
	private double [][] memM;
	private double [][] memMu;
	private double [][] memMuLine;
	private double [][] memHeta;
	
	public Hu_Moments (ImageProcessor imp) {
		this.imp = imp;
		this.IMAGE_WIDTH = imp.getWidth();
		this.IMAGE_HEIGHT = imp.getHeight();
		A = new double[IMAGE_WIDTH] [IMAGE_HEIGHT];
		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for (int j = 0; j < IMAGE_HEIGHT; j++) {
				A[i][j] = (double) imp.getPixel(i, j);
			}
		}
		
		//LUT
		memM = new double[MAX_VALUE][MAX_VALUE];
		memMu = new double[MAX_VALUE][MAX_VALUE];
		memMuLine = new double[MAX_VALUE][MAX_VALUE];
		memHeta = new double[MAX_VALUE][MAX_VALUE];
		for(int i = 0; i < MAX_VALUE; i++) {
			for(int j = 0; j < MAX_VALUE; j++) {
				memM[i][j] = -1.0;
				memMu[i][j] = -1.0;
				memMuLine[i][j] = -1.0;
				memHeta[i][j] = -1.0;
			}
		}
		
		
		//Recurrent values
		area = M(0,0);
		xc = M(1,0)/M(0,0);
		yc = M(0,1)/M(0,0);
		I = new double[7];
		angle = getAngle();
		
	}
	
	private double M(int p, int q) {
		
		if(memM[p][q] >= 0.0f)
			return memM[p][q];
		
		double dp = (double) p;
		double dq = (double) q;
		int ans = 0;
		for(int x = 0; x < IMAGE_WIDTH; x++) {
			for(int y = 0; y < IMAGE_HEIGHT; y++) {
				ans += Math.pow((double) x, p)*Math.pow((double) y, q)*A[x][y];
			}
		}
		
		memM[p][q] = ans;
		
		return ans;
	}
	
	private double mu(int p, int q) {
		
		if(memMu[p][q] >= 0.0f) {
			return memMu[p][q];
		}
		
		double dp = (double) p;
		double dq = (double) q;
		int ans = 0;
		for(int x = 0; x < IMAGE_WIDTH; x++) {
			for(int y = 0; y < IMAGE_HEIGHT; y++) {
				ans += Math.pow((double)x - xc, dp)*Math.pow((double)y - yc, dq)*A[x][y];
			}
		}
		
		memMu[p][q] = ans;
		
		return ans;
	}
	
	private double muLine(int p, int q) {
		if(memMuLine[p][q] >= 0.0f) {
			return memMuLine[p][q];
		}
		memMuLine[p][q] = mu(p, q)/mu(0,0);
		
		return memMuLine[p][q];
	}
	
	private double heta (int p, int q) {
		if(memHeta[p][q] >= 0.0f)
			return memHeta[p][q];
		
		memHeta[p][q] = mu(p,q) / Math.pow(mu(0,0),1.0f + ((double)p+(double)q)/(2.0f));
		return memHeta[p][q];
		
	}

	private double getAngle() {
		double m20 = mu(2,0);
		double m02 = mu(0,2);
		if(m20 == m02) 
			return Math.PI/4;
		
		return Math.atan(2*mu(1,1)/(mu(2,0)-mu(0,2)))/2.0f;
	}
	
	private void calculateMoments() {
		I[0] = heta(2,0)+heta(0,2);
		I[1] = (heta(2,0)-heta(0,2))*(heta(2,0)-heta(0,2))+4*heta(1,1)*heta(1,1);
		I[2] = (heta(3,0)-3*heta(1,2))*(heta(3,0)-3*heta(1,2))+(3*heta(2,1)-heta(0,3))*(3*heta(2,1)-heta(0,3));
		I[3] = (heta(3,0)+heta(1,2))*(heta(3,0)+heta(1,2))+(heta(2,1)+heta(0,3))*(heta(2,1)+heta(0,3));
		
		//TODO: The more complicated ones
	}
}
