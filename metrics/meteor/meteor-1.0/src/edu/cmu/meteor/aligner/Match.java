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

public class Match {

	public int start; // start of the match (reference)
	public int length; // length of this match (reference)
	public int matchStringStart; // start of this match (test)
	public int matchStringLength; // length of this match (test)
	public double prob; // probability supplied by matcher
	public int stage; // stage this match was found

	public Match() {
		start = -1;
		length = 0;
		matchStringStart = -1;
		matchStringLength = 0;
	}

	public Match(Match match) {
		start = match.start;
		length = match.length;
		matchStringStart = match.matchStringStart;
		matchStringLength = match.matchStringLength;
		prob = match.prob;
		stage = match.stage;
	}
}
