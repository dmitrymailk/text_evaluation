/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

package edu.cmu.meteor.aligner;

import java.util.HashSet;
import java.util.Hashtable;

public class ParaphraseMatcher {
	public static Stage match(Alignment a, ParaphraseDictionary dictionary) {
		Stage s = new Stage();

		Hashtable<Integer, Hashtable<String, Double>> string1Sets = new Hashtable<Integer, Hashtable<String, Double>>();
		Hashtable<Integer, Hashtable<String, Double>> string2Sets = new Hashtable<Integer, Hashtable<String, Double>>();

		// Line 1
		for (int i = 0; i < a.words1.size(); i++) {
			// Add if not aligned in previous stage
			if (!a.alignedWords1.contains(i)) {
				string1Sets.put(i, dictionary.getSet(a.words1.get(i)));
			}
		}

		// Line 2
		for (int i = 0; i < a.words2.size(); i++) {
			if (!a.alignedWords2.contains(i)) {
				string2Sets.put(i, dictionary.getSet(a.words2.get(i)));
			}
		}

		// Build data structures
		int index = 0;
		HashSet<Integer> wordsUsed = new HashSet<Integer>();

		for (int i = 0; i < a.words2.size(); i++) {
			// skip if already aligned
			if (a.alignedWords2.contains(i))
				continue;

			for (int j = 0; j < a.words1.size(); j++) {
				if (a.alignedWords1.contains(j))
					continue;

				if (wordsUsed.contains(j))
					continue;

				if (string2Sets.get(i).containsKey(a.words1.get(j))
						&& string2Sets.get(i).get(a.words1.get(j)) > 0.01
						|| string1Sets.get(j).containsKey(a.words2.get(i))
						&& string1Sets.get(j).get(a.words2.get(i)) > 0.01) {

					Match m = new Match();
					s.matches.add(m);
					m.prob = 1;
//					if (string2Sets.get(i).containsKey(a.words1.get(j)))
//						m.prob = string2Sets.get(i).get(a.words1.get(j));
//					else if (string1Sets.get(j).containsKey(a.words2.get(i)))
//						m.prob = string1Sets.get(j).get(a.words2.get(i));

					m.start = i;
					m.length = 1;
					m.matchStringStart = j;
					m.matchStringLength = 1;

					index++;
					wordsUsed.add(j);
					break;
				}
			}
		}
		return s;
	}
}