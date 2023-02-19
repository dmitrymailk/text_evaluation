package edu.cmu.meteor.aligner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;

public class ParaphraseDictionary {

	private Hashtable<String, Hashtable<String, Double>> dictionary;
	private static final Hashtable<String, Double> EMPTY_SET = new Hashtable<String, Double>();

	public ParaphraseDictionary(URL paraDirURL) throws IOException {
		dictionary = new Hashtable<String, Hashtable<String, Double>>();
		BufferedReader in = new BufferedReader(new InputStreamReader(paraDirURL
				.openStream()));
		String ref;
		try {
			while ((ref = in.readLine()) != null) {
				String para = in.readLine();
				double prob = Double.parseDouble(in.readLine());
				if (!dictionary.containsKey(ref))
					dictionary.put(ref, new Hashtable<String, Double>());
				dictionary.get(ref).put(para, prob);
			}
		} catch (IOException ex) {
			System.err.println("Error: Incomplete entry in paraphrase table.");
			System.err.println("Paraphrase dictionary may not be complete.");
		}
		in.close();
	}

	public Hashtable<String, Double> getSet(String key) {
		Hashtable<String, Double> set = dictionary.get(key);
		return set == null ? EMPTY_SET : set;
	}
}
