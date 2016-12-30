package brickpop;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

public class Capture {
	private static final int backColor = 16247772;
	BufferedImage image = null;
	Map<Integer, Integer> xCoords;
	Map<Integer, Integer> yCoords;
	public Capture() {
		
	}
	public Pair topCoords() {
		return new Pair(xCoords.get(0)+20, yCoords.get(0)-100);
	}
	public Pair mapCoords(Pair p) {
		return new Pair(xCoords.get(p.x)+10, yCoords.get(p.y)+10);
	}
	public void getWholeScreen() {
		try {
			image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			ImageIO.write(image, "png", new File("screenshot.png"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private TreeMap<Integer, ArrayList<Integer>> merge(TreeMap<Integer, ArrayList<Integer>> ori) {
		int p = -999;
		ArrayList<Integer> plist = null;
		TreeMap<Integer, ArrayList<Integer>> res = new TreeMap<Integer, ArrayList<Integer>>();
		for (Entry<Integer, ArrayList<Integer>> e : ori.entrySet()) {
			int cur = e.getKey();
			if (plist != null && cur - p <= 2) {
				plist.addAll(e.getValue());
			} else {
				plist = new ArrayList<Integer>();
				plist.addAll(e.getValue());
				res.put(cur, plist);
			}
			p = cur;
		}
		return res;
	}
	private TreeMap<Integer, ArrayList<Integer>> align(TreeMap<Integer, ArrayList<Integer>> ori) {
		ArrayList<Integer> keys = new ArrayList<Integer>(ori.keySet());
		int common = 0;
		{
			ArrayList<Integer> diffs = new ArrayList<Integer>();
			int p = -999;
			for (int ind : keys) {
				if (p >= 0) {
					int d = ind - p;
					diffs.add(d);
				}
				p = ind;
			}
			Collections.sort(diffs);
			int cnt = 0;
			int max = 0;
			int pdiff = 0;
			for (int i = 0; i < diffs.size(); i++) {
				if (diffs.get(i) - diffs.get(pdiff) > 2) {
					cnt = 1;
					pdiff = i;
				} else {
					cnt++;
					if (cnt > max) {
						cnt = max;
						// median
						common = diffs.get((i + pdiff)/2);
					}
				}
			}
		}
		assert common != 0;
		System.out.println("common: " + common);
		int best = 0;
		int besti = -1;
		for (int i = 0; i < keys.size(); i++)
		{
			int cnt = 0;
			int p = keys.get(i);
			for (int j = i+1; j < keys.size(); j++) {
				int d = keys.get(j) - p;
				int dist = Math.abs(d - common);
				if (dist <= 2) {
					p = keys.get(j);
					cnt++;
				} else if (d > common) {
					break;
				}
			}
			if (cnt > best) {
				best = cnt;
				besti = i;
			}
		}
		System.out.println("Best align: " + best + " " + besti);
		assert besti >= 0;
		TreeMap<Integer, ArrayList<Integer>> res = new TreeMap<Integer, ArrayList<Integer>>();
		int p = keys.get(besti) - common;
		for (int j = besti; j < keys.size(); j++) {
			int d = keys.get(j) - p;
			int dist = Math.abs(d - common);
			if (dist <= 2) {
				res.put(keys.get(j), ori.get(keys.get(j)));
				p = keys.get(j);
			} else if (d > common) {
				break;
			}
		}
		return res;
	}
	private static final int[] mx = new int[]{-1, 0, 1, 0};
	private static final int[] my = new int[]{0, -1, 0, 1};
	public int[][] convertGrid() {
		int w = image.getWidth();
		int h = image.getHeight();
		System.out.println(w + " " + h);
		boolean[][] vis = new boolean[w][h];
		TreeMap<Integer, ArrayList<Integer>> xMap = new TreeMap<Integer, ArrayList<Integer>>();
		TreeMap<Integer, ArrayList<Integer>> yMap = new TreeMap<Integer, ArrayList<Integer>>();
		TreeMap<Pair, Integer> ptMap = new TreeMap<Pair, Integer>();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (vis[i][j]) continue;
				int first = image.getRGB(i, j)&0xffffff;
				if (first != backColor) {
					int size = 0;
					Queue<Pair> q = new LinkedList<Pair>();
					Pair start = new Pair(i, j);
					q.add(start);
					vis[i][j] = true;
					int mini = 99999, maxi = -99999;
					int minj = 99999, maxj = -99999;
					while (!q.isEmpty()) {
						Pair cur = q.poll();
						size++;
						mini = Math.min(mini, cur.x);
						maxi = Math.max(maxi, cur.x);
						minj = Math.min(minj, cur.y);
						maxj = Math.max(maxj, cur.y);
						for (int k = 0; k < 4; k++) {
							Pair nxt = new Pair(cur.x + mx[k], cur.y + my[k]);
							if (nxt.x < 0 || nxt.x >= w || nxt.y < 0 || nxt.y >= h) continue;
							if (vis[nxt.x][nxt.y]) continue;
							if ((image.getRGB(nxt.x, nxt.y)&0xffffff) != backColor) {
								q.add(nxt);
								vis[nxt.x][nxt.y] = true;
							}
						}
					}
					
					int middle = image.getRGB((mini + maxi)/2, (minj + maxj)/2)&0xffffff;
//					System.out.println(size + " " + middle);
					xMap.putIfAbsent(i, new ArrayList<Integer>());
					xMap.get(i).add(middle);
					yMap.putIfAbsent(j, new ArrayList<Integer>());
					yMap.get(j).add(middle);
					ptMap.putIfAbsent(start, middle);
				}
			}
		}
		xMap = merge(xMap);
		yMap = merge(yMap);
		xMap = align(xMap);
		yMap = align(yMap);
		
		TreeMap<Integer, Integer> xBack = new TreeMap<Integer, Integer>();
		xCoords = new HashMap<Integer, Integer>();
		for (Entry<Integer, ArrayList<Integer>> x : xMap.entrySet()) {
			xBack.put(x.getKey(), xCoords.size());
			xCoords.put(xCoords.size(), x.getKey());
			System.out.println("x " + x.getKey());
		}
		TreeMap<Integer, Integer> yBack = new TreeMap<Integer, Integer>();
		yCoords = new HashMap<Integer, Integer>();
		for (Entry<Integer, ArrayList<Integer>> y : yMap.entrySet()) {
			yBack.put(y.getKey(), yCoords.size());
			yCoords.put(yCoords.size(), y.getKey());
			System.out.println("y " + y.getKey());
		}
		if (xCoords.size() == 0) {
			System.out.println("FAIL");
			System.exit(1);
		}
		System.out.println(xCoords.size());
		System.out.println(yCoords.size());
		int[][] grid = new int[xCoords.size()][yCoords.size()];
		TreeMap<Integer, Integer> colorMap = new TreeMap<Integer, Integer>();
		// Hacky af
		for (Entry<Pair, Integer> e : ptMap.entrySet()) {
			Pair cur = e.getKey();
			int x, y;
			try {
				x = xMap.floorKey(cur.x);
				y = yMap.floorKey(cur.y);
			} catch (NullPointerException n) {
				continue;
			}
//			System.out.println(x + " " + y + " " + e.getValue());
			if (cur.x - x <= 3 && cur.y - y <= 3 && xBack.containsKey(x) && yBack.containsKey(y)) {
				int color = e.getValue();
				colorMap.putIfAbsent(color, colorMap.size()+1);
				int lx = xBack.get(x);
				int ly = yBack.get(y);
				if (ly < 0 || ly >= grid[lx].length) continue;
				grid[lx][ly] = colorMap.get(color);
			}
		}
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				System.out.print(grid[i][j]);
				System.out.print(' ');
			}
			System.out.println();
		}
		return grid;
	}
	
}
