package brickpop;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.*;

public class Main {
	public static void main(String[] args) {
		while (true) {
			Capture c = new Capture();
			c.getWholeScreen();
			int[][] t = c.convertGrid();
			System.out.println(t.length);
			System.out.println(t[0].length);
			assert t.length >= 4;
			assert t[0].length >= 4;
			assert t.length <= 10;
			assert t[0].length <= 10;
			for (int[] row : t) {
				boolean f = false;
				for (int a : row) {
					if (f) {
						assert a != 0;
					} else if (a != 0) {
						f = true;
					}
				}
			}
			
			Solver s = new Solver(t);
			ArrayList<Pair> moves = s.solve();
			System.out.println("#moves: " + moves.size());
			try {
				Robot bot = new Robot();
	
				System.out.println("CLICKING");
				Thread.sleep(100);
				int mask = InputEvent.BUTTON1_MASK;
				{
					Pair click = c.topCoords();
					bot.mouseMove(click.x, click.y);
					Thread.sleep(50);
					bot.mousePress(mask);
					Thread.sleep(50);
					bot.mouseRelease(mask);
					Thread.sleep(100);
				}
				for (Pair p : moves) {
					Pair click = c.mapCoords(p);
//					System.out.println(click.x + " " + click.y);
					bot.mouseMove(click.x, click.y);
					Thread.sleep(50);
					bot.mousePress(mask);
					Thread.sleep(50);
					bot.mouseRelease(mask);
					Thread.sleep(1800);
				}
				Thread.sleep(7000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//			break;
		}
	}
}
