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

package uk.ac.gda.jython;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class ExtendedJythonMarkers {

	public static void fixMarker(IMarker marker, String line, List<String> alii, final String file) throws CoreException {
		
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);

		int start = file.indexOf(line);
		marker.setAttribute(IMarker.CHAR_START, start);
		int end = start+line.length();
		marker.setAttribute(IMarker.CHAR_END, end);
		
		final String newMsg = ExtendedJythonSyntax.getCorrectionMessage(line, alii);
		if (newMsg!=null) marker.setAttribute(IMarker.MESSAGE, newMsg);
		
		final String res    = ExtendedJythonSyntax.getResolution(line, alii);
		if (res!=null) marker.setAttribute(ExtendedCommand.EXTENDED_QUICK_FIX, res);
	}

}
