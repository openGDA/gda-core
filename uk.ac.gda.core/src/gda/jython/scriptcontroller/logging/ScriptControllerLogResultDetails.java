/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.jython.scriptcontroller.logging;

import java.io.Serializable;
import java.util.HashMap;

/**
 * TODO reference to parent required
 */
public class ScriptControllerLogResultDetails implements Serializable{

	private String uniqueID;
	private HashMap<String,String> details = new HashMap<String,String>();  // column,value

	public ScriptControllerLogResultDetails(String uniqueID, HashMap<String, String> details) {
		super();
		this.uniqueID = uniqueID;
		this.details = details;
	}

	public HashMap<String, String> getDetails() {
		return details;
	}

	public String getUniqueID() {
		return uniqueID;
	}
	
	@Override
	public String toString() {
		String output = "";
		for (String detail : details.keySet()){
			output += detail + "\t-\t" + details.get(detail) + "\n";
		}
		return output.trim();
	}
}
