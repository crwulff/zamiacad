package org.zamia.instgraph.interpreter.logger;

import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Anton Chepurov
 */
public class HammingDistance {

	public enum Print {SUM, MIN, AVE}

	private TreeMap<SourceFile, TreeMap<Integer, Distance>> distances;

	private double uniquenessJaan;
	private double uniquenessMaksim;

	private int numItems;

	private HammingDistance(int numItems) {
		this.numItems = numItems;
		distances = new TreeMap<SourceFile, TreeMap<Integer, Distance>>(Report.LOCATION_COMPARATOR);
	}

	public double getUniquenessJaan() {
		return uniquenessJaan;
	}

	public double getUniquenessMaksim() {
		return uniquenessMaksim;
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
		StringBuilder b = new StringBuilder("           Test Quality: " + getSum()).append("\n");
		b.append("Normalized Test Quality: ").append(getSumNorm()).append("\n");

		b.append("           Uniqueness (Jaan): ").append(uniquenessJaan).append("\n");
		b.append("           Uniqueness (Maks): ").append(uniquenessMaksim).append("\n");
		b.append("Normalized Uniqueness (Maks): ").append(uniquenessMaksim / numItems).append("\n\n");
		b.append("Num. Tests      : ").append(getNumTests()).append("\n");
		b.append("Num. Assignments: ").append(numItems).append("\n\n");

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

	public static HammingDistance createFrom(List<IGHitCountLogger> loggers, int numItems) {

		HashMap<SourceFile, boolean[][]> matrices = new HashMap<SourceFile, boolean[][]>();

		int tst = 0;

		for (IGHitCountLogger logger : loggers) {

			logger.dropSystemFiles();

			for (Map.Entry<SourceFile, IGCodeExecutionLogger> entry : logger.fLoggersByFile.entrySet()) {
				SourceFile file = entry.getKey();
				IGHitCountLogger fileLogger = (IGHitCountLogger) entry.getValue();

				int n = file.getNumLines();

				boolean[][] matrix = matrices.containsKey(file) ? matrices.get(file) : null;
				if (matrix == null) {
					matrix = new boolean[loggers.size()][];
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

		HammingDistance ret = new HammingDistance(numItems);

		double uniquenessJaan = 0, uniquenessMaksim = 0;

		for (Map.Entry<SourceFile, boolean[][]> entry : matrices.entrySet()) {
			SourceFile file = entry.getKey();
			boolean[][] matrix = entry.getValue();

			/* Compute Uniqueness (by Jaan) */
			/* Compute Uniqueness (by Maksim) */
			int numLines = matrix[0].length;
			int numTests = matrix.length;
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
