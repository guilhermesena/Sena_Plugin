import ij.*;
import ij.process.ImageProcessor;

import java.lang.Math;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import java.lang.StringBuffer;

/* Auxiliary function to calculate Hu's seven invariant moments, rotate image, etc
 * Author: Guilherme Sena
 */

public class Hu_Moments {
	
	private ImageProcessor imp;
	
	private final int DIVISION_PRECISION = 30;
	
	//Max values of p and q. TODO: Throw exception when input >= MAX_VALUE
	private final int MAX_VALUE = 10;
	
	//Current image's dimensions
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;

	//Recurrent values
	private BigDecimal [][] A; //TODO: Convert to bits for optimization
	private BigDecimal area;
	private BigDecimal xc, yc;
	private double angle;
	private BigDecimal [] I;
	
	
	//Memorize calculations
	private BigDecimal [][] memM;
	private BigDecimal [][] memMu;
	private BigDecimal [][] memMuLine;
	private BigDecimal [][] memHeta;
	
	//Name for debugging
	private String name;
	
	public Hu_Moments (ImageProcessor imp, String name) {
		this.imp = imp;
		this.name= name;
		
		this.IMAGE_WIDTH = imp.getWidth();
		this.IMAGE_HEIGHT = imp.getHeight();
		
		A = new BigDecimal[IMAGE_WIDTH] [IMAGE_HEIGHT];
		
		IJ.log("width = "+IMAGE_WIDTH+", height = "+IMAGE_HEIGHT);
		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for (int j = 0; j < IMAGE_HEIGHT; j++) {
				A[i][j] = (imp.getPixel(i,j) > 0) ? new BigDecimal(255) : BigDecimal.ZERO;
			}
		}
		
		//LUT
		memM = new BigDecimal[MAX_VALUE][MAX_VALUE];
		memMu = new BigDecimal[MAX_VALUE][MAX_VALUE];
		memMuLine = new BigDecimal[MAX_VALUE][MAX_VALUE];
		memHeta = new BigDecimal[MAX_VALUE][MAX_VALUE];
		for(int i = 0; i < MAX_VALUE; i++) {
			for(int j = 0; j < MAX_VALUE; j++) {
				memM[i][j] = null;
				memMu[i][j] = null;
				memMuLine[i][j] = null;
				memHeta[i][j] = null;
			}
		}
		
		
		//Recurrent value calculation
		area = M(0,0);
		xc = M(1,0).divide(M(0,0), 3, RoundingMode.HALF_DOWN);
		yc = M(0,1).divide(M(0,0), 3, RoundingMode.HALF_DOWN);
		IJ.log("centroid = ("+xc+","+yc+")");
		I = new BigDecimal[7];
		angle = getAngle();
		
	}
	
	private BigDecimal M(int p, int q) {
		
		if(memM[p][q] != null)
			return memM[p][q];
		
		BigDecimal ans = BigDecimal.ZERO;
		for(int x = 0; x < IMAGE_WIDTH; x++) {
			for(int y = 0; y < IMAGE_HEIGHT; y++) {
				ans = ans.add(
					(new BigDecimal(x)).pow(p).multiply(
					(new BigDecimal(y)).pow(q)).multiply(A[x][y])
				);
			}
		}
		
		memM[p][q] = ans;
		IJ.log("M("+p+","+q+") for image "+name+" = "+memM[p][q].toEngineeringString());
		return ans;
	}
	
	private BigDecimal mu(int p, int q) {
		
		if(memMu[p][q] != null) {
			return memMu[p][q];
		}
		
		BigDecimal ans = BigDecimal.ZERO;
		for(int x = 0; x < IMAGE_WIDTH; x++) {
			for(int y = 0; y < IMAGE_HEIGHT; y++) {
				ans = ans.add (
						(new BigDecimal(x).subtract(xc).pow(p)).multiply(
								new BigDecimal(y).subtract(yc).pow(q)
						).multiply(A[x][y])
					);
			}
		}
		
		memMu[p][q] = ans;
		IJ.log("mu("+p+","+q+") for image "+name+" = "+memMu[p][q]);
		return ans;
	}
	
	private BigDecimal muLine(int p, int q) {
		if(memMuLine[p][q] != null) {
			return memMuLine[p][q];
		}
		memMuLine[p][q] = mu(p, q).divide(mu(0,0), DIVISION_PRECISION, RoundingMode.HALF_DOWN);
		
		IJ.log("muLine("+p+","+q+") for image "+name+" = "+memMuLine[p][q]);
		return memMuLine[p][q];
	}
	
	private BigDecimal heta (int p, int q) {
		if(memHeta[p][q] != null)
			return memHeta[p][q];
		
		memHeta[p][q] = Utils.bigSqrt(mu(p,q).pow(2).divide(
							mu(0,0).pow(2+p+q), DIVISION_PRECISION, RoundingMode.HALF_DOWN));
		
		IJ.log("heta("+p+","+q+") for image "+name+" = "+memHeta[p][q]);
		return memHeta[p][q];
		
	}

	public double getAngle() {
		BigDecimal m20 = muLine(2,0);
		BigDecimal m02 = muLine(0,2);
		
		BigDecimal denom = muLine(2,0).subtract(muLine(0,2)).setScale(DIVISION_PRECISION, RoundingMode.HALF_DOWN);
		double ans;
		
		if(denom.equals(new BigDecimal("0E-"+DIVISION_PRECISION))) {
			IJ.log("Image "+name+" is already in its minimum axis");
			ans = Math.PI/4;
		}
		else
			ans = Math.atan(muLine(1,1).multiply(new BigDecimal(2)).divide(denom, DIVISION_PRECISION, RoundingMode.HALF_DOWN).doubleValue())/2.0f;
		
		ans = ans*180/Math.PI;
		IJ.log("rotation angle = "+ans);
		return ans;
	}
	
	public BigDecimal[] calculateMoments() {
		
		int NUM_DIGITS = 50;
		
		I[0] = heta(2,0).add(heta(0,2));
		I[1] = heta(2,0).subtract(heta(0,2)).pow(2).add(heta(1,1).multiply(new BigDecimal(4)));
		I[2] = heta(3,0).subtract(heta(1,2).multiply(new BigDecimal(3))).pow(2).add(heta(2,1).multiply(new BigDecimal(3)).subtract(heta(0,3)).pow(2));
		I[3] = heta(3,0).add(heta(1,2)).pow(2).add(heta(2,1).add(heta(0,3)).pow(2));
		I[4] = heta(3,0).subtract(heta(1,2).multiply(new BigDecimal(3))).multiply(heta(3,0).add(heta(1,2))).multiply(
					heta(3,0).add(heta(1,2)).pow(2).subtract(heta(2,1).add(heta(0,3)).pow(2).multiply(new BigDecimal(3)))
				).add (
					heta(2,1).multiply(new BigDecimal(3)).subtract(heta(0,3)).multiply(heta(2,1).add(heta(0,3))).multiply(
							heta(3,0).add(heta(1,2)).pow(2).multiply(new BigDecimal(3)).subtract(heta(2,1).add(heta(0,3)).pow(2))
					)
				);
		
		I[5] = heta(2,0).subtract(heta(0,2)).multiply(heta(3,0).add(heta(1,2)).pow(2).subtract(heta(2,1).add(heta(0,3).pow(2)))).add((new BigDecimal(4)).multiply(heta(1,1)).multiply(heta(3,0).add(heta(1,2))).multiply(heta(2,1).add(heta(0,3))));
		
		//Debug
		StringBuffer logAns = new StringBuffer("Hus invariants for image "+name+": \n");
		for(int i = 0; i < 6; i++)
			logAns.append("I["+i+"]="+I[i].setScale(NUM_DIGITS, BigDecimal.ROUND_DOWN)+", \n");
		
		IJ.log(logAns.toString());
		//TODO: The more complicated ones
		return I;
	}
}
