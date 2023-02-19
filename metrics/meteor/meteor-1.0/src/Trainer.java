import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import edu.cmu.meteor.scorer.MeteorConfiguration;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import edu.cmu.meteor.util.Constants;

public class Trainer {

	// Defaults
	public static final double[] INITIAL = { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 };
	public static final double[] FINAL = { 1.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0 };
	public static final double[] STEP = { 0.05, 0.05, 0.05, 0.05, 0.05, 0.05,
			0.05 };

	private static final double e = 0.001;

	private static final DecimalFormat df = new DecimalFormat("0.00");

	// Variables
	private static ArrayList<Double> initialWeights;
	private static ArrayList<Double> finalWeights;
	private static ArrayList<Double> step;

	private static ArrayList<MeteorStats> statsList;
	private static ArrayList<Double> terList;
	private static ArrayList<Double> lengthList;

	private static MeteorConfiguration config;
	private static ArrayList<Double> weights;

	public static void main(String[] args) {

		// Usage
		if (args.length < 2) {
			System.out.println("METEOR Trainer version " + Constants.VERSION);
			System.out.println("Usage: java -cp meteor.jar Trainer "
					+ "<task> <dataDir> [options]");
			System.out.println();
			System.out.println("Tasks:\t\t\t\tOne of: hter");
			System.out.println();
			System.out.println("Options:");
			System.out.println("-i \"p1 p2 p3 w1 w2 w3 w4\"\tInitial "
					+ "parameters and weights");
			System.out.println("-f \"p1 p2 p3 w1 w2 w3 w4\"\tFinal "
					+ "parameters and weights");
			System.out.println("-s \"p1 p2 p3 w1 w2 w3 w4\"\tSteps");
			return;
		}

		String task = args[0];
		String dataDir = args[1];

		// Load defaults
		initialWeights = new ArrayList<Double>();
		for (double n : INITIAL)
			initialWeights.add(n);
		finalWeights = new ArrayList<Double>();
		for (double n : FINAL)
			finalWeights.add(n);
		step = new ArrayList<Double>();
		for (double n : STEP)
			step.add(n);

		// Input args
		int curArg = 2;
		while (curArg < args.length) {
			if (args[curArg].equals("-i")) {
				initialWeights = makePaddedList(args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-f")) {
				finalWeights = makePaddedList(args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-s")) {
				step = makePaddedList(args[curArg + 1]);
				curArg += 2;
			}
		}

		// Add a value less than one step to account for Java double accuracy
		// issues
		for (int i = 0; i < finalWeights.size(); i++)
			finalWeights.set(i, finalWeights.get(i) + e);

		// Task
		if (!task.equals("hter")) {
			// Only HTER tuning in this version
			System.err.println("Unrecognized task: " + task);
			System.exit(1);
		}

		/*
		 * Run METEOR on each available set and collect the sufficient
		 * statistics for rescoring. Create the MeteorStats list and the TER
		 * list in the same order to avoid lookups on doc/seg IDs.
		 */

		statsList = new ArrayList<MeteorStats>();
		terList = new ArrayList<Double>();
		lengthList = new ArrayList<Double>();

		File dataDirFile = new File(dataDir);
		String testFile = "";
		String refFile = "";

		for (String terFile : dataDirFile.list()) {
			// For each set
			if (terFile.endsWith(".ter")) {
				String sysName = terFile.split("\\.")[0];
				testFile = dataDir + "/" + sysName + ".tst";
				refFile = dataDir + "/" + sysName + ".ref";

				// Read the TER file
				Hashtable<String, Double> terTable = new Hashtable<String, Double>();
				try {
					BufferedReader in = new BufferedReader(new FileReader(
							dataDir + "/" + terFile));
					String line;
					while ((line = in.readLine()) != null) {
						StringTokenizer tok = new StringTokenizer(line);
						String doc = tok.nextToken();
						String seg = tok.nextToken();
						double ter = Double.parseDouble(tok.nextToken());
						terTable.put(doc + ":" + seg, 0.01 * ter);
					}
					in.close();
				} catch (FileNotFoundException ex) {
					System.err.println("Error: If you are viewing this error "
							+ "message, please check your filesystem and "
							+ "Java installation.");
					System.exit(1);
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(1);
				}

				// Run METEOR with all modules
				String[] mArgs = { testFile, refFile, "-sgml", "-ssOut", "-m",
						"exact stem synonym paraphrase", "-w",
						"1.0 1.0 1.0 1.0", "-p", "0.5 0.5 0.5" };
				Meteor.main(mArgs);

				// Store the MeteorStats
				try {
					BufferedReader in = new BufferedReader(new FileReader(
							sysName + "-seg.score"));
					String line;
					while ((line = in.readLine()) != null) {
						StringTokenizer tok = new StringTokenizer(line, "\t");
						tok.nextToken(); // set
						tok.nextToken(); // sysName
						String doc = tok.nextToken(); // doc
						String seg = tok.nextToken(); // seg
						// stats
						String ss = tok.nextToken();
						MeteorStats stats = new MeteorStats(ss);
						// store stats
						statsList.add(stats);
						// store ter for same segment
						double ter = terTable.get(doc + ":" + seg);
						terList.add(ter);
						// store reference length
						lengthList.add((double) stats.referenceLength);
					}
					in.close();

					// Cleanup
					new File(sysName + "-seg.score").delete();
					new File(sysName + "-doc.score").delete();
					new File(sysName + "-sys.score").delete();
				} catch (FileNotFoundException ex) {
					System.err.println("Error: System name and file name do "
							+ "not match for \"" + sysName + "\"");
					System.exit(1);
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
		}

		/*
		 * Rescore the MeteorStats using different parameters and record
		 * correlation with TER scores
		 */

		// Create configuration
		config = new MeteorConfiguration();
		ArrayList<Integer> none = new ArrayList<Integer>();
		config.setModules(none);
		weights = new ArrayList<Double>(initialWeights);

		int param = 0;
		rescore(param);
	}

	private static void rescore(int param) {
		// Rescore if all weights specified
		if (param == step.size()) {

			ArrayList<Double> p = new ArrayList<Double>();
			p.add(weights.get(0));
			p.add(weights.get(1));
			p.add(weights.get(2));

			ArrayList<Double> w = new ArrayList<Double>();
			w.add(weights.get(3));
			w.add(weights.get(4));
			w.add(weights.get(5));
			w.add(weights.get(6));

			config.setParameters(p);
			config.setModuleWeights(w);

			MeteorScorer scorer = new MeteorScorer(config);

			ArrayList<Double> meteorScore = new ArrayList<Double>();

			for (int seg = 0; seg < statsList.size(); seg++) {
				MeteorStats stats = statsList.get(seg);
				scorer.computeMetrics(stats);
				meteorScore.add(stats.score);
			}

			double correlation = pearson(meteorScore, terList, lengthList);

			System.out.print(correlation);
			for (Double n : weights)
				System.out.print(" " + df.format(n));
			System.out.println();
			return;
		}

		for (double n = initialWeights.get(param); n <= finalWeights.get(param); n += step
				.get(param)) {
			weights.set(param, n);
			rescore(param + 1);
		}
	}

	private static double pearson(ArrayList<Double> x, ArrayList<Double> y,
			ArrayList<Double> w) {

		int N = w.size();

		double sum_x_w = 0.0;
		double sum_y_w = 0.0;
		double sum_w = 0.0;

		for (int i = 0; i < N; i++) {
			sum_x_w += (x.get(i) * w.get(i));
			sum_y_w += (y.get(i) * w.get(i));
			sum_w += w.get(i);
		}

		double mean_x = (sum_x_w / sum_w);
		double mean_y = (sum_y_w / sum_w);

		double cov_x_y_top = 0.0;
		double cov_x_x_top = 0.0;
		double cov_y_y_top = 0.0;

		for (int i = 0; i < N; i++) {
			cov_x_y_top += (w.get(i) * (x.get(i) - mean_x) * (y.get(i) - mean_y));
			cov_x_x_top += (w.get(i) * (x.get(i) - mean_x) * (x.get(i) - mean_x));
			cov_y_y_top += (w.get(i) * (y.get(i) - mean_y) * (y.get(i) - mean_y));
		}

		double cov_x_y = cov_x_y_top / sum_w;
		double cov_x_x = cov_x_x_top / sum_w;
		double cov_y_y = cov_y_y_top / sum_w;

		double corr_pearson = cov_x_y / Math.sqrt(cov_x_x * cov_y_y);
		if (Double.isNaN(corr_pearson))
			return 0.0;
		return corr_pearson;
	}

	private static ArrayList<Double> makePaddedList(String values) {
		ArrayList<Double> list = new ArrayList<Double>();
		StringTokenizer tok = new StringTokenizer(values);
		while (tok.hasMoreTokens())
			list.add(Double.parseDouble(tok.nextToken()));
		while (list.size() < INITIAL.length)
			list.add(0.0);
		return list;
	}
}