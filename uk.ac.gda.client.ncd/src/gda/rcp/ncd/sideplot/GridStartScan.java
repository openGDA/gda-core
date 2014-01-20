/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.rcp.ncd.sideplot;

import gda.jython.JythonServerFacade;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class GridStartScan extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String errorMessage = "Unable to scan - has grid been deleted?";
		String jythonCommand = 
				"try:\n\tncdgridscan.scan()\nexcept AttributeError, e:\n\tif e.message == \"'NoneType' object has no attribute \'_jroi'\":\n\t\tprint '"
				+ errorMessage
				+ "'\n\telse:\n\t\tprint e.message";
		JythonServerFacade.getInstance().runCommand(jythonCommand);
		return true;
	}
}