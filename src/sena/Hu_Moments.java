package sena;

import ij.*;
import ij.gui.Roi;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.lang.Math;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.lang.StringBuffer;


public class Hu_Moments extends Logger{
	private static final String PROCEDURE_NAME = "Hu Moments";
	
	private final int ARITHMETIC_PRECISION = 350;
	private final BigDecimal CONST_ZERO = new BigDecimal("0E-"+ARITHMETIC_PRECISION);
	private final BigDecimal CONST_TWO = new BigDecimal(2);
	private final BigDecimal CONST_THREE = new BigDecimal(3);
	private final BigDecimal CONST_FOUR = new BigDecimal(4);
	private final BigDecimal CONST_180 = new BigDecimal(180);
	private final BigDecimal CONST_45 = new BigDecimal(Math.PI/4);
	private final BigDecimal CONST_255 = new BigDecimal(255);
	private final BigDecimal CONST_PI = new BigDecimal(Math.PI);
	
	
	//Max values of p and q. TODO: Throw exception when input >= MAX_VALUE
	private final int MAX_VALUE = 10;
	
	//Current image's dimensions
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;

	//Recurrent values
	private BigDecimal [][] A; //TODO: Convert to bits for optimization
	private BigDecimal area;
	private BigDecimal xc, yc;
	private BigDecimal angle;
	private BigDecimal [] I;
	
	
	//Memorize calculations
	private BigDecimal [][] memM;
	private BigDecimal [][] memMu;
	private BigDecimal [][] memMuLine;
	private BigDecimal [][] memHeta;
	
	//Name for debugging
	private String name;
	
	public Hu_Moments (ImageProcessor imp, String name) {
		super(PROCEDURE_NAME);
		this.name= name;
		this.IMAGE_WIDTH = imp.getWidth();
		this.IMAGE_HEIGHT = imp.getHeight();
		
		A = new BigDecimal[IMAGE_WIDTH] [IMAGE_HEIGHT];
		
		log("width = "+IMAGE_WIDTH+", height = "+IMAGE_HEIGHT);
		for(int i = 0; i < IMAGE_WIDTH; i++) {
			for (int j = 0; j < IMAGE_HEIGHT; j++) {
				A[i][j] = (imp.getPixel(i,j) > 0) ? CONST_255 : BigDecimal.ZERO;
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
		log("centroid = ("+xc+","+yc+")");
		I = new BigDecimal[7];
		angle = getAngle();
		log("Area = "+area+", (xc, yc) = ("+xc+", "+yc+"), angle = "+angle);
	}
	
	private BigDecimal M(int p, int q) {
		
		if(memM[p][q] != null)
			return memM[p][q];
		
		BigDecimal ans = BigDecimal.ZERO;
		for(int x = 0; x < IMAGE_WIDTH; x++) {
			for(int y = 0; y < IMAGE_HEIGHT; y++) {
				double xCoord = x+0.5;
				double yCoord = y+0.5;
				ans = ans.add(
					(new BigDecimal(xCoord)).pow(p).multiply(
					(new BigDecimal(yCoord)).pow(q)).multiply(A[x][y])
				);
			}
		}
		
		memM[p][q] = ans;
		log("M("+p+","+q+") for image "+name+" = "+memM[p][q].toEngineeringString());
		return ans;
	}
	
	private BigDecimal mu(int p, int q) {
		
		if(memMu[p][q] != null) {
			return memMu[p][q];
		}
		
		BigDecimal ans = BigDecimal.ZERO;
		for(int x = 0; x < IMAGE_WIDTH; x++) {
			for(int y = 0; y < IMAGE_HEIGHT; y++) {
				double xCoord = x+0.5;
				double yCoord = y+0.5;
				ans = ans.add (
						(new BigDecimal(xCoord).subtract(xc).pow(p)).multiply(
								new BigDecimal(yCoord).subtract(yc).pow(q)
						).multiply(A[x][y])
					);
			}
		}
		
		memMu[p][q] = ans;
		log("mu("+p+","+q+") for image "+name+" = "+memMu[p][q]);
		return ans;
	}
	
	private BigDecimal muLine(int p, int q) {
		if(memMuLine[p][q] != null) {
			return memMuLine[p][q];
		}
		memMuLine[p][q] = mu(p, q).divide(mu(0,0), ARITHMETIC_PRECISION, RoundingMode.HALF_DOWN);
		
		log("muLine("+p+","+q+") for image "+name+" = "+memMuLine[p][q]);
		return memMuLine[p][q];
	}
	
	private BigDecimal heta (int p, int q) {
		if(memHeta[p][q] != null)
			return memHeta[p][q];
		
		memHeta[p][q] = BigFunctions.bigSqrt(mu(p,q).pow(2).divide(
						mu(0,0).pow(2+p+q), ARITHMETIC_PRECISION, RoundingMode.HALF_DOWN));
	
		log("heta("+p+","+q+") for image "+name+" = "+memHeta[p][q]);
		return memHeta[p][q];
	}

	public BigDecimal getAngle() {
		
		BigDecimal denom = mu(2,0).subtract(mu(0,2)).setScale(ARITHMETIC_PRECISION, RoundingMode.HALF_DOWN);
		BigDecimal ans;
		
		if(denom.equals(CONST_ZERO)) {
			log("Minimum rotation in X axis");
			ans = CONST_45;
		}
		
		else if(mu(1,1).equals(CONST_ZERO)) {
			log("Minimum rotation in Y axis");
			ans = CONST_ZERO;
		}
		else 
			ans = BigFunctions.arctan((mu(1,1).multiply(CONST_TWO).divide(denom, ARITHMETIC_PRECISION, RoundingMode.HALF_DOWN)), ARITHMETIC_PRECISION).divide(CONST_TWO);
		
		ans = ans.multiply(CONST_180).divide(CONST_PI, ARITHMETIC_PRECISION, RoundingMode.HALF_DOWN);
		log("rotation angle = "+ans);
		return ans;
	}
	
	public BigDecimal[] calculateMoments() {
		
		I[0] = heta(2,0).add(heta(0,2));
		I[1] = heta(2,0).subtract(heta(0,2)).pow(2).add(heta(1,1).multiply(CONST_FOUR));
		I[2] = heta(3,0).subtract(heta(1,2).multiply(CONST_THREE)).pow(2).add(heta(2,1).multiply(CONST_THREE).subtract(heta(0,3)).pow(2));
		I[3] = heta(3,0).add(heta(1,2)).pow(2).add(heta(2,1).add(heta(0,3)).pow(2));
		I[4] = heta(3,0).subtract(heta(1,2).multiply(CONST_THREE)).multiply(heta(3,0).add(heta(1,2))).multiply(
					heta(3,0).add(heta(1,2)).pow(2).subtract(heta(2,1).add(heta(0,3)).pow(2).multiply(CONST_THREE))
				).add (
					heta(2,1).multiply(CONST_THREE).subtract(heta(0,3)).multiply(heta(2,1).add(heta(0,3))).multiply(
							heta(3,0).add(heta(1,2)).pow(2).multiply(CONST_THREE).subtract(heta(2,1).add(heta(0,3)).pow(2))
					)
				);
		
		I[5] = heta(2,0).subtract(heta(0,2)).multiply(heta(3,0).add(heta(1,2)).pow(2).subtract(heta(2,1).add(heta(0,3).pow(2)))).add((new BigDecimal(4)).multiply(heta(1,1)).multiply(heta(3,0).add(heta(1,2))).multiply(heta(2,1).add(heta(0,3))));
		
		//Debug
		StringBuffer logAns = new StringBuffer("Hus (log) invariants for image "+name+": \n");
		for(int i = 0; i < 6; i++) {
			//if(!I[i].equals(CONST_ZERO))
			//	I[i] = BigFunctions.ln(I[i].abs(), ARITHMETIC_PRECISION).negate();
			logAns.append("I["+i+"]="+I[i].setScale(ARITHMETIC_PRECISION, BigDecimal.ROUND_DOWN)+", \n");
		}
		log(logAns.toString());
		
		//TODO: The more complicated ones
		return I;
	}
	
	//temporary calculation
	public static void calculateMomentsTemp(ImagePlus imp, ImageProcessor ip, Roi roi) {
		int measurements = Analyzer.getMeasurements(); // defined in Set Measurements dialog
		Analyzer.setMeasurements(measurements);
		double dCutoff = 0.0; // default cutoff (minimum) value for calcs
		//  (only values >= dCutoff are used)
		//  (use "0" to include all positive pixel values)
		double dFactor = 1.0; // default factor                              
		//  (multiplies pixel values prior to calculations)


		double zero = 0.0;
		double m00 = zero;
		double m10 = zero, m01 = zero;
		double m20 = zero, m02 = zero, m11 = zero;
		double m30 = zero, m03 = zero, m21 = zero, m12 = zero;
		double m40 = zero, m04 = zero, m31 = zero, m13 = zero;
		double xC=zero, yC=zero;
		double xxVar = zero, yyVar = zero, xyVar = zero;
		double xSkew = zero, ySkew = zero;
		double xKurt = zero, yKurt = zero;
		double orientation = zero, eccentricity = zero;
		double currentPixel, xCoord, yCoord;

		// Get image and ROI info
		//  Note: currently supports rectangular ROIs only
		Rectangle r = roi.getBounds();
		byte[] mask = ip.getMaskArray();
		int maskCounter = 0;

		// Compute moments of order 0 & 1

		for (int y=r.y; y<(r.y+r.height); y++) {
			for (int x=r.x; x<(r.x+r.width); x++) {
				if (mask==null || mask[maskCounter++]!=0) {
					xCoord = x-r.x+0.5; //this pixel's X calibrated coord. (e.g. cm)
					yCoord = y-r.y+0.5; //this pixel's Y calibrated coord. (e.g. cm)
					currentPixel=ip.getPixelValue(x,y);
					currentPixel=currentPixel-dCutoff;
					if (currentPixel < 0) currentPixel = zero; //gets rid of negative pixel values
					currentPixel = dFactor*currentPixel;
					/*0*/       m00+=currentPixel;
					/*1*/       m10+=currentPixel*xCoord;
					m01+=currentPixel*yCoord;
				}
			}
		}

		// Compute coordinates of centre of mass

		xC = m10/m00;
		yC = m01/m00;

		// Compute moments of orders 2, 3, 4

		// Reset index on "mask"
		maskCounter = 0;
		for (int y=r.y; y<(r.y+r.height); y++) {
			for (int x=r.x; x<(r.x+r.width); x++) {
				if (mask==null || mask[maskCounter++]!=0) {
					xCoord = x-r.x+0.5; //this pixel's X calibrated coord. (e.g. cm)
					yCoord = y-r.y+0.5; //this pixel's Y calibrated coord. (e.g. cm)
					currentPixel=ip.getPixelValue(x,y);
					currentPixel=currentPixel-dCutoff;
					if (currentPixel < 0) currentPixel = zero; //gets rid of negative pixel values
					currentPixel = dFactor*currentPixel;
					/*2*/       m20+=currentPixel*(xCoord-xC)*(xCoord-xC);
					m02+=currentPixel*(yCoord-yC)*(yCoord-yC);
					m11+=currentPixel*(xCoord-xC)*(yCoord-yC);

					/*3*/       m30+=currentPixel*(xCoord-xC)*(xCoord-xC)*(xCoord-xC);
					m03+=currentPixel*(yCoord-yC)*(yCoord-yC)*(yCoord-yC);
					m21+=currentPixel*(xCoord-xC)*(xCoord-xC)*(yCoord-yC);
					m12+=currentPixel*(xCoord-xC)*(yCoord-yC)*(yCoord-yC);

					/*4*/       m40+=currentPixel*(xCoord-xC)*(xCoord-xC)*(xCoord-xC)*(xCoord-xC);
					m04+=currentPixel*(yCoord-yC)*(yCoord-yC)*(yCoord-yC)*(yCoord-yC);
					m31+=currentPixel*(xCoord-xC)*(xCoord-xC)*(xCoord-xC)*(yCoord-yC);
					m13+=currentPixel*(xCoord-xC)*(yCoord-yC)*(yCoord-yC)*(yCoord-yC);
				}
			}
		}

		// Normalize 2nd moments & compute VARIANCE around centre of mass
		xxVar = m20/m00;
		yyVar = m02/m00;
		xyVar = m11/m00;

		// Normalize 3rd moments & compute SKEWNESS (symmetry) around centre of mass
		// source: Farrell et al, 1994, Water Resources Research, 30(11):3213-3223
		xSkew = m30 / (m00 * Math.pow(xxVar,(3.0/2.0)));
		ySkew = m03 / (m00 * Math.pow(yyVar,(3.0/2.0)));

		// Normalize 4th moments & compute KURTOSIS (peakedness) around centre of mass
		// source: Farrell et al, 1994, Water Resources Research, 30(11):3213-3223
		xKurt = m40 / (m00 * Math.pow(xxVar,2.0)) - 3.0;
		yKurt = m04 / (m00 * Math.pow(yyVar,2.0)) - 3.0;

		// Compute Orientation and Eccentricity
		// source: Awcock, G.J., 1995, "Applied Image Processing", pp. 162-165
		orientation = 0.5*Math.atan2((2.0*m11),(m20-m02));
		orientation = orientation*180./Math.PI; //convert from radians to degrees
		eccentricity = (Math.pow((m20-m02),2.0)+(4.0*m11*m11))/m00;	
		
		IJ.log("Hu moments: "
				+ "Mass="+m00+","
				+ "xC="+xC+","
				+ "yC="+yC+","
				+ "xxVar="+xxVar+","
				+ "yyVar="+yyVar+","
				+ "xyVar="+xyVar+","
				+ "xSkew="+xSkew+","
				+ "ySkew="+ySkew+","
				+ "xKurt="+xKurt+","
				+ "yKurt="+yKurt+","
				+ "orientation="+orientation+","
				+ "eccentricity="+eccentricity+","
			);

	}
}
