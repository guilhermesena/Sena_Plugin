import ij.*;
import ij.process.ImageProcessor;

import java.lang.Math;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/* Auxiliary function to calculate Hu's seven invariant moments, rotate image, etc
 * Author: Guilherme Sena
 */

public class Hu_Moments {
	
	private ImageProcessor imp;
	
	//Max values of p and q. TODO: Throw exception when input >= MAX_VALUE
	private final int MAX_VALUE = 10;
	
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;

	
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
				A[i][j] = (imp.getPixel(i,j) == 128) ? BigDecimal.ONE : BigDecimal.ZERO;
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
		
		
		//Recurrent values
		area = M(0,0);
		xc = M(1,0).divide(M(0,0), RoundingMode.HALF_DOWN);
		yc = M(0,1).divide(M(0,0), RoundingMode.HALF_DOWN);
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
		
		IJ.log("p = "+p+" q="+q);
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
		memMuLine[p][q] = mu(p, q).divide(mu(0,0), RoundingMode.HALF_DOWN);
		
		IJ.log("muLine("+p+","+q+") for image "+name+" = "+memMuLine[p][q]);
		return memMuLine[p][q];
	}
	
	private BigDecimal heta (int p, int q) {
		if(memHeta[p][q] != null)
			return memHeta[p][q];
		
		memHeta[p][q] = Utils.bigSqrt(mu(p,q).pow(2).divide(
							mu(0,0).pow(2+p+q),RoundingMode.HALF_DOWN));
		
		IJ.log("heta("+p+","+q+") for image "+name+" = "+memHeta[p][q]);
		return memHeta[p][q];
		
	}

	public double getAngle() {
		BigDecimal m20 = muLine(2,0);
		BigDecimal m02 = muLine(0,2);
		double ans;
		if((muLine(2,0).subtract(muLine(0,2))).equals(BigDecimal.ZERO)) 
			ans = Math.PI/4;
		else
			ans = Math.atan(muLine(1,1).multiply(new BigDecimal(2)).divide((muLine(2,0).subtract(muLine(0,2))), RoundingMode.HALF_DOWN).doubleValue())/2.0f;
		
		IJ.log("rotation angle = "+ans);
		return ans;
	}
	
	public BigDecimal[] calculateMoments() {
		
		IJ.log("Hus invariants for image "+name+": "+I[0]+" "+I[1]+" "+I[2]+" "+I[3]);
		//TODO: The more complicated ones
		return I;
	}
}
