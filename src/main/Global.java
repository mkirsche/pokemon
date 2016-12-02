package main;

import util.FileIO;
import util.Folder;
import util.RandomUtils;

import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

// Loads and maintains game data.
public class Global {
	// Title of the window
	public static final String TITLE = "Pok\u00e9mon++";

	// Size of the game window
	public static final Dimension GAME_SIZE = new Dimension(800, 600);

	// Cute little Bulby icon
	public static final BufferedImage FRAME_ICON = FileIO.readImage(Folder.POKEMON_TILES + "001.png");

	// Frame rate the game runs at
	public static final int FRAME_RATE = 30;

	// The time(ms) between each frame.
	public static final long MS_BETWEEN_FRAMES = 1000 / FRAME_RATE;

	// The size of each tile in the map
	public static final int TILE_SIZE = 32;

	// The time(ms) it takes for the character to move from one tile on the map to another
	public static final int TIME_BETWEEN_TILES = 128;

	public static final String MONEY_SYMBOL = "\u00A5";

	public static <T> void swap(T[] arr) {
		T temp = arr[0];
		arr[0] = arr[1];
		arr[1] = temp;
	}

	public static void error(String errorMessage) {
		JOptionPane.showMessageDialog(null, "Eggs aren't supposed to be green.", "ERROR", JOptionPane.ERROR_MESSAGE);
		Thread.dumpStack();
		System.err.println(errorMessage);
		System.exit(1);
	}

	public static int getPercentageIndex(int[] chances) {
		int sum = 0;
		int random = RandomUtils.getRandomInt(100);

		for (int i = 0; i < chances.length; i++) {
			sum += chances[i];
			if (random < sum) {
				return i;
			}
		}
		
		Global.error("Chances array is improperly formatted.");
		return -1;
	}
}
