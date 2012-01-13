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

package uk.ac.gda.common.rcp.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

public class DialogUtils {
	
	/**
	 * Centre shell in another.
	 * @param parent
	 * @param shell
	 */
	public static void centerDialog(Shell parent, Shell shell){
		Rectangle parentSize = parent.getBounds();
		Rectangle mySize = shell.getBounds();


		int locationX, locationY;
		locationX = (parentSize.width - mySize.width)/2+parentSize.x;
		locationY = (parentSize.height - mySize.height)/2+parentSize.y;


		shell.setLocation(new Point(locationX, locationY));
	}
}
