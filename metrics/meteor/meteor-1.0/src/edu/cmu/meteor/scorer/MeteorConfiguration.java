/*
 * Carnegie Mellon University
 * Copyright (c) 2004, 2009
 * All Rights Reserved.
 *
 * Any use of this software must follow the terms
 * outlined in the included LICENSE file.
 */

package edu.cmu.meteor.scorer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import edu.cmu.meteor.util.Constants;

/**
 * Meteor configuration class which can be instantiated, modified with set()
 * methods, and passed to a new MeteorScorer
 * 
 */
public class MeteorConfiguration {

	/* Configuration Instance */

	private String language;
	private int langID;
	private String task;
	private int normalization;
	private ArrayList<Double> parameters;
	private ArrayList<Integer> modules;
	private ArrayList<Double> moduleWeights;
	private int maxComp;
	private URL synDirURL;
	private URL paraDirURL;

	/**
	 * Create configuration with default parameters
	 */
	public MeteorConfiguration() {
		setLanguage("english");
		setTask("af");
		setMaxComp(Constants.DEFAULT_MAXCOMP);
		setSynDirURL(Constants.DEFAULT_SYN_DIR_URL);
		setParaDirURL(Constants.DEFAULT_PARA_DIR_URL);
		setNormalization(Constants.NO_NORMALIZE);
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = Constants.normLanguageName(language);
		this.langID = Constants.getLanguageID(this.language);
	}

	// No setter for langID since it must correspond to language

	public int getLangID() {
		return langID;
	}

	public String getTask() {
		return task;
	}

	public String getTaskDesc() {
		return Constants.getTaskDescription(task);
	}

	/**
	 * Sets task by name plus default parameters, modules, and module weights
	 * for task.
	 * 
	 * @param task
	 */
	public void setTask(String task) {
		setParameters(Constants.getParameters(language, task));
		setModules(Constants.getDefaultModules(language, task));
		ArrayList<Double> weightList = Constants.getDefaultModuleWeights(
				language, task);
		ArrayList<Double> sizedWeightList = new ArrayList<Double>();
		for (int i = 0; i < modules.size(); i++)
			sizedWeightList.add(weightList.get(i));
		setModuleWeights(sizedWeightList);
		setTaskName(task);
	}

	// Only used by other methods
	private void setTaskName(String name) {
		this.task = name;
	}

	public int getNormalization() {
		return normalization;
	}

	public void setNormalization(int normalization) {
		this.normalization = normalization;
	}

	public ArrayList<Double> getParameters() {
		return new ArrayList<Double>(parameters);
	}

	public String getParametersString() {
		StringBuilder sb = new StringBuilder();
		for (Double param : parameters)
			sb.append(param + " ");
		return sb.toString().trim();
	}

	public void setParameters(ArrayList<Double> parameters) {
		setTaskName("custom");
		this.parameters = new ArrayList<Double>(parameters);
	}

	public void setParameters(String language, String taskName) {
		setTaskName(taskName);
		parameters = Constants.getParameters(language, taskName);
	}

	public ArrayList<Integer> getModules() {
		return new ArrayList<Integer>(modules);
	}

	public String getModulesString() {
		StringBuilder sb = new StringBuilder();
		for (Integer module : modules)
			sb.append(Constants.getModuleName(module) + " ");
		return sb.toString().trim();
	}

	public void setModules(ArrayList<Integer> modules) {
		setTaskName("custom");
		this.modules = new ArrayList<Integer>();
		for (Integer modID : modules)
			this.modules.add(modID);
	}

	public void setModulesByName(ArrayList<String> modules) {
		setTaskName("custom");
		this.modules = new ArrayList<Integer>();
		for (String modName : modules)
			this.modules.add(Constants.getModuleID(modName));
	}

	public ArrayList<Double> getModuleWeights() {
		return new ArrayList<Double>(moduleWeights);
	}

	public String getModuleWeightsString() {
		StringBuilder sb = new StringBuilder();
		for (Double weight : moduleWeights)
			sb.append(weight + " ");
		return sb.toString().trim();
	}

	public void setModuleWeights(ArrayList<Double> moduleScores) {
		setTaskName("custom");
		this.moduleWeights = new ArrayList<Double>(moduleScores);
	}

	public URL getSynDirURL() {
		return synDirURL;
	}

	public void setSynDirURL(URL synDirURL) {
		try {
			// This should not ever throw a malformed url exception
			this.synDirURL = new URL(synDirURL.toString());
		} catch (MalformedURLException ex) {
			System.err.println("Error: Synonym directory URL NOT set");
			ex.printStackTrace();
		}
	}

	public URL getParaDirURL() {
		return paraDirURL;
	}

	public void setParaDirURL(URL paraDirURL) {
		try {
			// This should not ever throw a malformed url exception
			this.paraDirURL = new URL(paraDirURL.toString());
		} catch (MalformedURLException ex) {
			System.err.println("Error: Synonym directory URL NOT set");
			ex.printStackTrace();
		}
	}

	public int getMaxComp() {
		return maxComp;
	}

	public void setMaxComp(int maxComp) {
		this.maxComp = maxComp;
	}
}
