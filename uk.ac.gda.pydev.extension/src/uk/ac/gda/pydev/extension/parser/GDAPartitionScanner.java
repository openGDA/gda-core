/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.pydev.extension.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.Token;
import org.python.pydev.core.docutils.PyPartitionScanner;

/**
 *
 */
public class GDAPartitionScanner extends PyPartitionScanner implements IGdaJythonPartitions {
	/**
	 * 
	 */
	public GDAPartitionScanner(){
		super();
		List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
		
		addAliasRule(rules);
		addLsRule(rules);
		addPosRule(rules);
		addUposRule(rules);
		addIncRule(rules);
		addUincRule(rules);
		addHelpRule(rules);
		addListDefaultsRule(rules);
		addAddDefaultRule(rules);
		addRemoveDefaultRule(rules);
		addLevelRule(rules);
		addPauseRule(rules);
		addResetNamespaceRule(rules);
		addRunRule(rules);
		addScanRule(rules);
		addPscanRule(rules);
		addCscanRule(rules);
		addZacscanRule(rules);
		addTestscanRule(rules);
		addGscanRule(rules);
		addTscanRule(rules);
		addTimescanRule(rules);
		addCvscanRule(rules);
		addRobotscanRule(rules);
		addStagescanRule(rules);
		addTempscanRule(rules);
	}
	
    private void addTempscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("tempscan", new Token(IGdaJythonPartitions.GDA_TEMPSCAN)));	      
	}

	private void addStagescanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("stagescan", new Token(IGdaJythonPartitions.GDA_STAGESCAN)));	      
	}

	private void addRobotscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("robotscan", new Token(IGdaJythonPartitions.GDA_ROBOTSCAN)));	      
	}

	private void addCvscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("cvscan", new Token(IGdaJythonPartitions.GDA_CVSCAN)));	      
	}

	private void addTimescanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("timescan", new Token(IGdaJythonPartitions.GDA_TIMESCAN)));	      
	}

	private void addTscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("tscan", new Token(IGdaJythonPartitions.GDA_TSCAN)));	      
	}

	private void addGscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("gscan", new Token(IGdaJythonPartitions.GDA_GSCAN)));	      
	}

	private void addTestscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("testscan", new Token(IGdaJythonPartitions.GDA_TESTSCAN)));	      
	}

	private void addZacscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("zacscan", new Token(IGdaJythonPartitions.GDA_ZACSCAN)));	      
	}

	private void addCscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("cscan", new Token(IGdaJythonPartitions.GDA_CSCAN)));	      
	}

	private void addPscanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("pscan", new Token(IGdaJythonPartitions.GDA_PSCAN)));
	}

	private void addScanRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("scan", new Token(IGdaJythonPartitions.GDA_SCAN)));	      
	}

	private void addRunRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("run", new Token(IGdaJythonPartitions.GDA_RUN)));	      
	}

	private void addResetNamespaceRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("reset_namespace", new Token(IGdaJythonPartitions.GDA_RESET_NAMESPACE)));	      
    }

	private void addPauseRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("pause", new Token(IGdaJythonPartitions.GDA_PAUSE)));	      
	}

	private void addLevelRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("level", new Token(IGdaJythonPartitions.GDA_LEVEL)));	
	}

	private void addRemoveDefaultRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("remove_default", new Token(IGdaJythonPartitions.GDA_REMOVE_DEFAULT)));	
	}

	private void addAddDefaultRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("add_default", new Token(IGdaJythonPartitions.GDA_ADD_DEFAULT)));	
	}

	private void addListDefaultsRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("list_defaults", new Token(IGdaJythonPartitions.GDA_LIST_DEFAULTS)));	
	}

	private void addHelpRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("help", new Token(IGdaJythonPartitions.GDA_HELP)));	
	}

	private void addUincRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("uinc", new Token(IGdaJythonPartitions.GDA_UINC)));	
	}

	private void addIncRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("inc", new Token(IGdaJythonPartitions.GDA_INC)));	
	}

	private void addUposRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("upos", new Token(IGdaJythonPartitions.GDA_UPOS)));	
	}

	private void addLsRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("ls", new Token(IGdaJythonPartitions.GDA_LS)));	
	}

	private void addAliasRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("alias", new Token(IGdaJythonPartitions.GDA_ALIAS)));
    }
	
    private void addPosRule(List<IPredicateRule> rules) {
        rules.add(new EndOfLineRule("pos", new Token(IGdaJythonPartitions.GDA_POS)));
    }
    static public String[] getTypes(){
    	return IGdaJythonPartitions.types;
    }
}
