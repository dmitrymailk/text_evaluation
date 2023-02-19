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

import edu.cmu.meteor.aligner.Aligner;
import edu.cmu.meteor.aligner.Alignment;
import edu.cmu.meteor.util.Constants;
import edu.cmu.meteor.util.Normalizer;

/**
 * Entry point class which oversees Meteor scoring. Instantiate with either the
 * default configuration (no args) or an existing MeteorConfiguration. Call the
 * getMeteorStats() methods to obtain MeteorStats objects which include a score
 * field
 * 
 */
public class MeteorScorer {

	private Aligner aligner;
	private String language;
	private int langID;

	private boolean normalize;
	// Only used if we normalize
	private boolean keepPunctuation;

	// Parameters
	private double alpha;
	private double beta;
	private double gamma;

	// Weights
	private ArrayList<Double> moduleWeights;

	/**
	 * Use default configuration
	 * 
	 */
	public MeteorScorer() {
		MeteorConfiguration config = new MeteorConfiguration();
		loadConfiguration(config);
	}

	/**
	 * Use a custom configuration
	 * 
	 * @param config
	 */
	public MeteorScorer(MeteorConfiguration config) {
		loadConfiguration(config);
	}

	/**
	 * Create a new scorer that shares thread-safe resources with an existing
	 * scorer
	 * 
	 * @param scorer
	 */
	public MeteorScorer(MeteorScorer scorer) {
		language = scorer.language;
		langID = scorer.langID;
		normalize = scorer.normalize;
		keepPunctuation = scorer.keepPunctuation;
		alpha = scorer.alpha;
		beta = scorer.beta;
		gamma = scorer.gamma;
		moduleWeights = new ArrayList<Double>(scorer.moduleWeights);
		aligner = new Aligner(scorer.aligner);
	}

	/**
	 * Load configuration (only used by constructor)
	 * 
	 * @param config
	 */
	private void loadConfiguration(MeteorConfiguration config) {
		language = config.getLanguage();
		langID = config.getLangID();
		setNormalize(config.getNormalization());
		ArrayList<Double> parameters = config.getParameters();
		alpha = parameters.get(0);
		beta = parameters.get(1);
		gamma = parameters.get(2);
		moduleWeights = config.getModuleWeights();
		aligner = new Aligner(language, config.getModules(), config
				.getModuleWeights(), config.getMaxComp(),
				config.getSynDirURL(), config.getParaDirURL());
	}

	/**
	 * Set normalization type
	 * 
	 * @param normtype
	 */
	private void setNormalize(int normtype) {
		if (normtype == Constants.NORMALIZE_KEEP_PUNCT) {
			this.normalize = true;
			this.keepPunctuation = true;
		} else if (normtype == Constants.NORMALIZE_NO_PUNCT) {
			this.normalize = true;
			this.keepPunctuation = false;
		} else {
			// Assume MeteorConfiguration.NO_NORMALIZE
			this.normalize = false;
			this.keepPunctuation = true;
		}
	}

	/**
	 * Get the Meteor sufficient statistics for a test / reference pair
	 * 
	 * @param test
	 * @param reference
	 * @return
	 */
	public MeteorStats getMeteorStats(String test, String reference) {
		if (normalize) {
			test = Normalizer.normalizeLine(test, langID, keepPunctuation);
			reference = Normalizer.normalizeLine(reference, langID,
					keepPunctuation);
		}
		Alignment alignment = aligner.align(test, reference);
		return getMeteorStats(alignment);
	}

	/**
	 * Get the Meteor sufficient statistics for a test give a list of references
	 * 
	 * @param test
	 * @param references
	 * @return
	 */
	public MeteorStats getMeteorStats(String test, ArrayList<String> references) {
		if (normalize) {
			test = Normalizer.normalizeLine(test, langID, keepPunctuation);
			ArrayList<String> normRefs = new ArrayList<String>();
			for (String ref : references) {
				normRefs.add(Normalizer.normalizeLine(ref, langID,
						keepPunctuation));
			}
			references = normRefs;
		}
		MeteorStats stats = new MeteorStats();
		stats.score = -1;
		for (String reference : references) {
			Alignment alignment = aligner.align(test, reference);
			MeteorStats curStats = getMeteorStats(alignment);
			if (curStats.score > stats.score)
				stats = curStats;
		}
		return stats;
	}

	/**
	 * Get the Meteor sufficient statistics for an alignment
	 * 
	 * @param alignment
	 * @return
	 */
	public MeteorStats getMeteorStats(Alignment alignment) {
		MeteorStats stats = new MeteorStats();
		/*
		 * TODO: USE MATCHES FROM BOTH STRINGS
		 */
		stats.testLength = alignment.words1.size();
		stats.referenceLength = alignment.words2.size();

		stats.testTotalMatches = alignment.string1Matches;
		stats.referenceTotalMatches = alignment.string2Matches;

		stats.testTotalStageMatches = new ArrayList<Integer>(
				alignment.stageTotalMatches1);
		stats.referenceTotalStageMatches = new ArrayList<Integer>(
				alignment.stageTotalMatches2);

		stats.testWeightedStageMatches = new ArrayList<Double>(
				alignment.stageWeightedMatches1);
		stats.referenceWeightedStageMatches = new ArrayList<Double>(
				alignment.stageWeightedMatches2);

		stats.chunks = alignment.numChunks;

		// Length cost
		double ratio = ((double) stats.testLength)
				/ ((double) stats.referenceLength);
		double maxRatio = (2 - Math.pow((double) stats.referenceLength,
				1.0 / 4.4) / 4.4);
		// TODO: This could probably be refined
		double cost = 4 * Math.max(0.0, ((double) stats.testTotalMatches)
				* (ratio - maxRatio));
		if (Double.isNaN(cost))
			stats.lengthCost = 0;
		else
			stats.lengthCost = cost;

		// We also need actual Meteor scores so we can pick the best ref
		computeMetrics(stats);
		return stats;
	}

	/**
	 * Get the Meteor score given sufficient statistics
	 * 
	 * @param stats
	 */
	public void computeMetrics(MeteorStats stats) {

		// Apply module weights to test and reference matches
		for (int i = 0; i < moduleWeights.size(); i++)
			stats.testWeightedMatches += stats.testWeightedStageMatches.get(i)
					* moduleWeights.get(i);
		for (int i = 0; i < moduleWeights.size(); i++)
			stats.referenceWeightedMatches += stats.referenceWeightedStageMatches
					.get(i)
					* moduleWeights.get(i);

		// Precision = test matches / test length
		stats.precision = (stats.testWeightedMatches - stats.lengthCost)
				/ stats.testLength;
		// Recall = ref matches / ref length
		stats.recall = (stats.referenceWeightedMatches - stats.lengthCost)
				/ stats.referenceLength;
		// F1 = 2pr / (p + r) [not part of final score]
		stats.f1 = (2 * stats.precision * stats.recall)
				/ (stats.precision + stats.recall);
		// Fmean = 1 / alpha-weighted average of p and r
		stats.fMean = 1.0 / (((1 - alpha) / stats.precision) + (alpha / stats.recall));
		// Fragmentation
		double frag;
		// Case if test = ref
		if (stats.testTotalMatches == stats.testLength
				&& stats.referenceTotalMatches == stats.referenceLength
				&& stats.chunks == 1)
			frag = 0;
		else
			frag = ((double) stats.chunks) / stats.testTotalMatches;
		// Fragmentation penalty
		stats.fragPenalty = gamma * Math.pow(frag, beta);
		// Score
		double score = stats.fMean * (1.0 - stats.fragPenalty);

		// Catch division by zero
		if (Double.isNaN(score))
			stats.score = 0;
		else
			// score >= 0.0
			stats.score = Math.max(score, 0.0);
	}
}
