/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

package edu.cmu.meteor.aligner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class Stage {

	/* Initial and Final data */
	public ArrayList<Match> matches;

	public Hashtable<Integer, ArrayList<Integer>> multiChoices;
	public ArrayList<Integer> choiceKeys;
	public HashSet<Integer> choiceIdx;

	/* Intermediate data */

	// Data structure to keep track of what pos have been used
	public Hashtable<Integer, ArrayList<Integer>> usedPos;
	public Hashtable<Long, Integer> scoreCache;

	public ArrayList<Match> bestMatchSoFar;
	public ArrayList<Match> currentMatch;

	public int bestScoreSoFar;
	public int scoreSoFar;

	Stage() {
		multiChoices = new Hashtable<Integer, ArrayList<Integer>>();
		choiceKeys = new ArrayList<Integer>();
		choiceIdx = new HashSet<Integer>();

		matches = new ArrayList<Match>();

		usedPos = new Hashtable<Integer, ArrayList<Integer>>();
		scoreCache = new Hashtable<Long, Integer>();
		bestMatchSoFar = new ArrayList<Match>();
		currentMatch = new ArrayList<Match>();

		bestScoreSoFar = -1;
		scoreSoFar = 0;
	}
}
