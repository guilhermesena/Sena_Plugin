import ij.*;
import ij.gui.Roi;

import java.util.List;

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
	public static Roi makeRoi(List<Pair> points) {
		int minX = 2000000000;
		int maxX = -1;
		int minY = 2000000000;
		int maxY = -1;
		
		for(Pair p: points) {
			if (p.x < minX) minX = p.x;
			if (p.y < minY) minY = p.y;
			if (p.x > maxX) maxX = p.x;
			if (p.y > maxY) maxY = p.y;
		}
		
		return new Roi(minX, minY, maxX-minX, maxY-minY);
		
	}

}
