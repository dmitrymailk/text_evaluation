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
import java.util.Iterator;

public class SynonymMatcher {
	public static Stage match(Alignment a, SynonymDictionary synonyms) {
		Stage s = new Stage();

		// We don't use vocabularies since we need the actual words to pass to
		// the synonym dictionary

		Hashtable<Integer, HashSet<Integer>> string1Syn = new Hashtable<Integer, HashSet<Integer>>();
		Hashtable<Integer, HashSet<Integer>> string2Syn = new Hashtable<Integer, HashSet<Integer>>();
		Hashtable<Integer, HashSet<Integer>> string1StemSyn = new Hashtable<Integer, HashSet<Integer>>();
		Hashtable<Integer, HashSet<Integer>> string2StemSyn = new Hashtable<Integer, HashSet<Integer>>();

		// Line 1
		for (int i = 0; i < a.words1.size(); i++) {
			// Add if not aligned in previous stage
			if (!a.alignedWords1.contains(i)) {
				string1Syn.put(i, synonyms.getSynSet(a.words1.get(i)));
				string1StemSyn.put(i, synonyms.getStemSynSet(a.words1.get(i)));
			}
		}

		// Line 2
		for (int i = 0; i < a.words2.size(); i++) {
			if (!a.alignedWords2.contains(i)) {
				string2Syn.put(i, synonyms.getSynSet(a.words2.get(i)));
				string2StemSyn.put(i, synonyms.getStemSynSet(a.words2.get(i)));
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

				Iterator<Integer> sets1 = string1Syn.get(j).iterator();
				Iterator<Integer> sets1stem = string1StemSyn.get(j).iterator();
				HashSet<Integer> sets2 = string2Syn.get(i);
				HashSet<Integer> sets2stem = string2StemSyn.get(i);

				boolean syn = false;
				while (sets1.hasNext()) {
					int key = sets1.next();
					if (sets2.contains(key) || sets2stem.contains(key)) {
						syn = true;
						break;
					}
				}
				if (!syn)
					while (sets1stem.hasNext()) {
						int key = sets1stem.next();
						if (sets2.contains(key) || sets2stem.contains(key)) {
							syn = true;
							break;
						}
					}
				if (syn) {

					Match m = new Match();
					s.matches.add(m);
					m.prob = 1;
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