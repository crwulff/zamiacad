package org.zamia.instgraph.interpreter.logger;

import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Anton Chepurov
 */
public class DiagnosticTestQuality {

	public enum Print {SUM, MIN, AVE}

	private TreeMap<SourceFile, TreeMap<Integer, Distance>> distances;

	private double uniquenessJaan;
	private double uniquenessMaksim;
	private double deviation;
	private double coverage;

	private int numItems;

	private DiagnosticTestQuality(int numItems) {
		this.numItems = numItems;
		distances = new TreeMap<SourceFile, TreeMap<Integer, Distance>>(Report.LOCATION_COMPARATOR);
	}

	public double getUniquenessJaan() {
		return uniquenessJaan;
	}

	public double getUniquenessMaksim() {
		return uniquenessMaksim;
	}

	public double getDeviation() {
		return deviation;
	}

	public double getCoverage() {
		return coverage;
	}

	public Integer getNumTests(SourceFile file) {
		return distances.get(file).size();
	}

	public int getNumTests() {
		return distances.values().iterator().next().size();
	}

	public int getSum() {
		int sum = 0;
		int n = getNumTests();
		for (int test = 0; test < n; test++) {
			sum += getSum(test);
		}
		return sum;
	}

	public double getSumNorm() {
		return (double) getSum() / (getNumTests() * numItems);
	}

	public double getSumNormRaimund() {
		int N = getNumDistances();
		double halfSum = getSum() / 2;
		return halfSum / N;
	}

	private int getNumDistances() {
		int n = getNumTests();
		return n * (n - 1) / 2;
	}

	public int getSum(int test) {
		int sum = 0;
		for (SourceFile file : distances.keySet()) {
			sum += getSum(file, test);
		}
		return sum;
	}

	public int getSum(SourceFile file, int test) {
		if (!distances.containsKey(file)) {
			return 0;
		}
		TreeMap<Integer, Distance> testDistances = distances.get(file);
		return testDistances.containsKey(test) ? testDistances.get(test).sum : 0;
	}

	public double getAverage(int test) {
		double sum = 0;
		for (SourceFile file : distances.keySet()) {
			sum += getAverage(file, test);
		}
		return sum;
	}

	public double getAverage(SourceFile file, int test) {
		if (!distances.containsKey(file)) {
			return 0;
		}
		TreeMap<Integer, Distance> testDistances = distances.get(file);
		return testDistances.containsKey(test) ? testDistances.get(test).average : 0;
	}

	public int getMin(SourceFile file, int test) {
		if (!distances.containsKey(file)) {
			return 0;
		}
		TreeMap<Integer, Distance> testDistances = distances.get(file);
		return testDistances.containsKey(test) ? testDistances.get(test).min : 0;
	}

	private static final ZamiaLogger logger = ZamiaLogger.getInstance();

	@SuppressWarnings("UnusedDeclaration")
	public void toFile(PrintStream out) {
		print(out, "%s", this);
	}

	private static void print(PrintStream out, String line, Object... args) {

		if (out == null) {
			logger.info(line, args);
		} else {
			out.printf(line + "\n", args);
		}
	}

	public String toString(EnumSet<Print> flags) {
		StringBuilder b = new StringBuilder("                Test Quality: " + getSum()).append("\n");
		b.append("     Normalized Test Quality: ").append(getSumNorm()).append("\n");
		b.append("     Normalized Test Raimund: ").append(getSumNormRaimund()).append("\n");
		b.append("              RMSD Deviation: ").append(deviation).append("\n");
		b.append("           Uniqueness (Jaan): ").append(uniquenessJaan).append("\n");
		b.append("           Uniqueness (Maks): ").append(uniquenessMaksim).append("\n");
		b.append("Normalized Uniqueness (Maks): ").append(uniquenessMaksim / numItems).append("\n\n");
		b.append("Num. Tests      : ").append(getNumTests()).append("\n");
		b.append("Num. Assignments: ").append(numItems).append("\n");
		b.append("Assign. Coverage: ").append(String.format("%.2f%%\n\n", coverage));

		for (Map.Entry<SourceFile, TreeMap<Integer, Distance>> entry : distances.entrySet()) {
			b.append(entry.getKey()).append(": ");
			for (Distance distance : entry.getValue().values()) {
				b.append(distance.toString(flags)).append(" | ");
			}
			b.append("\n");
		}

		for (int test = 0; test < getNumTests(); test++) {
			b.append(String.format("%d %.4f | ", getSum(test), getAverage(test)));
		}
		return b.toString();
	}

	@Override
	public String toString() {
		return toString(EnumSet.of(Print.SUM, Print.AVE));
	}

	private static class Distance {

		private final int sum;
		private final int min;
		private final double average;

		public Distance(int sum, int min, double average) {
			this.sum = sum;
			this.min = min;
			this.average = average;
		}

		@Override
		public String toString() {
			return toString(EnumSet.allOf(Print.class));
		}

		public String toString(EnumSet<Print> flags) {
			StringBuilder b = new StringBuilder();
			if (flags.contains(Print.SUM)) {
//                b.append(String.format("(S)%3s ", sum));
				b.append(sum);
			}
			if (flags.contains(Print.MIN)) {
				b.append(String.format("(M)%3s ", min));
			}
			if (flags.contains(Print.AVE)) {
//                b.append(String.format("(A)%7.4f", average));
				b.append(String.format(" %.4f", average));
			}
			return b.toString();
		}
	}

	private void setDistance(SourceFile file, int test, Distance distance) {

		TreeMap<Integer, Distance> testDistances = distances.get(file);
		if (testDistances == null) {
			testDistances = new TreeMap<Integer, Distance>();
			distances.put(file, testDistances);
		}

		testDistances.put(test, distance);
	}

	/**
	 * @param loggers loggers that represent executed statements
	 * @param numItems total number of assignments in the design
	 * @return assessment of the quality of diagnostic test
	 */
	public static DiagnosticTestQuality createFrom(List<IGHitCountLogger> loggers, int numItems) {

		if (loggers.size() < 2) {
			logger.info("DiagnosticTestQuality: test quality can only be computed for multiple tests. Num of received tests: %d", loggers.size());
			return null;
		}

		HashMap<SourceFile, boolean[][]> matrices = new HashMap<SourceFile, boolean[][]>();

		int tst = 0;
		int numTests = loggers.size();

		for (IGHitCountLogger logger : loggers) {

			logger.dropSystemFiles();

			for (Map.Entry<SourceFile, IGCodeExecutionLogger> entry : logger.fLoggersByFile.entrySet()) {
				SourceFile file = entry.getKey();
				IGHitCountLogger fileLogger = (IGHitCountLogger) entry.getValue();

				int n = file.getNumLines();

				boolean[][] matrix = matrices.containsKey(file) ? matrices.get(file) : null;
				if (matrix == null) {
					matrix = new boolean[numTests][];
					matrices.put(file, matrix);
				}
				matrix[tst] = new boolean[n];

				for (int line = 0; line < n; line++) {
					int hits = fileLogger.getCount(line);
					if (hits > 0) {
						matrix[tst][line] = true;
					}
				}
			}

			tst++;
		}

		DiagnosticTestQuality ret = new DiagnosticTestQuality(numItems);

		double uniquenessJaan = 0, uniquenessMaksim = 0;

		// todo: matrices can be merged into a single matrix => quality can be computed as with deviation below.
		// todo: In this case only the final quality values have to be stored in the object. The rest => to GC.
		for (Map.Entry<SourceFile, boolean[][]> entry : matrices.entrySet()) {
			SourceFile file = entry.getKey();
			boolean[][] matrix = entry.getValue();

			/* Compute Uniqueness (by Jaan) */
			/* Compute Uniqueness (by Maksim) */
			int numLines = matrix[0].length;
			for (int line = 0; line < numLines; line++) {
				int count = 0;
				for (int test = 0; test < numTests; test++) {
					if (matrix[test][line]) {
						count++;
					}
				}
				uniquenessJaan += count > 0 ? (double) numTests / count : 0;
				uniquenessMaksim += (double) count / numTests;
			}

			/* Compute HAMMING */
			for (int first = 0; first < numTests; first++) {
				int total = 0, min = Integer.MAX_VALUE;
				for (int second = 0; second < numTests; second++) {
					if (first == second) {
						continue;
					}
					int dist = computeHammingDistance(first, second, matrix);
					total += dist;
					if (dist < min) {
						min = dist;
					}
				}

				ret.setDistance(file, first, new Distance(total, min, (double) total / (numTests - 1)));
			}
		}

		ret.uniquenessJaan = uniquenessJaan;
		ret.uniquenessMaksim = uniquenessMaksim;

		/* Compute DEVIATION */
		boolean[][] bigMatrix = mergeMatrices(new ArrayList<boolean[][]>(matrices.values()));
		double hAve = ret.getSumNormRaimund();
		int N = ret.getNumDistances();
		double total = 0;
		for (int first = 0; first < numTests - 1; first++) {
			for (int second = first; second < numTests; second++) {
				if (first == second) {
					continue;
				}
				int dist = computeHammingDistance(first, second, bigMatrix);
				total += Math.pow((double) dist - hAve, 2);
			}
		}
		ret.deviation = Math.sqrt(total / N);

		double coverage = 0;
		try {
			IGCodeExecutionLogger mergedLogger = IGCodeExecutionLogger.mergeAll(loggers.toArray(new IGCodeExecutionLogger[numTests]));
			int covered = mergedLogger.getNumItems();
			coverage = (double) covered / numItems * 100;
		} catch (ZamiaException e) {
			logger.debug("DiagnosticTestQuality: could not merge loggers to compute coverage", e, "");
		}

		ret.coverage = coverage;

		return ret;
	}

	private static boolean[][] mergeMatrices(List<boolean[][]> matrices) {
		int numTests = 0;
		int totalLines = 0; // over all files
		for (boolean[][] matrix : matrices) {
			totalLines += matrix[0].length;
			numTests = matrix.length;
		}

		boolean[][] ret = new boolean[numTests][];
		for (int test = 0; test < numTests; test++) {
			ret[test] = new boolean[totalLines];
			int globalLine = 0;
			for (boolean[][] matrix : matrices) {
				for (int line = 0; line < matrix[test].length; line++) {
					ret[test][globalLine++] = matrix[test][line];
				}
			}
		}
		return ret;
	}

	private static int computeHammingDistance(int firstTest, int secondTest, boolean[][] matrix) {
		int dist = 0;
		for (int line = 0; line < matrix[firstTest].length; line++) {
			if (matrix[firstTest][line] ^ matrix[secondTest][line])
				dist++;
		}
		return dist;
	}
}
