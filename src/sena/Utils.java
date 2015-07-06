package sena;
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

}
