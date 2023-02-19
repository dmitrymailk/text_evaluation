/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import edu.cmu.meteor.scorer.MeteorConfiguration;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import edu.cmu.meteor.util.Constants;
import edu.cmu.meteor.util.Normalizer;
import edu.cmu.meteor.util.SGMData;

public class Meteor {

	public static void main(String[] args) {

		// Usage
		if (args.length < 2) {
			printUsage();
			return;
		}

		// Files
		String testFile = args[0];
		String refFile = args[1];

		// Default configuration
		String propFile = "meteor.properties";
		Properties props = new Properties();
		try {
			props.load(ClassLoader.getSystemResource(propFile).openStream());
		} catch (Exception ex) {
			System.err.println("Error: Could not load properties file:");
			ex.printStackTrace();
			System.exit(1);
		}

		// Use command line options and props to create configuration
		updatePropertiesWithArgs(props, args, 2);
		MeteorConfiguration config = createConfiguration(props);

		// Print settings
		boolean ssOut = Boolean.parseBoolean(props.getProperty("ssOut"));
		boolean sgml = Boolean.parseBoolean(props.getProperty("sgml"));
		boolean nBest = Boolean.parseBoolean(props.getProperty("nBest"));
		boolean oracle = Boolean.parseBoolean(props.getProperty("oracle"));

		String format = sgml ? "SGML" : "plaintext";
		if (!oracle && !ssOut) {
			System.out.println("METEOR version: " + Constants.VERSION);
			System.out.println("Language:       " + config.getLanguage());
			System.out.println("Format:         " + format);
			System.out.println("Task:           " + config.getTaskDesc());
			System.out.println("Modules:        " + config.getModulesString());
			System.out.println("Weights:        "
					+ config.getModuleWeightsString());
			System.out.println("Parameters:     "
					+ config.getParametersString());
			System.out.println();
		}

		// Module / Weight check
		if (config.getModuleWeights().size() < config.getModules().size()) {
			System.err.println("Warning: More modules than weights specified "
					+ "- modules with no weights will not be counted.");
		}

		// SGML /NBest check
		if (sgml && nBest) {
			System.err.println("Warning: nBest incompatible with SGML - using "
					+ "SGML only");
		}

		MeteorScorer scorer = new MeteorScorer(config);

		if (sgml) {
			try {
				scoreSGML(scorer, props, config, testFile, refFile);
			} catch (IOException ex) {
				System.err.println("Error: Could not score SGML files:");
				ex.printStackTrace();
				System.exit(1);
			}
		} else
			try {
				if (nBest) {
					scoreNBest(scorer, props, config, testFile, refFile);
				} else {
					scorePlaintext(scorer, props, config, testFile, refFile);
				}
			} catch (IOException ex) {
				System.err.println("Error: Could not score text files:");
				ex.printStackTrace();
				System.exit(1);
			}
	}

	private static int getRefCount(Properties props) {
		String refCountString = props.getProperty("refCount");
		if (refCountString.equals("default"))
			return 1;
		else
			return Integer.parseInt(refCountString);
	}

	/**
	 * Input is in plaintext format, output is in simple format
	 */

	private static void scorePlaintext(MeteorScorer scorer, Properties props,
			MeteorConfiguration config, String testFile, String refFile)
			throws IOException {

		ArrayList<String> lines1 = new ArrayList<String>();
		ArrayList<String> lines2 = new ArrayList<String>();
		ArrayList<ArrayList<String>> lines2mref = new ArrayList<ArrayList<String>>();

		BufferedReader in = new BufferedReader(new FileReader(testFile));
		String line;
		while ((line = in.readLine()) != null)
			lines1.add(line);
		in.close();

		int refCount = getRefCount(props);

		in = new BufferedReader(new FileReader(refFile));
		if (refCount == 1)
			while ((line = in.readLine()) != null)
				lines2.add(line);
		else {
			while ((line = in.readLine()) != null) {
				ArrayList<String> refs = new ArrayList<String>();
				refs.add(line);
				for (int refNum = 1; refNum < refCount; refNum++)
					refs.add(in.readLine());
				lines2mref.add(refs);
			}
		}
		in.close();
		if ((refCount == 1 && lines1.size() != lines2.size())
				|| (refCount > 1 && lines1.size() != lines2mref.size())) {
			System.err.println("Error: test and reference not same length");
			return;
		}

		MeteorStats aggStats = new MeteorStats();

		boolean ssOut = Boolean.parseBoolean(props.getProperty("ssOut"));
		for (int i = 0; i < lines1.size(); i++) {
			MeteorStats stats;
			if (refCount == 1) {
				stats = scorer.getMeteorStats(lines1.get(i), lines2.get(i));
			} else
				stats = scorer.getMeteorStats(lines1.get(i), lines2mref.get(i));
			if (ssOut)
				System.out.println(stats.toString());
			else
				System.out.println("Segment " + (i + 1) + " score:\t"
						+ stats.score);
			aggStats.addStats(stats);
		}

		if (!ssOut) {
			scorer.computeMetrics(aggStats);
			printVerboseStats(aggStats, config);
		}
	}

	/**
	 * Input is in nBest format, output will be in nBest score format
	 */

	private static void scoreNBest(final MeteorScorer scorer, Properties props,
			final MeteorConfiguration config, String testFile, String refFile)
			throws IOException {

		// Info for normalization since it will be done outside of the scorer
		int langID = config.getLangID();
		boolean normalize = Boolean
				.parseBoolean(props.getProperty("normalize"));
		boolean keepPunctuation = Boolean.parseBoolean(props
				.getProperty("keepPunctuation"));
		int refCount = getRefCount(props);
		final boolean oracle = Boolean
				.parseBoolean(props.getProperty("oracle"));
		final boolean ssOut = Boolean.parseBoolean(props.getProperty("ssOut"));

		// Number of jobs to run simultaneously
		int jobs = 1;
		String jobString = props.getProperty("jobs");
		if (!jobString.equals("default"))
			jobs = Integer.parseInt(jobString);

		final MeteorWorker.Request.Completion nextSegment = new MeteorWorker.Request.Completion();
		final MeteorWorker.Request.Completion doneScoring = new MeteorWorker.Request.Completion();
		final MeteorWorker.Request.Completion errorScoring = new MeteorWorker.Request.Completion();

		final ArrayBlockingQueue<MeteorWorker.Request> toScore = new ArrayBlockingQueue<MeteorWorker.Request>(
				jobs * 2);
		final ArrayBlockingQueue<MeteorWorker.Request.Completion> toOutput = new ArrayBlockingQueue<MeteorWorker.Request.Completion>(
				jobs * 2);

		ArrayList<Thread> scorers = new ArrayList<Thread>(jobs);
		for (int i = 0; i < jobs; i++) {
			Thread worker = new Thread(new MeteorWorker(scorer, toScore));
			worker.start();
			scorers.add(worker);
		}

		Thread printer = new Thread(new Runnable() {
			public void run() {
				MeteorStats bestAgg = new MeteorStats();
				MeteorStats firstAgg = new MeteorStats();
				String oracleTrans = "";
				MeteorWorker.Request.Completion task = null;
				int curSeg = 0;
				while (task != doneScoring) {
					int segNum = 0;
					MeteorStats best = null;
					MeteorStats first = null;
					while (true) {
						try {
							task = toOutput.take();
						} catch (java.lang.InterruptedException e) {
							return;
						}
						if (task == nextSegment)
							break;
						if (task == doneScoring)
							break;
						if (task == errorScoring)
							return;
						MeteorStats stats;
						try {
							stats = task.get();
						} catch (java.lang.InterruptedException e) {
							return;
						}
						if (first == null) {
							oracleTrans = task.test;
							best = stats;
							first = stats;
						} else if (stats.score > best.score) {
							oracleTrans = task.test;
							best = stats;
						}
						if (ssOut)
							System.out.println(stats.toString());
						else if (!oracle)
							System.out.println("Segment " + curSeg
									+ " translation " + (segNum + 1)
									+ " score:\t" + stats.score);
						segNum++;
					}
					if (first != null) {
						bestAgg.addStats(best);
						firstAgg.addStats(first);
					}
					if (task == nextSegment) {
						if (oracle) {
							System.out.println(oracleTrans);
						} else {
							System.out.println();
						}
					}
					curSeg++;
				}
				if (!oracle && !ssOut) {
					scorer.computeMetrics(bestAgg);
					scorer.computeMetrics(firstAgg);
					printVerboseStats(bestAgg, config,
							"Best-choice translation statistics\n");
					printVerboseStats(firstAgg, config,
							"\nFirst-sentence translation statistics\n");
				}
			}
		});
		printer.start();

		// Reading thread
		BufferedReader testRead = new BufferedReader(new FileReader(testFile));
		BufferedReader refRead = new BufferedReader(new FileReader(refFile));
		try {
			String line;
			while ((line = testRead.readLine()) != null) {
				int segCount = Integer.parseInt(line);

				// Read references
				ArrayList<String> refs = new ArrayList<String>(refCount);
				for (int refNum = 0; refNum < refCount; refNum++) {
					String ref = refRead.readLine();
					if (ref == null) {
						System.err.println("Error: too few references");
						toOutput.put(doneScoring);
						System.exit(1);
					}
					// Normalize refs as they are read
					if (normalize)
						ref = Normalizer.normalizeLine(ref, langID,
								keepPunctuation);
					refs.add(ref);
				}

				for (int segNum = 0; segNum < segCount; segNum++) {
					String seg = testRead.readLine();
					if (seg == null) {
						System.err.println("Error: too few segments");
						toOutput.put(doneScoring);
						System.exit(1);
					}
					// Normalize segments as they are read
					if (normalize)
						seg = Normalizer.normalizeLine(seg, langID,
								keepPunctuation);
					MeteorWorker.Request request = new MeteorWorker.Request(
							seg, refs);
					toScore.put(request);
					toOutput.put(request.getOut());
				}
				toOutput.put(nextSegment);
			}
			toOutput.put(doneScoring);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			try {
				toOutput.put(doneScoring);
			} catch (InterruptedException ex2) {
				ex.printStackTrace();
				System.exit(1);
			}
			System.err.println("Error: Interrupted");
			System.exit(1);
		}

		try {
			printer.join();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		try {
			for (int i = 0; i < jobs; i++) {
				toScore.put(MeteorWorker.DONE);
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		if (refRead.readLine() != null) {
			System.err.println("Error: too many references");
			System.exit(1);
		}

		testRead.close();
		refRead.close();
	}

	// Used for multithreaded nBest scoring
	static class MeteorWorker implements Runnable {

		public static class Request {
			public static class Completion {
				private Semaphore wait;
				private String test;
				private MeteorStats result;

				public Completion() {
					wait = new Semaphore(0);
				}

				public Completion(String test) {
					this.test = test;
					wait = new Semaphore(0);
				}

				public void finish(MeteorStats value) {
					result = value;
					wait.release();
				}

				// Only call once to avoid deadlock
				public MeteorStats get() throws InterruptedException {
					wait.acquire();
					return result;
				}
			}

			protected String test;
			protected ArrayList<String> references;

			private Completion out;

			// Null request
			protected Request() {
			}

			public Request(String seg, ArrayList<String> refs) {
				test = seg;
				references = refs;
				out = new Completion(test);
			}

			public Completion getOut() {
				return out;
			}
		}

		public static final Request DONE = new Request();

		private MeteorScorer scorer;
		private BlockingQueue<Request> requests;

		public MeteorWorker(MeteorScorer scorer, BlockingQueue<Request> queue) {
			this.scorer = new MeteorScorer(scorer);
			requests = queue;
		}

		public void run() {
			Request req;
			try {
				while ((req = requests.take()) != DONE) {
					req.getOut().finish(
							scorer.getMeteorStats(req.test, req.references));
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				return;
			}
		}
	}

	/**
	 * Input is in SGML format, output to be in seg, doc, sys score format
	 */

	private static void scoreSGML(MeteorScorer scorer, Properties props,
			MeteorConfiguration config, String testFile, String refFile)
			throws IOException {

		// Gather SGML data
		SGMData data = new SGMData();
		SGMData.populate(data, testFile, false);
		SGMData.populate(data, refFile, true);

		// Segment level data
		ArrayList<String> segKeys = new ArrayList<String>();
		segKeys.addAll(data.segKeys);
		Collections.sort(segKeys, new Comparator<String>() {
			public int compare(String s1, String s2) {
				String doc1 = s1.substring(0, s1.indexOf(":") + 1);
				String seg1 = s1.substring(s1.lastIndexOf(":") + 1);
				String doc2 = s2.substring(0, s2.indexOf(":") + 1);
				String seg2 = s2.substring(s2.lastIndexOf(":") + 1);
				int comp = doc1.compareTo(doc2);
				if (comp != 0)
					return comp;
				return Integer.parseInt(seg1) - Integer.parseInt(seg2);
			}
		});
		Hashtable<String, MeteorStats> segStats = new Hashtable<String, MeteorStats>();

		// Document level data
		ArrayList<String> docKeys = new ArrayList<String>();
		docKeys.addAll(data.docKeys);
		Collections.sort(docKeys);
		Hashtable<String, MeteorStats> docStats = new Hashtable<String, MeteorStats>();
		for (String docID : docKeys)
			docStats.put(docID, new MeteorStats());

		// System level data
		String sysID = props.getProperty("system");
		if (sysID.equals("default"))
			sysID = data.firstSysID;
		MeteorStats aggStats = new MeteorStats();

		// Score segments, aggregate stats
		for (String key : segKeys) {
			String docID = key.substring(0, key.indexOf(":"));
			String testText = data.segText.get(key + "::" + sysID);
			ArrayList<String> refText = new ArrayList<String>();
			Iterator<String> refs = data.references.iterator();
			while (refs.hasNext())
				refText.add(data.segText.get(key + "::" + refs.next()));

			// Skip cases of incomplete data
			if (testText == null) {
				System.err.println("Warning: no hypothesis for document \""
						+ docID + "\" segment \""
						+ key.substring(key.lastIndexOf(":") + 1)
						+ "\" so SKIPPING");
				continue;
			}
			if (refText.get(0) == null) {
				System.err.println("Warning: no reference for document \""
						+ docID + "\" segment \""
						+ key.substring(key.lastIndexOf(":") + 1)
						+ "\" so SKIPPING");
				continue;
			}

			MeteorStats stats = scorer.getMeteorStats(testText, refText);
			segStats.put(key, stats);
			docStats.get(docID).addStats(stats);
			aggStats.addStats(stats);
		}

		// Score documents
		for (String key : docKeys) {
			MeteorStats stats = docStats.get(key);
			scorer.computeMetrics(stats);
		}

		// Score system
		scorer.computeMetrics(aggStats);

		// Print scores (or stats)
		boolean ssOut = Boolean.parseBoolean(props.getProperty("ssOut"));

		// System
		PrintWriter out = new PrintWriter(sysID + "-sys.score");
		out.print(data.testSetID + "\t" + sysID + "\t");
		if (ssOut)
			out.println(aggStats.toString(" "));
		else
			out.println(aggStats.score);
		out.close();

		// Document
		out = new PrintWriter(sysID + "-doc.score");
		for (String key : docKeys) {
			MeteorStats stats = docStats.get(key);
			out.print(data.testSetID + "\t" + sysID + "\t" + key + "\t");
			if (ssOut)
				out.println(stats.toString(" "));
			else
				out.println(stats.score);
		}
		out.close();

		// Segment
		out = new PrintWriter(sysID + "-seg.score");
		for (String key : segKeys) {
			MeteorStats stats = segStats.get(key);
			if (stats == null)
				continue;
			String docID = key.substring(0, key.indexOf(":"));
			String segID = key.substring(key.lastIndexOf(":") + 1);
			out.print(data.testSetID + "\t" + sysID + "\t" + docID + "\t"
					+ segID + "\t");
			if (ssOut)
				out.println(stats.toString(" "));
			else
				out.println(stats.score);
		}
		out.close();

		if (!ssOut) {
			printVerboseStats(aggStats, config);
		}
	}

	private static void printUsage() {
		System.out.println("METEOR version " + Constants.VERSION);
		System.out.println("Usage: java -jar meteor.jar <test> <reference> "
				+ "[options]");
		System.out.println();
		System.out.println("Options:");
		System.out.println("-l language\t\t\tOne of: en cz de es fr");
		System.out.println("-t task\t\t\t\tOne of: af rank hter");
		System.out
				.println("-p \"alpha beta gamma\"\t\tCustom parameters (overrides default)");
		System.out
				.println("-m \"module1 module2 ...\"\tSpecify modules (overrides default)");
		System.out.println("\t\t\t\t  One of: exact stem synonym paraphrase");
		System.out
				.println("-w \"weight1 weight2 ...\"\tSpecify module weights (overrides default)");
		System.out.println("-s systemID\t\t\tNot usually required");
		System.out
				.println("-r refCount\t\t\tNumber of references (plaintext only)");
		System.out.println("-x maxComputations\t\tKeep speed reasonable");
		System.out.println("-d synonymDirectory\t\t(if not default)");
		System.out.println("-a paraphraseFile\t\t(if not default)");
		System.out.println("-j jobs\t\t\t\tNumber of jobs to run (nBest only)");
		System.out
				.println("-normalize\t\t\tConvert symbols and tokenize (plaintext input only)");
		System.out
				.println("-keepPunctuation\t\tConsider punctuation when aligning sentences");
		System.out.println("-sgml\t\t\t\tInput is in SGML format");
		System.out.println("-nBest\t\t\t\tInput is in nBest format");
		System.out
				.println("-oracle\t\t\t\tOutput oracle translation (nBest only)");
		System.out
				.println("-ssOut\t\t\t\tOutput sufficient statistics instead of scores");
		System.out.println();
		System.out.println("Default settings are stored in the "
				+ "meteor.properties file");
	}

	private static void printVerboseStats(MeteorStats stats,
			MeteorConfiguration config) {
		printVerboseStats(stats, config, "\nSystem level statistics:\n");
	}

	private static void printVerboseStats(MeteorStats stats,
			MeteorConfiguration config, String header) {
		System.out.println(header);
		System.out.println("Stage\tTest Matches (weighted)\tReference "
				+ "Matches (weighted)");
		ArrayList<Double> weights = config.getModuleWeights();
		for (int i = 0; i < weights.size(); i++)
			System.out.println((i + 1) + "\t"
					+ stats.testTotalStageMatches.get(i) + " ("
					+ stats.testWeightedStageMatches.get(i) * weights.get(i)
					+ ")\t\t" + stats.referenceTotalStageMatches.get(i) + " ("
					+ stats.referenceWeightedStageMatches.get(i)
					* weights.get(i) + ")");
		System.out.println("Total\t" + stats.testTotalMatches + " ("
				+ stats.testWeightedMatches + ")\t\t"
				+ stats.referenceTotalMatches + " ("
				+ stats.referenceWeightedMatches + ")");
		System.out.println();
		System.out.println("Test words:\t\t" + stats.testLength);
		System.out.println("Reference words:\t" + stats.referenceLength);
		System.out.println("Chunks:\t\t\t" + stats.chunks);
		System.out.println("Precision:\t\t" + stats.precision);
		System.out.println("Recall:\t\t\t" + stats.recall);
		System.out.println("f1:\t\t\t" + stats.f1);
		System.out.println("fMean:\t\t\t" + stats.fMean);
		System.out.println("Fragmentation penalty:\t" + stats.fragPenalty);
		System.out.println();
		System.out.println("Final score:\t\t" + stats.score);
	}

	public static void updatePropertiesWithArgs(Properties props,
			String[] args, int startIndex) {
		int curArg = startIndex;
		while (curArg < args.length) {
			if (args[curArg].equals("-l")) {
				props.setProperty("language", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-t")) {
				props.setProperty("task", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-p")) {
				props.setProperty("parameters", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-m")) {
				props.setProperty("modules", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-w")) {
				props.setProperty("moduleWeights", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-s")) {
				props.setProperty("system", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-r")) {
				props.setProperty("refCount", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-x")) {
				props.setProperty("maxComp", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-d")) {
				props.setProperty("synDir", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-a")) {
				props.setProperty("paraDir", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-j")) {
				props.setProperty("jobs", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-normalize")) {
				props.setProperty("normalize", "true");
				curArg += 1;
			} else if (args[curArg].equals("-nBest")) {
				props.setProperty("nBest", "true");
				curArg += 1;
			} else if (args[curArg].equals("-oracle")) {
				props.setProperty("oracle", "true");
				curArg += 1;
			} else if (args[curArg].equals("-sgml")) {
				props.setProperty("sgml", "true");
				curArg += 1;
			} else if (args[curArg].equals("-keepPunctuation")) {
				props.setProperty("keepPunctuation", "true");
				curArg += 1;
			} else if (args[curArg].equals("-ssOut")) {
				props.setProperty("ssOut", "true");
				curArg += 1;
			} else {
				System.err.println("Error: unknown option \"" + args[curArg]
						+ "\"");
				System.exit(1);
			}
			String params = props.getProperty("parameters");
			if (!params.equals("default"))
				props.setProperty("task", "custom (" + params + ")");
		}
	}

	public static MeteorConfiguration createConfiguration(Properties props) {

		// Default configuration
		MeteorConfiguration config = new MeteorConfiguration();

		// Language
		String language = props.getProperty("language");
		if (!language.equals("default"))
			config.setLanguage(language);

		// Task
		String task = props.getProperty("task");
		config.setTask(task);

		// Parameters
		String parameters = props.getProperty("parameters");
		if (!parameters.equals("default")) {
			ArrayList<Double> params = new ArrayList<Double>();
			StringTokenizer p = new StringTokenizer(parameters);
			while (p.hasMoreTokens())
				params.add(Double.parseDouble(p.nextToken()));
			config.setParameters(params);
		}

		// Weights
		String weights = props.getProperty("moduleWeights");
		if (!weights.equals("default")) {
			ArrayList<Double> weightList = new ArrayList<Double>();
			StringTokenizer wtok = new StringTokenizer(weights);
			while (wtok.hasMoreTokens())
				weightList.add(Double.parseDouble(wtok.nextToken()));
			config.setModuleWeights(weightList);
		}

		// Modules
		String modules = props.getProperty("modules");
		if (!modules.equals("default")) {
			ArrayList<String> modList = new ArrayList<String>();
			StringTokenizer mods = new StringTokenizer(modules);
			while (mods.hasMoreTokens())
				modList.add(mods.nextToken());
			config.setModulesByName(modList);
			// Update weights to match number of modules
			ArrayList<Double> weightList = config.getModuleWeights();
			ArrayList<Double> sizedWeightList = new ArrayList<Double>();
			for (int i = 0; i < modList.size(); i++) {
				if (i < weightList.size())
					sizedWeightList.add(weightList.get(i));
				else
					sizedWeightList.add(0.0);
			}
			config.setModuleWeights(sizedWeightList);
		}

		// MaxComp
		String maxComp = props.getProperty("maxComp");
		if (!maxComp.equals("default"))
			config.setMaxComp(Integer.parseInt(maxComp));

		// SynDir
		String synDir = (props.getProperty("synDir"));
		if (!synDir.equals("default"))
			try {
				// This should not ever throw a malformed url exception
				config.setSynDirURL((new File(synDir)).toURI().toURL());
			} catch (MalformedURLException ex) {
				System.err.println("Error: Synonym directory URL NOT set");
				ex.printStackTrace();
			}

		// ParaDir
		String paraDir = (props.getProperty("paraDir"));
		if (!paraDir.equals("default"))
			try {
				// This should not ever throw a malformed url exception
				config.setParaDirURL((new File(paraDir)).toURI().toURL());
			} catch (MalformedURLException ex) {
				System.err.println("Error: Paraphrase directory URL NOT set");
				ex.printStackTrace();
			}

		// Normalization & SGML
		boolean normalize = Boolean
				.parseBoolean(props.getProperty("normalize"));
		boolean sgml = Boolean.parseBoolean(props.getProperty("sgml"));
		boolean keepPunctuation = Boolean.parseBoolean(props
				.getProperty("keepPunctuation"));
		boolean nBest = Boolean.parseBoolean(props.getProperty("nBest"));
		if (sgml) {
			if (keepPunctuation)
				config.setNormalization(Constants.NORMALIZE_KEEP_PUNCT);
			else
				config.setNormalization(Constants.NORMALIZE_NO_PUNCT);
		} else {
			if (nBest) {
				config.setNormalization(Constants.NO_NORMALIZE);
			} else if (normalize) {
				if (keepPunctuation)
					config.setNormalization(Constants.NORMALIZE_KEEP_PUNCT);
				else
					config.setNormalization(Constants.NORMALIZE_NO_PUNCT);
			} else
				config.setNormalization(Constants.NO_NORMALIZE);
		}
		return config;
	}
}
