package brickpop;

import java.util.*;
import java.util.Map.Entry;

public class Solver {
	State ori;
	State best;
	public Solver(int[][] grid) {
		ori = new State(grid);
		best = ori;
	}
	public ArrayList<Pair> solve() {
		ArrayList<State> poss = new ArrayList<State>();
		poss.add(ori);
		int iter = 0;
		while (!poss.isEmpty()) {
			ArrayList<State> future = new ArrayList<State>();
			for (int j = 0; j < poss.size(); j++) {
				State cur = poss.get(j);
				ArrayList<Pair> moves = cur.getMoves();
//				System.out.println("moves: " + moves.size());
				for (Pair m : moves) {
					State nxt = cur.doMove(m);
					if (nxt.done) {
//						System.out.println("Found: " + nxt.score);
						if (nxt.score > best.score) {
							best = nxt;
						}
					} else {
						nxt.getHeuristic();
						if (nxt.estScore > -1e8 && nxt.capScore > best.score) {
							future.add(nxt);
						}
					}
				}
			}
			Collections.sort(future);
			poss = new ArrayList<State>();
			// Real hacky dedupe
			HashSet<Long> vis = new HashSet<Long>();
			for (State s : future) {
				long h = s.getHash();
				if (vis.contains(h)) continue;
				vis.add(h);
				poss.add(s);
				if (poss.size() >= 5000) {
					break;
				}
			}
			System.out.println("Iter: " + iter++ + " len: " + poss.size());
			for (State s : poss.subList(0, Math.min(2, poss.size()))) {
				System.out.println(s);
			}
		}
		ArrayList<Pair> res = best.listMoves;
		System.out.println("Best: " + best.score);

		if (!best.done) {
			System.out.println("NO SOL");
			System.exit(1);
		}
		return res;
	}
}

class State implements Comparable<State> {
	public static final double SINGLETON_SCORE = -50;
	public static final double COMPONENT_SCORE = -3;
	public static final double SCORE_MULT = 0.6;
	public static final double POTENTIAL_MULT = 0.4;
	public static final double DEAD_SCORE = -1e9;

	int[][] grid;
	int n;
	int m;
	boolean done;
	int score;
	double estScore;
	int capScore;
	ArrayList<Pair> listMoves;
	State(int[][] _grid) {
		grid = new int[_grid.length][];
		for (int i = 0; i < _grid.length; i++) {
			grid[i] = Arrays.copyOf(_grid[i], _grid[i].length);
		}
		n = grid.length;
		m = grid[0].length;
		done = false;
		score = 0;
		estScore = 0;
		capScore = 0;
		listMoves = new ArrayList<Pair>();
	}
	@Override
	public String toString() {
		String res = "";
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				res += grid[i][j];
				res += " ";
			}
			res += '\n';
		}
		res += score;
		res += '\n';
		res += estScore;
		res += '\n';
		res += capScore;
		res += '\n';
		return res;
	}
	State(State oth) {
		grid = new int[oth.grid.length][];
		for (int i = 0; i < oth.grid.length; i++) {
			grid[i] = Arrays.copyOf(oth.grid[i], oth.grid[i].length);
		}
		n = grid.length;
		m = grid[0].length;
		done = oth.done;
		score = oth.score;
		estScore = oth.estScore;
		listMoves = new ArrayList<Pair>(oth.listMoves);
	}
	public int compareTo(State oth) {
		return -Double.compare(estScore, oth.estScore);
	}
	private static final int[] mx = new int[]{-1, 0, 1, 0};
	private static final int[] my = new int[]{0, -1, 0, 1};
	ArrayList<Pair> getMoves() {
		boolean[][] vis = new boolean[n][m];
		ArrayList<Pair> res = new ArrayList<Pair>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (vis[i][j] || grid[i][j] == 0) continue;
				int cur = grid[i][j];
				Queue<Pair> q = new LinkedList<Pair>();
				q.add(new Pair(i, j));
				vis[i][j] = true;
				int size = 0;
				while (!q.isEmpty()) {
					Pair p = q.poll();
					size++;
					for (int k = 0; k < 4; k++) {
						Pair nxt = new Pair(p.x + mx[k], p.y + my[k]);
						if (nxt.x < 0 || nxt.x >= n || nxt.y < 0 || nxt.y >= m) continue;
						if (vis[nxt.x][nxt.y] || grid[nxt.x][nxt.y] != cur) continue;
						vis[nxt.x][nxt.y] = true;
						q.add(nxt);
					}
				}
				if (size > 1) {
					res.add(new Pair(i, j));
				}
			}
		}
		return res;
	}
	void getHeuristic() {
		estScore = score;
		capScore = score;

		boolean[][] vis = new boolean[n][m];
		HashMap<Integer, Integer> colors = new HashMap<Integer, Integer>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (grid[i][j] == 0) continue;
				int cur = grid[i][j];
				colors.putIfAbsent(cur, 0);
				colors.put(cur, colors.get(cur)+1);

				if (vis[i][j]) continue;
				Queue<Pair> q = new LinkedList<Pair>();
				q.add(new Pair(i, j));
				vis[i][j] = true;
				int size = 0;
				while (!q.isEmpty()) {
					Pair p = q.poll();
					size++;
					for (int k = 0; k < 4; k++) {
						Pair nxt = new Pair(p.x + mx[k], p.y + my[k]);
						if (nxt.x < 0 || nxt.x >= n || nxt.y < 0 || nxt.y >= m) continue;
						if (vis[nxt.x][nxt.y] || grid[nxt.x][nxt.y] != cur) continue;
						vis[nxt.x][nxt.y] = true;
						q.add(nxt);
					}
				}
				if (size == 1) {
					estScore += SINGLETON_SCORE;
				} else {
					estScore += COMPONENT_SCORE;
				}
				estScore += size * (size - 1) * SCORE_MULT;
			}
		}
		for (Entry<Integer, Integer> e : colors.entrySet()) {
			int count = e.getValue();
			if (count == 1) {
				estScore += DEAD_SCORE;
				capScore += DEAD_SCORE;
			}
			estScore += count * (count - 1) * POTENTIAL_MULT;
			capScore += count * (count - 1);
		}
	}
	State doMove(Pair start) {
		State res = new State(this);
		int i = start.x;
		int j = start.y;
		int cur = grid[i][j];

		Queue<Pair> q = new LinkedList<Pair>();
		q.add(new Pair(i, j));
		res.grid[i][j] = 0;
		int size = 0;
		while (!q.isEmpty()) {
			Pair p = q.poll();
			size++;
			for (int k = 0; k < 4; k++) {
				Pair nxt = new Pair(p.x + mx[k], p.y + my[k]);
				if (nxt.x < 0 || nxt.x >= n || nxt.y < 0 || nxt.y >= m) continue;
				if (res.grid[nxt.x][nxt.y] != cur) continue;
				res.grid[nxt.x][nxt.y] = 0;
				q.add(nxt);
			}
		}
		assert(size > 1);
		res.score += size * (size - 1);
		res.reduce();
		res.listMoves.add(start);
		return res;
	}
	void reduce() {
		for (int i = 0; i < grid.length; i++) {
			int k = grid[i].length-1;
			for (int j = grid[i].length-1; j >= 0; j--) {
				if (grid[i][j] != 0) {
					int tmp = grid[i][k];
					grid[i][k] = grid[i][j];
					grid[i][j] = tmp;
					k--;
				}
			}
		}
		int k = 0;
		for (int i = 0; i < grid.length; i++) {
			boolean ok = false;
			for (int j = 0; j < grid[i].length; j++) {
				if (grid[i][j] != 0) {
					ok = true;
				}
			}
			if (ok) {
				int[] tmp = grid[i];
				grid[i] = grid[k];
				grid[k] = tmp;
				k++;
			}
		}
		done = (k == 0);
		return;
	}
	long getHash() {
		long res = 0;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				res = res * 107 + grid[i][j] + 1;
			}
			res *= 109;
		}
		return res;
	}
}
