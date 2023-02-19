/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

package edu.cmu.meteor.aligner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class SynonymDictionary {

	// Synsets
	private ArrayList<HashSet<Integer>> synSets;
	private Hashtable<String, Integer> setIndex;

	// Exceptions
	private ArrayList<String> wordBases;
	private Hashtable<String, Integer> wordIndex;

	public SynonymDictionary(URL synFileURL, URL excFileURL) throws IOException {
		synSets = new ArrayList<HashSet<Integer>>();
		setIndex = new Hashtable<String, Integer>();
		wordBases = new ArrayList<String>();
		wordIndex = new Hashtable<String, Integer>();

		// Empty sets to return if word not found
		synSets.add(new HashSet<Integer>());
		wordBases.add("");

		// Synset file
		int lastIndex = 1;
		BufferedReader inSyn = new BufferedReader(new InputStreamReader(
				synFileURL.openStream()));
		String word;
		String line;
		while ((word = inSyn.readLine()) != null) {
			synSets.add(new HashSet<Integer>());
			line = inSyn.readLine();
			StringTokenizer sets = new StringTokenizer(line);
			while (sets.hasMoreTokens())
				synSets.get(lastIndex).add(Integer.parseInt(sets.nextToken()));
			setIndex.put(word, lastIndex);
			lastIndex++;
		}
		inSyn.close();

		// Exception file
		lastIndex = 1;
		BufferedReader inExc = new BufferedReader(new InputStreamReader(
				excFileURL.openStream()));
		while ((word = inExc.readLine()) != null) {
			wordBases.add(word);
			line = inExc.readLine();
			StringTokenizer words = new StringTokenizer(line);
			while (words.hasMoreTokens())
				wordIndex.put(words.nextToken(), lastIndex);
			lastIndex++;
		}
		inExc.close();
	}

	public HashSet<Integer> getSynSet(String word) {
		return synSets.get(lookupIndex(word));
	}

	public HashSet<Integer> getStemSynSet(String word) {
		return synSets.get(lookupStemIndex(word));
	}

	private int lookupIndex(String word) {
		if (setIndex.containsKey(word))
			return setIndex.get(word);
		return 0;
	}

	private int lookupStemIndex(String word) {
		if (wordIndex.containsKey(word))
			return lookupIndex(wordBases.get(wordIndex.get(word)));
		return lookupIndex(morph(word));
	}

	/*
	 * This information and the morphology algorithm are taken from the WordNet
	 * 3 release. See the WordNet license replicated at the end of this file.
	 */

	private static final int OFFSET = 0;
	private static final int CNT = 20;

	private static final String[] sufx = {
	/* Noun suffixes */
	"s", "ses", "xes", "zes", "ches", "shes", "men", "ies",
	/* Verb suffixes */
	"s", "ies", "es", "es", "ed", "ed", "ing", "ing",
	/* Adjective suffixes */
	"er", "est", "er", "est" };

	private static final String[] addr = {
	/* Noun endings */
	"", "s", "x", "z", "ch", "sh", "man", "y",
	/* Verb endings */
	"", "y", "e", "", "e", "", "e", "",
	/* Adjective endings */
	"", "", "e", "e" };

	private String morph(String word) {
		String tmp = "";
		String end = "";
		String retval = "";

		if (word.endsWith("ful")) {
			tmp = word.substring(0, word.lastIndexOf('f'));
			end = "ful";
		}

		if (word.endsWith("ss") || word.length() <= 2)
			return word;

		tmp = word;

		for (int i = 0; i < CNT; i++) {
			int ender = i + OFFSET;
			if (tmp.endsWith(sufx[ender]))
				retval = word
						.substring(0, word.length() - sufx[ender].length())
						+ addr[ender];
			else
				retval = tmp;
			if (retval != tmp && setIndex.containsKey(retval)) {
				retval += end;
				return retval;
			}
		}

		return "";
	}
}

/*
 * WordNet Release 3.0
 * 
 * This software and database is being provided to you, the LICENSEE, by
 * Princeton University under the following license. By obtaining, using and/or
 * copying this software and database, you agree that you have read, understood,
 * and will comply with these terms and conditions.:
 * 
 * Permission to use, copy, modify and distribute this software and database and
 * its documentation for any purpose and without fee or royalty is hereby
 * granted, provided that you agree to comply with the following copyright
 * notice and statements, including the disclaimer, and that the same appear on
 * ALL copies of the software, database and documentation, including
 * modifications that you make for internal use or for distribution.
 * 
 * WordNet 3.0 Copyright 2006 by Princeton University. All rights reserved.
 * 
 * THIS SOFTWARE AND DATABASE IS PROVIDED "AS IS" AND PRINCETON UNIVERSITY MAKES
 * NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED. BY WAY OF EXAMPLE, BUT
 * NOT LIMITATION, PRINCETON UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES
 * OF MERCHANT- ABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF
 * THE LICENSED SOFTWARE, DATABASE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD
 * PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 * 
 * The name of Princeton University or Princeton may not be used in advertising
 * or publicity pertaining to distribution of the software and/or database.
 * Title to copyright in this software, database and any associated
 * documentation shall at all times remain with Princeton University and
 * LICENSEE agrees to preserve same.
 */