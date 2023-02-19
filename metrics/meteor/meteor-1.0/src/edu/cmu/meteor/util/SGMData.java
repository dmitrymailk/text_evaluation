package edu.cmu.meteor.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SGMData {
	// Patterns for SGML processing
	private static Pattern r_doc1 = Pattern.compile("<DOC",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_doc2 = Pattern.compile("docid=\"([^\"]*)\"",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_doc3 = Pattern.compile("sysid=\"([^\"]*)",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_doc4 = Pattern.compile("<\\/DOC>",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_seg1 = Pattern.compile(
			"<\\s*seg\\s*id\\s*=\\s*\"?\\s*(.+?)\\s*\"?\\s*>",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_seg2 = Pattern.compile("<\\s*seg\\s*>",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_seg3 = Pattern.compile("<\\s*tstset",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_seg4 = Pattern.compile(
			"setid\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
	private static Pattern r_seg5 = Pattern.compile("<\\s*\\/seg\\s*>",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_sp = Pattern.compile("\\s",
			Pattern.CASE_INSENSITIVE);
	private static Pattern r_txt = Pattern.compile(
			"<\\s*seg.*?>(.*)<\\s*\\/seg\\s*>", Pattern.CASE_INSENSITIVE);
	private static String space = " ";

	/* Instance Variables */

	public String testSetID;
	public String firstSysID;

	// System IDs
	public HashSet<String> systems;
	public HashSet<String> references;

	// Segment level
	public HashSet<String> segKeys;
	public Hashtable<String, String> segText;

	// Document level
	public HashSet<String> docKeys;

	public SGMData() {
		systems = new HashSet<String>();
		references = new HashSet<String>();
		segKeys = new HashSet<String>();
		segText = new Hashtable<String, String>();
		docKeys = new HashSet<String>();
	}

	public static void populate(SGMData data, String fileName,
			boolean isReference) throws IOException {

		String currentDocID = "";
		String currentSysID = "";
		int lastAutoSegID = 0;

		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String line;
		Matcher match;

		while ((line = in.readLine()) != null) {
			// Document information
			// Example: <DOC docid="ABC123.456" sysid="test">
			if (r_doc1.matcher(line).find()) {
				if ((match = r_doc2.matcher(line)).find()) {
					currentDocID = match.group(1);
					data.docKeys.add(currentDocID);
				} else {
					throw new IOException(
							"Couldn't read document id from line: " + line);
				}
				if ((match = r_doc3.matcher(line)).find()) {
					currentSysID = match.group(1);
					// Save system ID
					if (isReference) {
						data.references.add(currentSysID);
					} else {
						if (data.systems.size() == 0)
							data.firstSysID = currentSysID;
						data.systems.add(currentSysID);
					}
				} else {
					throw new IOException("Couldn't read system id from line: "
							+ line);
				}
				lastAutoSegID = 0;
				continue;
			}

			// End of document
			if (r_doc4.matcher(line).find()) {
				currentDocID = "";
				currentSysID = "";
				lastAutoSegID = 0;
				continue;
			}

			int currentSegID = 0;

			// Example: <seg id=1> Text goes here </seg>
			if ((match = r_seg1.matcher(line)).find()) {
				currentSegID = Integer.parseInt(match.group(1));
			} // Example: <seg> Text goes here </seg>
			else if (r_seg2.matcher(line).find()) {
				currentSegID = ++lastAutoSegID;
			} else if (r_seg3.matcher(line).find()) {
				(match = r_seg4.matcher(line)).find();
				data.testSetID = match.group(1);
				continue;
			} else {
				// Ignore anything else
				continue;
			}

			// Collect the data for the segment
			while (!r_seg5.matcher(line).find()) {
				line += in.readLine();
			}

			String lineToMatch = r_sp.matcher(line).replaceAll(space);
			String str = "";

			// Extract text
			if ((match = r_txt.matcher(lineToMatch)).find())
				str = match.group(1).trim();
			else {
				throw new IOException("Couldn't read segment from line: "
						+ line);
			}

			// Sanity check
			if (currentDocID.equals("")) {
				throw new IOException(
						"The following seems to be outside a DOC block: "
								+ line);
			}

			// Keys are DocID::SegID
			String outerKey = currentDocID + "::" + currentSegID;
			String fullKey = outerKey + "::" + currentSysID;

			// Store
			data.segKeys.add(outerKey);
			data.segText.put(fullKey, str);
		}

		in.close();
	}
}