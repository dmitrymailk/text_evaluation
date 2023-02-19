/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

package edu.cmu.meteor.aligner;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.tartarus.snowball.SnowballStemmer;

import edu.cmu.meteor.util.Constants;

public class Aligner {

	/* Configuration */

	private String language;

	private int moduleCount;
	private ArrayList<Integer> modules;
	// TODO: Use weights as part of search
	private ArrayList<Double> moduleWeights;

	private int maxComputations;
	private int numComputations;

	SnowballStemmer stemmer;

	SynonymDictionary synonyms;

	ParaphraseDictionary paraphrase;

	public Aligner(String language, ArrayList<Integer> modules) {
		this.maxComputations = Constants.DEFAULT_MAXCOMP;
		setupModules(language, modules, Constants.DEFAULT_SYN_DIR_URL,
				Constants.DEFAULT_PARA_DIR_URL);
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights) {
		this.maxComputations = Constants.DEFAULT_MAXCOMP;
		setupModules(language, modules, Constants.DEFAULT_SYN_DIR_URL,
				Constants.DEFAULT_PARA_DIR_URL);
		this.moduleWeights = moduleWeights;
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights, int maxComputations) {
		this.maxComputations = maxComputations;
		setupModules(language, modules, Constants.DEFAULT_SYN_DIR_URL,
				Constants.DEFAULT_PARA_DIR_URL);
		this.moduleWeights = moduleWeights;
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights, int maxComputations, URL synDirURL) {
		this.maxComputations = maxComputations;
		setupModules(language, modules, synDirURL,
				Constants.DEFAULT_PARA_DIR_URL);
		this.moduleWeights = moduleWeights;
	}

	public Aligner(String language, ArrayList<Integer> modules,
			ArrayList<Double> moduleWeights, int maxComputations,
			URL synDirURL, URL paraDirURL) {
		this.maxComputations = maxComputations;
		setupModules(language, modules, synDirURL, paraDirURL);
		this.moduleWeights = moduleWeights;
	}

	public Aligner(Aligner aligner) {
		this.maxComputations = aligner.maxComputations;
		this.moduleCount = aligner.moduleCount;
		this.language = aligner.language;
		this.modules = new ArrayList<Integer>(aligner.modules);
		this.moduleWeights = new ArrayList<Double>(aligner.moduleWeights);
		for (int module : this.modules) {
			if (module == Constants.MODULE_STEM) {
				// Each aligner needs its own stemmer
				this.stemmer = Constants.newStemmer(this.language);
			} else if (module == Constants.MODULE_SYNONYM) {
				// Dictionaries can be shared
				this.synonyms = aligner.synonyms;
			} else if (module == Constants.MODULE_PARAPHRASE) {
				// Dictionaries can be shared
				this.paraphrase = aligner.paraphrase;
			}
		}
	}

	private void setupModules(String language, ArrayList<Integer> modules,
			URL synDirURL, URL paraDirURL) {
		this.language = Constants.normLanguageName(language);
		this.moduleCount = modules.size();
		this.modules = modules;
		this.moduleWeights = new ArrayList<Double>();
		for (int i = 0; i < this.modules.size(); i++) {
			int module = this.modules.get(i);
			if (module == Constants.MODULE_EXACT) {
				this.moduleWeights.add(Constants.DEFAULT_WEIGHT_EXACT);
			} else if (module == Constants.MODULE_STEM) {
				this.moduleWeights.add(Constants.DEFAULT_WEIGHT_STEM);
				this.stemmer = Constants.newStemmer(this.language);
			} else if (module == Constants.MODULE_SYNONYM) {
				this.moduleWeights.add(Constants.DEFAULT_WEIGHT_SYNONYM);
				try {
					URL synFileURL = new URL(synDirURL.toString() + "/"
							+ this.language + ".synsets");
					URL excFileURL = new URL(synDirURL.toString() + "/"
							+ this.language + ".exceptions");
					this.synonyms = new SynonymDictionary(synFileURL,
							excFileURL);
				} catch (IOException ex) {
					System.err
							.println("Error: Synonym dictionary could not be loaded:");
					ex.printStackTrace();
					System.err
							.println("Falling back to EXACT module for stage "
									+ i);
					this.synonyms = null;
					this.modules.set(i, Constants.MODULE_EXACT);
				}
			} else if (module == Constants.MODULE_PARAPHRASE) {
				this.moduleWeights.add(Constants.DEFAULT_WEIGHT_PARAPHRASE);
				try {
					this.paraphrase = new ParaphraseDictionary(paraDirURL);
				} catch (IOException ex) {
					System.err
							.println("Error: Paraphrase dictionary could not be loaded:");
					ex.printStackTrace();
					System.err
							.println("Falling back to EXACT module for stage "
									+ i);
					this.paraphrase = null;
					this.modules.set(i, Constants.MODULE_EXACT);
				}
			}
		}
	}

	public Alignment align(String line1, String line2) {
		Alignment a = new Alignment(line1, line2);
		match(a);
		return a;
	}

	public Alignment align(ArrayList<String> words1, ArrayList<String> words2) {
		Alignment a = new Alignment(words1, words2);
		match(a);
		return a;
	}

	private void match(Alignment a) {

		numComputations = 0;

		// Stages
		for (int stageNum = 0; stageNum < moduleCount; stageNum++) {

			// Stage object to be initialized by the matcher
			// module based on the current data in "a"
			Stage s;

			// Get the matcher for this stage
			int matcher = modules.get(stageNum);

			// Match with the appropriate module
			if (matcher == Constants.MODULE_EXACT) {
				// Exact just needs the alignment object
				s = ExactMatcher.match(a);
			} else if (matcher == Constants.MODULE_STEM) {
				// Stem also need the stemmer
				s = StemMatcher.match(a, stemmer);
			} else if (matcher == Constants.MODULE_SYNONYM) {
				// Synonym also need the synonym dictionary
				s = SynonymMatcher.match(a, synonyms);
			} else if (matcher == Constants.MODULE_PARAPHRASE) {
				// Paraphrase also need the paraphrase dictionary
				s = ParaphraseMatcher.match(a, paraphrase);
			} else {
				System.err.println("Matcher not recognized: " + matcher);
				return;
			}

			// Now that there is a packed alignment, look for the best scoring
			// 1 to 1 alignment. This is the same process for every stage.

			for (int key : s.choiceKeys) {
				int choices = s.multiChoices.get(key).size();
				ArrayList<Integer> choiceList = new ArrayList<Integer>();
				for (int j = 0; j < choices; j++) {
					choiceList.add(0);
				}
				s.usedPos.put(key, choiceList);
			}

			s.currentMatch = new ArrayList<Match>();
			for (Match m : s.matches)
				s.currentMatch.add(new Match(m));

			// Find the best alignment
			if (s.choiceIdx.size() > 0) {
				getBestMatch(0, s);
			} else {
				// This is only safe if "s" is not further modified before
				// going out of scope
				s.bestMatchSoFar = s.currentMatch;
				s.bestScoreSoFar = 0;
			}

			// Match totals for this stage
			int totalMatches1 = 0;
			int totalMatches2 = 0;
			double weightedMatches1 = 0;
			double weightedMatches2 = 0;
			for (int i = 0; i < s.bestMatchSoFar.size(); i++) {
				Match m = s.bestMatchSoFar.get(i);
				if (m.matchStringStart != -1) {
					totalMatches1 += m.matchStringLength;
					totalMatches2 += m.length;
					weightedMatches1 += m.matchStringLength * m.prob;
					weightedMatches2 += m.length * m.prob;
				}
			}
			a.stageTotalMatches1.add(totalMatches1);
			a.stageTotalMatches2.add(totalMatches2);
			a.stageWeightedMatches1.add(weightedMatches1);
			a.stageWeightedMatches2.add(weightedMatches2);

			// Copy best match to final alignment
			for (int i = 0; i < s.matches.size(); i++) {
				s.bestMatchSoFar.get(i).stage = stageNum;
				a.matches.set(s.matches.get(i).start, s.bestMatchSoFar.get(i));
			}

			// Mark words aligned
			for (int i = 0; i < a.matches.size(); i++) {
				if (a.matches.get(i).matchStringStart != -1) {
					a.alignedWords1.add(a.matches.get(i).matchStringStart);
					a.alignedWords2.add(i);
				}
			}
		}

		int chunkStartPointer = -1;

		for (int i = 0; i < a.matches.size(); i++) {
			Match m = a.matches.get(i);
			if (m.matchStringStart != -1) {
				// Total match count
				a.string1Matches += m.matchStringLength;
				a.string2Matches += m.length;
			}
			// Chunks
			if (chunkStartPointer != -1) {
				// there is an open chunk
				if (m.matchStringStart == -1) {
					chunkStartPointer = -1;
					a.numChunks++;
				}// TODO: match length
				else if (m.matchStringStart != (a.matches.get(i - 1).matchStringStart + 1)) {
					chunkStartPointer = i;
					a.numChunks++;
				}
			} else {
				// there aren't any open chunks
				if (m.matchStringStart != -1)
					chunkStartPointer = i;
			}
		}
		if (chunkStartPointer != -1)
			a.numChunks++;

		double avgMatches = ((double) (a.string1Matches + a.string2Matches)) / 2;
		a.avgChunkLength = (a.numChunks > 0) ? avgMatches / a.numChunks : 0;
	}

	private void getBestMatch(int index, Stage s) {
		numComputations++;
		if ((maxComputations != -1) && (s.bestScoreSoFar != -1)
				&& (numComputations > maxComputations)) {
			return;
		}

		while (index < s.matches.size()) {
			// Do we have multiple choices?
			if (s.choiceIdx.contains(index))
				break;
			index++;
		}
		if (index > s.matches.size() - 1) {
			if ((s.bestScoreSoFar == -1) || (s.scoreSoFar < s.bestScoreSoFar)) {
				s.bestMatchSoFar = new ArrayList<Match>();
				for (Match m : s.currentMatch)
					s.bestMatchSoFar.add(new Match(m));
				s.bestScoreSoFar = s.scoreSoFar;
			}
			return;
		}

		int startPos = -1;

		int word = s.matches.get(index).matchStringStart;
		ArrayList<Integer> wordPos = s.usedPos.get(word);
		ArrayList<Integer> wordChoices = s.multiChoices.get(word);

		int size = wordPos != null ? wordPos.size() : 0;

		for (int i = size - 1; i >= 0; i--) {
			if (wordPos.get(i) == 0) {
				startPos = i;
				continue;
			}

			if ((wordPos.get(i) == 1) && (wordChoices.get(i) != -1)) {
				break;
			}
		}

		if (startPos == -1) {
			return;
		}

		int previousScoreSoFar = s.scoreSoFar;

		for (int i = startPos; i < wordChoices.size(); i++) {
			if (wordPos.get(i) == 1) {
				continue;
			}
			wordPos.set(i, 1);
			// TODO: length
			s.currentMatch.get(index).matchStringStart = wordChoices.get(i);
			s.scoreSoFar = previousScoreSoFar + getScore(index, s);
			if (((s.bestScoreSoFar == -1) || (s.scoreSoFar < s.bestScoreSoFar))) {
				getBestMatch(index + 1, s);
			}
			if ((maxComputations != -1) && (s.bestScoreSoFar != -1)
					&& (numComputations > maxComputations)) {
				return;
			}

			wordPos.set(i, 0);

			if (s.currentMatch.get(index).matchStringStart == -1) {
				break;
			}
		}
	}

	private int getScore(int index, Stage s) {
		if (s.currentMatch.get(index).matchStringStart == -1) {
			return 0;
		}

		// Pack index, match[index] into long
		long fixedScoreKey = index;
		fixedScoreKey <<= 32;
		// TODO: account for length
		fixedScoreKey += s.currentMatch.get(index).matchStringStart;

		Integer score = s.scoreCache.get(fixedScoreKey);
		if (score == null) {
			score = 0;
			for (int i = 0; i < index; i++) {
				if (s.choiceIdx.contains(i)) {
					continue;
				}
				if (s.currentMatch.get(i).matchStringStart > s.currentMatch
						.get(index).matchStringStart) {
					score++;
				}
			}
			for (int i = index + 1; i < s.currentMatch.size(); i++) {
				if (s.choiceIdx.contains(i)) {
					continue;
				}
				if (s.currentMatch.get(i).matchStringStart < s.currentMatch
						.get(index).matchStringStart) {
					score++;
				}
			}
			s.scoreCache.put(fixedScoreKey, score);
		}

		for (int i = 0; i < index; i++) {
			if (!s.choiceIdx.contains(i)) {
				continue;
			}
			if (s.currentMatch.get(i).matchStringStart > s.currentMatch
					.get(index).matchStringStart) {
				score++;
			}
		}
		return score;
	}
}
