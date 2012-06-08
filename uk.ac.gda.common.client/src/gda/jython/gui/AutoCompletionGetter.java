/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.gui;

import gda.jython.ICommandRunner;
import gda.util.PyStringToJava;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AutoCompletionGetter {
	final String lastWord;
	final List<AutoCompletionParts> options;

	/**
	 * @param commandRunner 
	 * @param lastWord
	 */
	public AutoCompletionGetter(ICommandRunner commandRunner,  String lastWord) {
		this.lastWord = lastWord;
		String responseString = commandRunner.evaluateCommand(
				"completer.complete(\"" + lastWord + "\")");
		options = (responseString != null) ? constructReponse(responseString) : new ArrayList<AutoCompletionParts>();
	}

	/**
	 * @return List<String>
	 */
	public List<AutoCompletionParts> getOptions() {
		return options;
	}

	private List<AutoCompletionParts> constructReponse(String _response) {
		String response = _response;
		List<AutoCompletionParts> ret = new ArrayList<AutoCompletionParts>();

		// On Windows we get the string terminated in \r\n whilst on Linux we get \n
		if (response.charAt(response.length() - 1) == '\n') {
			response = response.substring(0, response.length() - 1);
		}
		if (response.charAt(response.length() - 1) == '\r') {
			response = response.substring(0, response.length() - 1);
		}

		if (response.length() < 3 || response.charAt(0) != '[' || response.charAt(response.length() - 1) != ']') {
			return ret;
		}

		List<? extends List<String>> responseAsList = PyStringToJava.ListOfTuplesToJava(response);
		for (List<String> list : responseAsList) {
			if (list.size() == 4) {
				String name = list.get(0);
				String docStr = list.get(1);
				String docStr2 = docStr.replaceAll("\\\\n", "\n");
				String argsReceived = list.get(2);
				/*
				 * remove final parenthesis if move args is more than a pair of parenthesis as we want to show the user
				 * the arguments but not allow them to just hit return
				 */
				if (argsReceived.length() > 2 && argsReceived.charAt(argsReceived.length() - 1) == ')') {
					argsReceived = argsReceived.substring(0, argsReceived.length() - 1);
				}
				int type;
				try {
					type = Integer.parseInt(list.get(3));
				} catch (NumberFormatException e) {
					type = 0;
				}
				ret.add(new AutoCompletionParts(name, type, docStr2, argsReceived));
			}
		}
		return ret;
	}

}


