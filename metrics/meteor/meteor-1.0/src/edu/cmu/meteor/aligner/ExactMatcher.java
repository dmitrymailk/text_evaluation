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
import java.util.Hashtable;

public class ExactMatcher {

	private static ArrayList<Integer> wordsToKeys(ArrayList<String> words) {
		ArrayList<Integer> keys = new ArrayList<Integer>();
		for (String word : words)
			keys.add(word.hashCode());
		return keys;
	}

	public static Stage match(Alignment a) {
		Stage s = new Stage();

		// Call this matcher's vocabulary generator
		ArrayList<Integer> words1 = wordsToKeys(a.words1);
		ArrayList<Integer> words2 = wordsToKeys(a.words2);

		// Map words to their positions
		Hashtable<Integer, ArrayList<Integer>> string1Pos = new Hashtable<Integer, ArrayList<Integer>>();
		Hashtable<Integer, ArrayList<Integer>> string2Pos = new Hashtable<Integer, ArrayList<Integer>>();

		// Line 1
		for (int i = 0; i < words1.size(); i++) {
			// Add if not aligned in previous stage
			if (!a.alignedWords1.contains(i)) {
				if (!string1Pos.containsKey(words1.get(i)))
					string1Pos.put(words1.get(i), new ArrayList<Integer>());
				string1Pos.get(words1.get(i)).add(i);
			}
		}

		// Line 2
		for (int i = 0; i < words2.size(); i++) {
			if (!a.alignedWords2.contains(i)) {
				if (!string2Pos.containsKey(words2.get(i)))
					string2Pos.put(words2.get(i), new ArrayList<Integer>());
				string2Pos.get(words2.get(i)).add(i);
			}
		}

		// Build data structures
		int index = 0;
		for (int i = 0; i < words2.size(); i++) {

			// skip if already aligned
			if (a.alignedWords2.contains(i))
				continue;

			// skip unless word is in string 1
			if (!string1Pos.containsKey(words2.get(i)))
				continue;

			Match m = new Match();
			s.matches.add(m);

			// Exact matches are certain
			m.prob = 1;

			m.start = i;
			m.length = 1; // Exact matches are word level

			// Only one occ in both strings?
			ArrayList<Integer> str1w2i = string1Pos.get(words2.get(i));
			if ((str1w2i.size() == 1)
					&& (string2Pos.get(words2.get(i)).size() == 1)) {
				m.matchStringStart = str1w2i.get(0);
				m.matchStringLength = 1;
			} else {
				// We have multiple pos choices
				s.choiceIdx.add(index);
				m.matchStringStart = words2.get(i);
				m.matchStringLength = 1;
				if (!s.multiChoices.containsKey(m.matchStringStart)) {
					s.multiChoices.put(m.matchStringStart, string1Pos
							.get(m.matchStringStart));
					s.choiceKeys.add(m.matchStringStart);

					// Pad the choices list with "-1" (skip) entries for the
					// difference in number of occs between lines
					int size = string2Pos.get(m.matchStringStart).size()
							- string1Pos.get(m.matchStringStart).size();

					for (int k = 0; k < size; k++) {
						s.multiChoices.get(m.matchStringStart).add(-1);
					}
				}
			}
			index++;
		}
		return s;
	}
}