/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.jython.completion.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.Jython;
import gda.jython.completion.AutoCompleteOption;
import gda.jython.completion.AutoCompletion;
import gda.jython.completion.CompletionType;
import gda.jython.completion.TextCompleter;

/**
 * {@link TextCompleter} than takes a {@link Jython} instance and uses its namespace to provide
 * context sensitive text completion
 */
public class JythonCompleter implements TextCompleter {
	public static final String COMPLETION_STYLE_PROPERTY = "gda.jython.completion.style";
	private static final Logger logger = LoggerFactory.getLogger(JythonCompleter.class);
	private boolean completeOnEmpty;
	private PyObject jyComplete;

	private enum MODE {
		/** Must match exactly (ignoring case) */
		BASIC,
		/** Allow use of upper case letters to match beginnings of words (eg gP&nbsp;→&nbsp;getPosition) */
		CAMEL,
		/** Anything goes - if letter are present and in the right order it works */
		FUZZY,
		/** {@link #BASIC} if all lower case, {@link #CAMEL} otherwise */
		AUTO,
		;
		public static MODE get(String mode) {
			return valueOf(mode.toUpperCase());
		}
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	/**
	 * Set the jython interpreter that should be used to provide the completions.<p>
	 * There should be a {@code gda_completer} module on the PYTHON_PATH that has a {@code Completer}
	 * class with a {@code complete} method taking a string and returning a list of tuples
	 * (completion, args, doc, type).<p>
	 * Currently, only completion and type are used.
	 * @param jython {@link Jython} instance to get the complete method from
	 */
	public JythonCompleter(Jython jython) {
		String style = LocalProperties.get(COMPLETION_STYLE_PROPERTY);
		MODE mode;
		try {
			mode = style == null ? MODE.AUTO : MODE.get(style);
		} catch (IllegalArgumentException iae) {
			logger.debug("{} not a recognised mode", style, iae);
			mode = MODE.AUTO;
		}
		logger.info("Using {} jython completion style", mode);

		// Create the completer object in namespace to provide context aware command line completion
		jython.exec("from gda_completer import Completer");
		jyComplete = jython.eval("Completer(globals(), '" + mode + "').complete");
	}

	/**
	 * Get possible completions for given line and cursor position.<br>
	 * The returned {@link AutoCompletion} contains
	 * @param line the full line so far
	 * @param posn the position of the cursor in the line
	 * @return {@link AutoCompletion} containing list of completions and unchanged parts of original line
	 */
	@Override
	public AutoCompletion getCompletionsFor(String line, int posn) {
		List<AutoCompleteOption> completions = new ArrayList<>();
		String before = line.substring(0, posn);
		int position = 0;
		if (!completeOnEmpty && (before.isEmpty() || before.matches("\\s+"))) {
			if (before.matches("\\t*")) {
				completions.add(new AutoCompleteOption("\t" + before, CompletionType.NONE));
			} else {
				//don't add a tab to spaces or mixed tabs/spaces
				//do nothing / leave list empty;
			}
		} else {
			//split on anything that can't be in a python variable/attribute name
			String[] splits = before.split("[^a-zA-Z0-9._]+",-1);
			String finalSplit = splits[splits.length - 1];
			String[] parts = finalSplit.split("\\.", -1);
			String lastPart = parts[parts.length - 1];
			List<AutoCompleteOption> comps = getWordCompletion(finalSplit);
			completions.addAll(comps);
			position = before.lastIndexOf(finalSplit) + finalSplit.length() - lastPart.length();
		}
		return new AutoCompletion(before.substring(0, position), line.substring(posn), completions, position);
	}

	private List<AutoCompleteOption> getWordCompletion(String partial) {
		if (partial == null || (!completeOnEmpty && partial.isEmpty())) {
			return new ArrayList<>();
		}

		List<PyTuple> results = getJythonCompletions(partial);
		return results
				.stream()
				.map(o -> new AutoCompleteOption((String)o.get(0), (int)o.get(3)))
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private List<PyTuple> getJythonCompletions(String partial) {
		return (PyList)jyComplete.__call__(new PyString(partial));
	}

	/**
	 * Set whether this completer will provide completions (all of {@code globals()}) when given an empty string.
	 * If not, a tab will be returned as the only option
	 * @param completeOnEmpty true if an empty string should complete to the full list of globals()<br>
	 * false if an empty string should complete to an indent
	 */
	public void setCompleteOnEmpty(boolean completeOnEmpty) {
		this.completeOnEmpty = completeOnEmpty;
	}
}
