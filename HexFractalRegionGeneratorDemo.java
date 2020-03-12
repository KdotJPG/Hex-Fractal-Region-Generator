import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import javax.swing.*;
import java.awt.Color;

public class HexFractalRegionGeneratorDemo
{
	private static final int WIDTH = 1024;
	private static final int HEIGHT = 1024;
	
	private static final int SEED = 8;
	private static final int VARIETY = 4;
	private static final int SIZE = 9;
	private static final int STEPS = 9;
	
	private static final Color[] COLORS = new Color[] {
		Color.GREEN, Color.YELLOW, Color.DARK_GRAY, Color.GRAY, Color.ORANGE, Color.MAGENTA, Color.PINK, Color.RED, Color.BLUE
	};

	public static void main(String[] args)
			throws IOException {
		
		// Initialize
		HexFractalRegionGenerator hexLayer = new HexFractalRegionGenerator(SEED, VARIETY, SIZE, STEPS);
		
		// Image
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int value = hexLayer.smoothSample(x * SIZE * 1.0 / WIDTH, y * SIZE * 1.0 / HEIGHT);
				int rgb = COLORS[value].getRGB();
				image.setRGB(x, y, rgb);
			}
		}
		
		// Save it or show it
		if (args.length > 0 && args[0] != null) {
			ImageIO.write(image, "png", new File(args[0]));
			System.out.println("Saved image as " + args[0]);
		} else {
			JFrame frame = new JFrame();
			JLabel imageLabel = new JLabel();
			imageLabel.setIcon(new ImageIcon(image));
			frame.add(imageLabel);
			frame.pack();
			frame.setResizable(false);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		
	}
}