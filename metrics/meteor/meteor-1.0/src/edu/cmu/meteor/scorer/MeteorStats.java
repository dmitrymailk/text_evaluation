/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

package edu.cmu.meteor.scorer;

import java.util.ArrayList;
import java.util.Scanner;

import edu.cmu.meteor.util.Constants;

/**
 * Class used to hold several Meteor statistics, including final score
 * 
 */
public class MeteorStats {
	/* Aggregable statistics */

	public int testLength;
	public int referenceLength;

	public int testTotalMatches;
	public int referenceTotalMatches;

	public ArrayList<Integer> testTotalStageMatches;
	public ArrayList<Integer> referenceTotalStageMatches;

	/**
	 * Note: these are weighted by the probability from the matcher, not the
	 * weight for the module. This way, alignment stats can be rescored using
	 * different module weights (in tuning, for example)
	 */
	public ArrayList<Double> testWeightedStageMatches;
	public ArrayList<Double> referenceWeightedStageMatches;

	public int chunks;
	public double lengthCost;

	/* Calculated statistics */

	/**
	 * Note: these do include module weights
	 */
	public double testWeightedMatches;
	public double referenceWeightedMatches;

	public double precision;
	public double recall;
	public double f1;
	public double fMean;
	public double fragPenalty;

	/**
	 * Score is required to select the best reference
	 */
	public double score;

	public MeteorStats() {
		testLength = 0;
		referenceLength = 0;

		testTotalMatches = 0;
		referenceTotalMatches = 0;

		testTotalStageMatches = new ArrayList<Integer>();
		referenceTotalStageMatches = new ArrayList<Integer>();

		testWeightedStageMatches = new ArrayList<Double>();
		referenceWeightedStageMatches = new ArrayList<Double>();

		chunks = 0;
		lengthCost = 0;

		testWeightedMatches = 0;
		referenceWeightedMatches = 0;
	}

	/**
	 * Aggregate SS (except score), result stored in this instance
	 * 
	 * @param ss
	 */
	public void addStats(MeteorStats ss) {

		testLength += ss.testLength;
		referenceLength += ss.referenceLength;

		testTotalMatches += ss.testTotalMatches;
		referenceTotalMatches += ss.referenceTotalMatches;

		int sizeDiff = ss.referenceTotalStageMatches.size()
				- referenceTotalStageMatches.size();
		for (int i = 0; i < sizeDiff; i++) {
			testTotalStageMatches.add(0);
			referenceTotalStageMatches.add(0);
			testWeightedStageMatches.add(0.0);
			referenceWeightedStageMatches.add(0.0);
		}
		for (int i = 0; i < ss.testTotalStageMatches.size(); i++)
			testTotalStageMatches.set(i, testTotalStageMatches.get(i)
					+ ss.testTotalStageMatches.get(i));
		for (int i = 0; i < ss.referenceTotalStageMatches.size(); i++)
			referenceTotalStageMatches.set(i, referenceTotalStageMatches.get(i)
					+ ss.referenceTotalStageMatches.get(i));
		for (int i = 0; i < ss.testWeightedStageMatches.size(); i++)
			testWeightedStageMatches.set(i, testWeightedStageMatches.get(i)
					+ ss.testWeightedStageMatches.get(i));
		for (int i = 0; i < ss.referenceWeightedStageMatches.size(); i++)
			referenceWeightedStageMatches.set(i, referenceWeightedStageMatches
					.get(i)
					+ ss.referenceWeightedStageMatches.get(i));

		if (!(ss.testTotalMatches == ss.testLength
				&& ss.referenceTotalMatches == ss.referenceLength && ss.chunks == 1))
			chunks += ss.chunks;
		lengthCost += ss.lengthCost;

		// Score does not aggregate
	}

	/**
	 * Stats are output in lines:
	 * 
	 * tstLen refLen stage1tstTotalMatches stage1refTotalMatches
	 * stage1tstWeightedMatches stage1refWeightedMatches s2tTM s2rTM s2tWM s2rWM
	 * s3tTM s3rTM s3tWM s3rWM s4tTM s4rTM s4tWM s4rWM chunks lenCost
	 * 
	 * ex: 15 14 8 8 8.0 8.0 2 2 2.0 2.0 1 1 1.0 1.0 3 2 0.6 0.4 3 0.25
	 * 
	 * @param delim
	 */
	public String toString(String delim) {
		StringBuilder sb = new StringBuilder();
		sb.append(testLength + delim);
		sb.append(referenceLength + delim);
		for (int i = 0; i < Constants.MAX_MODULES; i++) {
			if (i < testTotalStageMatches.size()) {
				sb.append(testTotalStageMatches.get(i) + delim);
				sb.append(referenceTotalStageMatches.get(i) + delim);
				sb.append(testWeightedStageMatches.get(i) + delim);
				sb.append(referenceWeightedStageMatches.get(i) + delim);
			} else {
				sb.append(0 + delim);
				sb.append(0 + delim);
				sb.append(0 + delim);
				sb.append(0 + delim);
			}
		}
		sb.append(chunks + delim);
		sb.append(lengthCost);
		return sb.toString();
	}

	public String toString() {
		return this.toString(" ");
	}

	/**
	 * Use a string from the toString() method to create a MeteorStats object.
	 * 
	 * @param ssString
	 */
	public MeteorStats(String ssString) {
		Scanner s = new Scanner(ssString);

		testLength = s.nextInt();
		referenceLength = s.nextInt();

		testTotalMatches = 0;
		referenceTotalMatches = 0;

		testTotalStageMatches = new ArrayList<Integer>();
		referenceTotalStageMatches = new ArrayList<Integer>();

		testWeightedStageMatches = new ArrayList<Double>();
		referenceWeightedStageMatches = new ArrayList<Double>();

		for (int i = 0; i < Constants.MAX_MODULES; i++) {

			int tstTotal = s.nextInt();
			int refTotal = s.nextInt();

			testTotalMatches += tstTotal;
			referenceTotalMatches += refTotal;

			testTotalStageMatches.add(tstTotal);
			referenceTotalStageMatches.add(refTotal);

			double tstWeighted = s.nextDouble();
			double refWeighted = s.nextDouble();

			testWeightedStageMatches.add(tstWeighted);
			referenceWeightedStageMatches.add(refWeighted);
		}

		chunks = s.nextInt();
		lengthCost = s.nextDouble();
	}
}