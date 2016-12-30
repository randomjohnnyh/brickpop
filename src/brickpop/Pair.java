package brickpop;

public class Pair implements Comparable<Pair> {
	public int x, y;
	Pair(int _x, int _y) {
		x = _x;
		y = _y;
	}
	public int compareTo(Pair oth) {
		if (x != oth.x) return Integer.compare(x, oth.x);
		return Integer.compare(y, oth.y);
	}
}