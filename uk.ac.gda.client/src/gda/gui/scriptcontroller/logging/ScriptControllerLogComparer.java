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

package gda.gui.scriptcontroller.logging;

import gda.jython.scriptcontroller.logging.ScriptControllerLogResults;

import org.eclipse.jface.viewers.IElementComparer;

public class ScriptControllerLogComparer implements IElementComparer {

	@Override
	public boolean equals(Object a, Object b) {
		// not expecting any other object type except ScriptControllerLogResults in this tree
		if (a instanceof ScriptControllerLogResults && b instanceof ScriptControllerLogResults){
			return ((ScriptControllerLogResults)a).getUniqueID().equals(((ScriptControllerLogResults)b).getUniqueID());
		}
		if (a instanceof ScriptControllerLogResults[] && b instanceof ScriptControllerLogResults[]){
			
			ScriptControllerLogResults[] aArray = ((ScriptControllerLogResults[])a);
			ScriptControllerLogResults[] bArray = ((ScriptControllerLogResults[])b);
			
			if (aArray.length != bArray.length) return false;
			
			for (int element = 0; element < aArray.length; element++){
				if (!aArray[element].equals(bArray[element])) return false;
			}
			return true;
//			return ((ScriptControllerLogResults)a).getUniqueID().equals(((ScriptControllerLogResults)b).getUniqueID());
		}
		return a.equals(b);
	}

	@Override
	public int hashCode(Object element) {
		// not expecting any other object type except ScriptControllerLogResults in this tree
		if (element instanceof ScriptControllerLogResults){
			((ScriptControllerLogResults)element).hashCode();
		}
		return element.hashCode();
	}

}
