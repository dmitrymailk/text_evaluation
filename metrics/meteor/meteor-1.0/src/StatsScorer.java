import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.cmu.meteor.scorer.MeteorConfiguration;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import edu.cmu.meteor.util.Constants;

public class StatsScorer {

	public static void main(String[] args) {

		// Usage for -h, --help, or similar
		if (args.length == 1 && args[0].contains("-h")) {
			System.out.println("METEOR Stats Scorer version "
					+ Constants.VERSION);
			System.out.println("Usage: java -cp meteor.jar "
					+ "StatsScorer [options]");
			System.out.println();
			System.out.println("Options:");
			System.out.println("-l language\t\t\tOne of: en cz de es fr");
			System.out.println("-t task\t\t\t\tOne of: af rank hter");
			System.out
					.println("-p \"alpha beta gamma\"\t\tCustom parameters (overrides default)");
			System.out
					.println("-w \"weight1 weight2 ...\"\tSpecify module weights (overrides default)");
			System.out.println();
			System.out.println("Default settings are stored in the "
					+ "statsscorer.properties file");
			return;
		}

		// Load defaults
		String propFile = "statsscorer.properties";
		Properties props = new Properties();
		try {
			props.load(ClassLoader.getSystemResource(propFile).openStream());
		} catch (Exception ex) {
			System.err.println("Error: Could not load properties file:");
			ex.printStackTrace();
			System.exit(1);
		}

		// Get input args
		int arg = 0;
		while (arg < args.length) {
			if (args[arg].equals("-l")) {
				props.setProperty("language", args[arg + 1]);
				arg += 2;
			} else if (args[arg].equals("-t")) {
				props.setProperty("task", args[arg + 1]);
				arg += 2;
			} else if (args[arg].equals("-p")) {
				props.setProperty("parameters", args[arg + 1]);
				arg += 2;
			} else if (args[arg].equals("-w")) {
				props.setProperty("moduleWeights", args[arg + 1]);
				arg += 2;
			}
		}

		// Create configuration
		MeteorConfiguration config = new MeteorConfiguration();

		String language = props.getProperty("language");
		if (!language.equals("default"))
			config.setLanguage(language);

		String task = props.getProperty("task");
		if (!task.equals("default"))
			config.setTask(task);

		String parameters = props.getProperty("parameters");
		if (!parameters.equals("default")) {
			ArrayList<Double> params = new ArrayList<Double>();
			StringTokenizer tok = new StringTokenizer(parameters);
			while (tok.hasMoreTokens())
				params.add(Double.parseDouble(tok.nextToken()));
			config.setParameters(params);
		}

		String weights = props.getProperty("moduleWeights");
		if (!weights.equals("default")) {
			ArrayList<Double> w = new ArrayList<Double>();
			StringTokenizer tok = new StringTokenizer(weights);
			while (tok.hasMoreTokens())
				w.add(Double.parseDouble(tok.nextToken()));
			config.setModuleWeights(w);
		}

		// Do not load resources for any modules
		ArrayList<Integer> none = new ArrayList<Integer>();
		config.setModules(none);

		MeteorScorer scorer = new MeteorScorer(config);

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;

		// Score input lines
		try {
			while ((line = in.readLine()) != null) {
				MeteorStats stats = new MeteorStats(line);
				scorer.computeMetrics(stats);
				System.out.println(stats.score);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}