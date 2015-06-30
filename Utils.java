import ij.*;
import ij.gui.Roi;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

class Pair implements Comparable<Pair>{
	public int x,y;
	public Pair(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int compareTo(Pair rhs) {
		if (x != rhs.x)
			return x - rhs.x;
		
		return y - rhs.y;
	}
}

public class Utils {
	
	private static final BigDecimal SQRT_DIG = new BigDecimal(150);
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());
	
	public static Roi makeRoi(List<Pair> points) {
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

	private static BigDecimal sqrtNewtonRaphson  (BigDecimal c, BigDecimal xn, BigDecimal precision){
	    BigDecimal fx = xn.pow(2).add(c.negate());
	    BigDecimal fpx = xn.multiply(new BigDecimal(2));
	    BigDecimal xn1 = fx.divide(fpx,2*SQRT_DIG.intValue(),RoundingMode.HALF_DOWN);
	    xn1 = xn.add(xn1.negate());
	    BigDecimal currentSquare = xn1.pow(2);
	    BigDecimal currentPrecision = currentSquare.subtract(c);
	    currentPrecision = currentPrecision.abs();
	    if (currentPrecision.compareTo(precision) <= -1){
	        return xn1;
	    }
	    return sqrtNewtonRaphson(c, xn1, precision);
	}

	public static BigDecimal bigSqrt(BigDecimal c){
	    return sqrtNewtonRaphson(c,new BigDecimal(1),new BigDecimal(1).divide(SQRT_PRE));
	}
}
