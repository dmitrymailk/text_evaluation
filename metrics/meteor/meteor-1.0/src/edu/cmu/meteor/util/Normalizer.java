/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

package edu.cmu.meteor.util;

import java.util.regex.Pattern;

public class Normalizer {
	private static Pattern r_skip = Pattern.compile("<skipped>",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_dashnl = Pattern.compile("-\\n");
	private static Pattern r_nl = Pattern.compile("\\n");
	private static Pattern r_quote = Pattern.compile("&quot;",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_amp = Pattern.compile("&amp;",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_lt = Pattern.compile("&lt;",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_gt = Pattern.compile("&gt;",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_u1 = Pattern.compile("[\u2018\u2019\u201A\u201B]");
	private static Pattern r_u2 = Pattern.compile("[\u201C\u201D\u201E\u201F]");
	private static Pattern r_punct1 = Pattern
			.compile("([\\{-\\~\\[-\\` -\\&\\(-\\+\\:-\\@\\/])");
	private static Pattern r_punct2 = Pattern.compile("([^0-9])([\\.,])");
	private static Pattern r_punct3 = Pattern.compile("([\\.,])([^0-9])");
	private static Pattern r_punct4 = Pattern.compile("([0-9])(-)");
	private static Pattern r_punct5 = Pattern.compile("[_#]");
	private static Pattern r_nonalpha = Pattern
			.compile("[^a-z0-9\u00C0-\u00FF ]");
	private static Pattern r_norm1 = Pattern.compile("\\s+");
	private static Pattern r_norm2 = Pattern.compile("^\\s+");
	private static Pattern r_norm3 = Pattern.compile("\\s+$");

	private static String space = " ";
	private static String quote = "\"";
	private static String amp = "&";
	private static String lt = "<";
	private static String gt = ">";
	private static String apos = "'";
	private static String punct1 = " $1 ";
	private static String punct2 = "$1 $2 ";
	private static String punct3 = " $1 $2";
	private static String punct4 = "$1 $2 ";

	public static String normalizeLine(String line, int langID,
			boolean keepPunctuation) {

		String workingLine = line;

		// Clean up SGML tags
		workingLine = r_skip.matcher(workingLine).replaceAll("");
		workingLine = r_dashnl.matcher(workingLine).replaceAll("");
		workingLine = r_nl.matcher(workingLine).replaceAll(space);
		workingLine = r_quote.matcher(workingLine).replaceAll(quote);
		workingLine = r_amp.matcher(workingLine).replaceAll(amp);
		workingLine = r_lt.matcher(workingLine).replaceAll(lt);
		workingLine = r_gt.matcher(workingLine).replaceAll(gt);

		// Punctuation to normal apostrophe
		workingLine = r_u1.matcher(workingLine).replaceAll(apos);
		workingLine = r_u2.matcher(workingLine).replaceAll(quote);

		// Additional normalization for European languages
		if (langID == Constants.LANG_EN || langID == Constants.LANG_CS
				|| langID == Constants.LANG_DE || langID == Constants.LANG_ES
				|| langID == Constants.LANG_FR) {

			// Lowercase
			workingLine = " " + line.toLowerCase() + " ";

			if (keepPunctuation) {
				// Tokenize punctuation
				workingLine = r_punct1.matcher(workingLine).replaceAll(punct1);
				workingLine = r_punct2.matcher(workingLine).replaceAll(punct2);
				workingLine = r_punct3.matcher(workingLine).replaceAll(punct3);
				workingLine = r_punct4.matcher(workingLine).replaceAll(punct4);
				workingLine = r_punct5.matcher(workingLine).replaceAll(space);
			} else {
				workingLine = r_nonalpha.matcher(workingLine).replaceAll(space);
			}
		}

		// Collapse spaces
		workingLine = r_norm1.matcher(workingLine).replaceAll(space);
		workingLine = r_norm2.matcher(workingLine).replaceAll("");
		workingLine = r_norm3.matcher(workingLine).replaceAll("");

		return workingLine;
	}
}