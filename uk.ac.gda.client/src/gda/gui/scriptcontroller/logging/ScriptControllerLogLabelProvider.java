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

import gda.jython.scriptcontroller.logging.ScriptControllerLogResultDetails;
import gda.jython.scriptcontroller.logging.ScriptControllerLogResults;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class ScriptControllerLogLabelProvider extends LabelProvider {

	ScriptControllerLogContentProvider content;

	public ScriptControllerLogLabelProvider(ScriptControllerLogContentProvider content) {
		super();
		this.content = content;
	}

	@Override
	public void dispose() {
		// no images to dispose of
	}

	@Override
	public Image getImage(Object arg0) {
		return null; // no images yet
	}

	@Override
	public String getText(Object arg0) {
		if (arg0 instanceof ScriptControllerLogResults) {
			ScriptControllerLogResults result = (ScriptControllerLogResults) arg0;
			return result.toString();
		} else if (arg0 instanceof ScriptControllerLogResultDetails) {
			return arg0.toString();
		}
		return null;
	}

}
