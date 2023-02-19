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
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.cmu.meteor.aligner.Aligner;
import edu.cmu.meteor.aligner.Alignment;
import edu.cmu.meteor.aligner.Match;
import edu.cmu.meteor.util.Constants;

public class Matcher {
	public static void main(String[] args) throws Exception {

		// Usage
		if (args.length < 2) {
			System.out.println("METEOR Aligner version " + Constants.VERSION);
			System.out.println("Usage: java -cp meteor.jar Matcher "
					+ "<test> <reference> [options]");
			System.out.println();
			System.out.println("Options:");
			System.out.println("-l language\t\t\tOne of: en cz de es fr");
			System.out
					.println("-m \"module1 module2 ...\"\tSpecify modules (overrides default)");
			System.out
					.println("\t\t\t\t  One of: exact stem synonym paraphrase");
			System.out.println("-x maxComputations\t\tKeep speed reasonable");
			System.out.println("-d synonymDirectory\t\t(if not default)");
			System.out.println("-a paraphraseFile\t\t(if not default)");
			System.out.println();
			System.out
					.println("Default settings are stored in the matcher.properties file bundled in the JAR");
			return;
		}

		// Files
		String test = args[0];
		String ref = args[1];

		// Defaults
		String propFile = "matcher.properties";
		Properties props = new Properties();
		try {
			props.load(ClassLoader.getSystemResource(propFile).openStream());
		} catch (Exception ex) {
			System.err.println("Error: Could not load properties file:");
			ex.printStackTrace();
			System.exit(1);
		}

		// Input args
		int curArg = 2;
		while (curArg < args.length) {
			if (args[curArg].equals("-l")) {
				props.setProperty("language", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-x")) {
				props.setProperty("maxcomp", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-d")) {
				props.setProperty("synDir", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-a")) {
				props.setProperty("paraDir", args[curArg + 1]);
				curArg += 2;
			} else if (args[curArg].equals("-m")) {
				props.setProperty("modules", args[curArg + 1]);
				curArg += 2;
			}
		}

		// Language
		String language = props.getProperty("language");
		if (language.equals("default"))
			language = "english";
		language = Constants.normLanguageName(language);

		// Synonym Location
		String synDir = props.getProperty("synDir");
		URL synURL;
		if (synDir.equals("default"))
			synURL = Constants.DEFAULT_SYN_DIR_URL;
		else
			synURL = (new File(synDir)).toURI().toURL();

		// Paraphrase Location
		String paraDir = props.getProperty("paraDir");
		URL paraURL;
		if (paraDir.equals("default"))
			paraURL = Constants.DEFAULT_PARA_DIR_URL;
		else
			paraURL = (new File(paraDir)).toURI().toURL();

		// Max Computations
		String mx = props.getProperty("maxcomp");
		int maxComp = 0;
		if (mx.equals("default"))
			maxComp = Constants.DEFAULT_MAXCOMP;
		else
			maxComp = Integer.parseInt(mx);

		// Modules
		String modNames = props.getProperty("modules");
		if (modNames.equals("default"))
			modNames = "exact stem synonym paraphrase";
		ArrayList<Integer> modules = new ArrayList<Integer>();
		ArrayList<Double> moduleWeights = new ArrayList<Double>();
		StringTokenizer mods = new StringTokenizer(modNames);
		while (mods.hasMoreTokens()) {
			int module = Constants.getModuleID(mods.nextToken());
			modules.add(module);
			moduleWeights.add(1.0);
		}

		// Construct aligner
		Aligner aligner = new Aligner(language, modules, moduleWeights,
				maxComp, synURL, paraURL);

		ArrayList<String> lines1 = new ArrayList<String>();
		ArrayList<String> lines2 = new ArrayList<String>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(test));
			String line;
			while ((line = in.readLine()) != null)
				lines1.add(line);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader(ref));
			String line;
			while ((line = in.readLine()) != null)
				lines2.add(line);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		if (lines1.size() != lines2.size()) {
			System.err.println("Error: files not of same length.");
			System.err.println("Test: " + lines1.size());
			System.err.println("Reference: " + lines2.size());
			System.exit(1);
		}

		ArrayList<Alignment> alignments = new ArrayList<Alignment>();

		for (int i = 0; i < lines1.size(); i++) {
			alignments.add(aligner.align(lines1.get(i), lines2.get(i)));
		}

		// Output results
		for (int i = 0; i < alignments.size(); i++) {
			System.out.println("Alignment " + (i));
			System.out.println(lines1.get(i));
			System.out.println(lines2.get(i));
			System.out.println("Line2 Index\tLine1 Index\tModule Stage\tScore");
			for (int j = 0; j < alignments.get(i).matches.size(); j++) {
				Match m = alignments.get(i).matches.get(j);
				if (m.matchStringStart != -1) {
					// Second string word
					System.out.print(m.start + "\t\t");
					// First string word
					System.out.print(m.matchStringStart + "\t\t");
					// Module stage
					System.out.print(m.stage + "\t\t");
					// Score
					System.out.println(m.prob * moduleWeights.get(m.stage));
				}
			}
			System.out.println();
		}
	}
}
