/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

/*
 * This is the alignment object which stores the initial and final
 * information for an alignment between two sentences.  While this
 * data can be freely modified, the most common use involves creating
 * an alignment object for two sentences, passing it to an aligner
 * for processing, and then reading the results.
 */

package edu.cmu.meteor.aligner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

public class Alignment {
	/* Initial and final data structures */

	public ArrayList<String> words1;
	public ArrayList<String> words2;

	public ArrayList<Match> matches;

	public int string1Matches;
	public int string2Matches;

	public ArrayList<Integer> stageTotalMatches1;
	public ArrayList<Integer> stageTotalMatches2;

	public ArrayList<Double> stageWeightedMatches1;
	public ArrayList<Double> stageWeightedMatches2;

	public int numChunks;
	public double avgChunkLength;

	/* Intermediate data structures */

	public HashSet<Integer> alignedWords1;
	public HashSet<Integer> alignedWords2;

	// Lines as Strings
	public Alignment(String line1, String line2) {
		words1 = tokenizeLcLine(line1);
		words2 = tokenizeLcLine(line2);
		initData();
	}

	// Lines as ArrayLists of tokenized lowercased Strings
	public Alignment(ArrayList<String> words1, ArrayList<String> words2) {
		this.words1 = new ArrayList<String>(words1);
		this.words2 = new ArrayList<String>(words2);
		initData();
	}

	// Initialize values
	private void initData() {
		matches = new ArrayList<Match>();

		string1Matches = 0;
		string2Matches = 0;

		stageTotalMatches1 = new ArrayList<Integer>();
		stageTotalMatches2 = new ArrayList<Integer>();

		stageWeightedMatches1 = new ArrayList<Double>();
		stageWeightedMatches2 = new ArrayList<Double>();

		numChunks = 0;
		avgChunkLength = 0;

		alignedWords1 = new HashSet<Integer>();
		alignedWords2 = new HashSet<Integer>();

		for (int i = 0; i < words2.size(); i++) {
			matches.add(new Match());
		}
	}

	// Tokenize input line
	private ArrayList<String> tokenizeLcLine(String line) {
		ArrayList<String> tokens = new ArrayList<String>();
		StringTokenizer tok = new StringTokenizer(line);
		while (tok.hasMoreTokens())
			tokens.add(tok.nextToken().toLowerCase());
		return tokens;
	}
}
