import java.util.Random;

public class HexFractalRegionGenerator {
	
	private int[][] cells;
	double coordinateScale;
	
	public HexFractalRegionGenerator(long seed, int variety, int size, int steps) {
		Random random = new Random(seed);
		
		// (2^N)x(2^N) plus one for padding, for diagonally-compressed-square array
		// which models hexagonal grid.
		int stride = 1 << steps; // Math.pow(2, steps);
		int unpaddedArraySize = stride * size;
		int arraySize = unpaddedArraySize + 1;
		cells = new int[arraySize][arraySize];
		
		// Adjust for detail level and "compression" of square, so (1, 1) maps to (stride, stride)
		this.coordinateScale = stride * 0.5773502691896257; // sqrt(1/3)
		
		// Assign initial values
		for (int y = 0; y < arraySize; y += stride) {
			for (int x = 0; x < arraySize; x += stride) {
				cells[x][y] = random.nextInt(variety);
			}
		}
		
		// Fractal steps
		for (int i = 0; i < steps; i++) {
			
			// When indexing neighbors on the next level of detail, we use this.
			// We will also use it as the actual stride for the next step.
			int halfStride = stride / 2;
			
			// For each cell on the current level of detail...
			for (int y = 0; y < arraySize; y += stride) {
				for (int x = 0; x < arraySize; x += stride) {
					// It has six hexagonal neighbors on the next layer down. It suffices and also avoids repetition,
					// to consider only the "positive" three, (h, 0), (h, h), (h, 0), omitting those out of range.
					// h = halfStride, s = stride
					
					// (h, 0) picks between current and current+(s, 0)
					if (x < unpaddedArraySize)
						cells[y][x + halfStride] = random.nextBoolean() ? cells[y][x + stride] : cells[y][x];
					
					// (0, h) picks between current and current+(0, s)
					if (y < unpaddedArraySize)
						cells[y + halfStride][x] = random.nextBoolean() ? cells[y + stride][x] : cells[y][x];
					
					// (h, h) picks between current and current+(s, s)
					if (x < unpaddedArraySize && y < unpaddedArraySize)
						cells[y + halfStride][x + halfStride] = random.nextBoolean() ? cells[y + stride][x + stride] : cells[y][x];
				}
			}
			
			stride = halfStride;
		}
	}
	
	public int simpleSample(double x, double y) {
		
		// Rescale input coordinates [0,1] to match stride.
		x *= coordinateScale;
		y *= coordinateScale;
		
		// Skew like simplex noise
		double s = 0.366025403784439 * (x + y);
		double xs = x + s, ys = y + s;
		
		// Get base and internal offsets, for local diagonally-compressed square.
		int xsb = (int)xs; if (xs < xsb) xsb -= 1;
		int ysb = (int)ys; if (ys < ysb) ysb -= 1;
		double xsi = xs - xsb, ysi = ys - ysb;
		
		// Find closest of the four points.
		double p = 2 * xsi - ysi;
		double q = 2 * ysi - xsi;
		double r = xsi + ysi;
		if (r > 1) {
			p -= 1; q -= 1; r -= 2;
			if (p < -1) {
				return cells[ysb + 1][xsb];
			} else if (q < -1) {
				return cells[ysb][xsb + 1];
			}
			return cells[ysb + 1][xsb + 1];
		} else {
			if (p > 1) {
				return cells[ysb][xsb + 1];
			} else if (q > 1) {
				return cells[ysb + 1][xsb];
			}
			return cells[ysb][xsb];
		}
	}
	
	public int smoothSample(double x, double y) {
		
		// Rescale input coordinates [0,1] to match stride.
		x *= coordinateScale;
		y *= coordinateScale;
		
		// Skew like simplex noise
		double s = 0.366025403784439 * (x + y);
		double xs = x + s, ys = y + s;
		
		// Get base and internal offsets, for local diagonally-compressed square.
		int xsb = (int)xs; if (xs < xsb) xsb -= 1;
		int ysb = (int)ys; if (ys < ysb) ysb -= 1;
		double xsi = xs - xsb, ysi = ys - ysb;
		
		// Prepare arrays to combine weights of like values, and separate those of different ones.
		int[] values = new int[] { -1, -1, -1 };
		double[] scores = new double[3];
		
		// Consider (0, 0) and (1, 1)
		handleSmoothSampleWeight(values, scores, cells[ysb][xsb], xsi, ysi);
		handleSmoothSampleWeight(values, scores, cells[ysb + 1][xsb + 1], xsi - 1, ysi - 1);
		
		// Consider one of (1, 0) and (0, 1)
		if (ysi > xsi)
			handleSmoothSampleWeight(values, scores, cells[ysb + 1][xsb], xsi, ysi - 1);
		else
			handleSmoothSampleWeight(values, scores, cells[ysb][xsb + 1], xsi - 1, ysi);
		
		// Find the winning value for this input coordinate.
		int value = -1;
		double score = -1;
		for (int i = 0; i < 3; i++) {
			if (scores[i] > score) {
				value = values[i];
				score = scores[i];
			}
		}
		
		return value;
	}
	
	private void handleSmoothSampleWeight(int[] values, double[] scores, int value, double xsi, double ysi) {
		
		// Hexagonally-symmetric bump function
		double zsi = xsi - ysi;
		double weight = (1 - xsi * xsi) * (1 - ysi * ysi) * (1 - zsi * zsi);
		
		// Combine weights of identical cells, otherwise create new entry.
		for (int i = 0; i < 3; i++) {
			if (values[i] == -1 || values[i] == value) {
				values[i] = value;
				scores[i] += weight;
				break;
			}
		}
	}
}
