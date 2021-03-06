package main;

import util.FileIO;
import util.Folder;

import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public final class Global {

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

	public static final String MONEY_SYMBOL = "\u00A5";

	public static void error(String errorMessage) {
		error(errorMessage, true);
	}

	public static void error(String errorMessage, boolean exit) {
		JOptionPane.showMessageDialog(null, "Eggs aren't supposed to be green.", "ERROR", JOptionPane.ERROR_MESSAGE);
		Thread.dumpStack();
		System.err.println(errorMessage);

		if (exit) {
			System.exit(1);
		}
	}

	// Cannot be instantiated
	private Global() {}
}
