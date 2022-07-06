/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class EclipseWidgetUtils {
	private static Composite getTopParentWithLayout(Composite cmp){
		Composite parent = cmp.getParent();
		if( parent == null || parent.getLayout() == null || parent instanceof Shell )
			return cmp;
		return getTopParentWithLayout(parent);
	}

	public static void forceLayoutOfTopParent(Composite cmp){
		getTopParentWithLayout(cmp).layout(true, true);
	}
}
