/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

package edu.cmu.meteor.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.frenchStemmer;
import org.tartarus.snowball.ext.germanStemmer;
import org.tartarus.snowball.ext.spanishStemmer;

public class Constants {

	/* Version */

	public static final String VERSION = "1.0";

	/*
	 * Aligner Constants
	 */

	public static final int MODULE_EXACT = 0;
	public static final int MODULE_STEM = 1;
	public static final int MODULE_SYNONYM = 2;
	public static final int MODULE_PARAPHRASE = 3;

	public static final int MAX_MODULES = 4;

	public static final double DEFAULT_WEIGHT_EXACT = 1.0;
	public static final double DEFAULT_WEIGHT_STEM = 1.0;
	public static final double DEFAULT_WEIGHT_SYNONYM = 1.0;
	public static final double DEFAULT_WEIGHT_PARAPHRASE = 1.0;

	public static final int DEFAULT_MAXCOMP = 10000;

	public static final URL DEFAULT_SYN_DIR_URL = ClassLoader
			.getSystemResource("synonym");

	public static final URL DEFAULT_PARA_DIR_URL = ClassLoader
			.getSystemResource("paraphrase");

	/*
	 * Scorer Constants
	 */

	/* Languages */
	public static final int LANG_EN = 0;
	public static final int LANG_CS = 1;
	public static final int LANG_FR = 2;
	public static final int LANG_ES = 3;
	public static final int LANG_DE = 4;
	public static final int LANG_AR = 5;
	public static final int LANG_OTHER = 99;

	/* Adequacy and fluency task */
	public static final int TASK_AF = 0;
	public static final double PARAM_AF[][] = {
	//
			{ 0.8, 2.5, 0.4 }, // English
			{ 0.8, 0.83, 0.28 }, // Czech
			{ 0.76, 0.5, 1.0 }, // French
			{ 0.95, 0.5, 0.75 }, // Spanish
			{ 0.95, 1.0, 0.98 }, // German
			{ .80, 3.0, 0.1 } // Arabic
	// Arabic parameters tuned by Greg Sanders
	// (gregory dot sanders at nist dot gov)
	};

	/* Ranking task (Arabic not supported) */
	public static final int TASK_RANK = 1;
	public static final double PARAM_RANK[][] = {
	//
			{ 0.95, 0.5, 0.5 }, // English
			{ 0.95, 0.5, 0.45 }, // Czech
			{ 0.9, 0.5, 0.55 }, // French
			{ 0.9, 0.5, 0.55 }, // Spanish
			{ 0.9, 3.0, 0.15 }, // German
			{ 0.0, 0.0, 0.0 } // Arabic
	};

	/* HTER task (English only) */
	public static final int TASK_HTER = 2;
	public static final double PARAM_HTER[][] = {
	//
			{ 0.65, 1.8, 0.45 }, // English
			{ 0.0, 0.0, 0.0 }, // Czech
			{ 0.0, 0.0, 0.0 }, // French
			{ 0.0, 0.0, 0.0 }, // Spanish
			{ 0.0, 0.0, 0.0 }, // German
			{ 0.0, 0.0, 0.0 } // Arabic

	};

	// Cannot be used to set task, only used when options are specified manually
	public static final int TASK_CUSTOM = 99;

	/* Normalization */
	public static final int NO_NORMALIZE = 0;
	public static final int NORMALIZE_KEEP_PUNCT = 1;
	public static final int NORMALIZE_NO_PUNCT = 2;

	/*
	 * Methods to look up constants
	 */

	public static String getLocation() {
		File codeDir = new File(Constants.class.getProtectionDomain()
				.getCodeSource().getLocation().getFile());
		// Class is in directory
		if (codeDir.isDirectory())
			return codeDir.toString();
		// Class is in JAR file
		return codeDir.getParent();
	}

	public static String normLanguageName(String language) {
		String lang = language.toLowerCase();
		if (lang.equals("english") || lang.equals("en"))
			return "english";
		if (lang.equals("czech") || lang.equals("cs") || lang.equals("cz"))
			return "czech";
		if (lang.equals("french") || lang.equals("fr"))
			return "french";
		if (lang.equals("german") || lang.equals("de"))
			return "german";
		if (lang.equals("spanish") || lang.equals("es"))
			return "spanish";
		if (lang.equals("arabic") || lang.equals("ar"))
			return "arabic";

		// Not found
		return "other";
	}

	public static int getLanguageID(String language) {
		if (language.equals("english"))
			return LANG_EN;
		if (language.equals("czech"))
			return LANG_CS;
		if (language.equals("french"))
			return LANG_FR;
		if (language.equals("spanish"))
			return LANG_ES;
		if (language.equals("german"))
			return LANG_DE;
		if (language.equals("arabic"))
			return LANG_AR;

		// Not found
		return LANG_OTHER;
	}

	public static String getLanguageName(int langID) {
		if (langID == LANG_EN)
			return "english";
		if (langID == LANG_CS)
			return "czech";
		if (langID == LANG_FR)
			return "french";
		if (langID == LANG_ES)
			return "spanish";
		if (langID == LANG_DE)
			return "german";
		if (langID == LANG_AR)
			return "arabic";

		// Not found
		return "other";
	}

	public static int getModuleID(String modName) {
		String mod = modName.toLowerCase();
		if (mod.equals("exact"))
			return MODULE_EXACT;
		if (mod.equals("stem"))
			return MODULE_STEM;
		if (mod.equals("synonym"))
			return MODULE_SYNONYM;
		if (mod.equals("paraphrase"))
			return MODULE_PARAPHRASE;

		// Not found
		System.err.println("Module \"" + modName
				+ "\" not found, using \"exact\"");
		return MODULE_EXACT;
	}

	public static String getModuleName(int module) {
		if (module == MODULE_EXACT)
			return "exact";
		if (module == MODULE_STEM)
			return "stem";
		if (module == MODULE_SYNONYM)
			return "synonym";
		if (module == MODULE_PARAPHRASE)
			return "paraphrase";

		// Not found
		return "other";
	}

	public static ArrayList<Double> getDefaultModuleWeights(String language,
			String task) {
		return getDefaultModuleWeights(getLanguageID(language), getTaskID(task));
	}

	public static ArrayList<Double> getDefaultModuleWeights(int langID,
			int taskID) {
		ArrayList<Double> weights = new ArrayList<Double>();

		// All tasks currently have 1.0 for all weights. If this
		// changes, the logic should be updated here only.

		if (langID == LANG_EN) {
			weights.add(1.0);
			weights.add(1.0);
			weights.add(1.0);
			weights.add(1.0);
		}
		if (langID == LANG_FR) {
			weights.add(1.0);
			weights.add(1.0);
		}
		if (langID == LANG_ES) {
			weights.add(1.0);
			weights.add(1.0);
		}
		if (langID == LANG_DE) {
			weights.add(1.0);
			weights.add(1.0);
		}
		if (langID == LANG_CS) {
			weights.add(1.0);
		}
		if (langID == LANG_AR) {
			weights.add(1.0);
		}

		return weights;
	}

	public static int getTaskID(String taskName) {
		String task = taskName.toLowerCase();
		if (task.equals("default"))
			return TASK_AF;
		if (task.equals("af"))
			return TASK_AF;
		if (task.equals("rank"))
			return TASK_RANK;
		if (task.equals("hter"))
			return TASK_HTER;

		// Other
		return TASK_CUSTOM;
	}

	public static String getTaskDescription(String task) {
		return getTaskDescription(getTaskID(task));
	}

	public static String getTaskDescription(int task) {
		if (task == TASK_AF)
			return "Adequacy & Fluency";
		if (task == TASK_RANK)
			return "Ranking";
		if (task == TASK_HTER)
			return "HTER";

		// Other
		return "Custom";
	}

	public static ArrayList<Double> getParameters(String language,
			String taskName) {
		int langID = getLanguageID(language);
		int task = getTaskID(taskName);

		double[] TASK_CONST;
		if (task == TASK_RANK)
			if (langID == LANG_AR) {
				System.err.println("Error: Rank task not available for "
						+ "Arabic, using Adequacy/Fluency parameters");
				TASK_CONST = PARAM_AF[langID];
			} else {
				TASK_CONST = PARAM_RANK[langID];
			}
		else if (task == TASK_HTER) {
			if (langID != LANG_EN) {
				System.err.println("Error: HTER task only available for "
						+ "English, using Adequacy/Fluency parameters");
				TASK_CONST = PARAM_AF[langID];
			} else
				TASK_CONST = PARAM_HTER[langID];
		} else
			// Assume TASK_AF
			TASK_CONST = PARAM_AF[langID];

		ArrayList<Double> parameters = new ArrayList<Double>();
		for (double param : TASK_CONST)
			parameters.add(param);

		return parameters;
	}

	public static SnowballStemmer newStemmer(String language) {
		if (language.equals("english"))
			return new englishStemmer();
		if (language.equals("french"))
			return new frenchStemmer();
		if (language.equals("german"))
			return new germanStemmer();
		if (language.equals("spanish"))
			return new spanishStemmer();

		// Not found
		return new englishStemmer();
	}

	public static ArrayList<Integer> getDefaultModules(String language,
			String taskName) {
		ArrayList<Integer> modules = new ArrayList<Integer>();
		if (language.equals("english")) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
			modules.add(MODULE_SYNONYM);
			if (getTaskID(taskName) == TASK_HTER)
				modules.add(MODULE_PARAPHRASE);
		} else if (language.equals("french")) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (language.equals("german")) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (language.equals("spanish")) {
			modules.add(MODULE_EXACT);
			modules.add(MODULE_STEM);
		} else if (language.equals("czech")) {
			modules.add(MODULE_EXACT);
		} else if (language.equals("arabic")) {
			modules.add(MODULE_EXACT);
		}

		return modules;
	}
}
