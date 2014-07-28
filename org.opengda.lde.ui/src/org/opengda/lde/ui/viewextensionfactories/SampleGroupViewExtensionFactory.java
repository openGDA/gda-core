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

package org.opengda.lde.ui.viewextensionfactories;

import gda.rcp.views.ViewFactory;

/**
 * An Extension Factory for contribute SequenceView to the workbench's public extension point {@org.eclipse.ui.views}.
 * 
 * This allows the extensions to be made available for use by RCP applications without exposing their concrete implementation classes.
 * It expects a factory instance of {@link gda.rcp.views.FindableExecutableExtension} as parameter to the extension point. This factory instance
 * creates the actual SequenceView instance that contribute to the extension point.
 *
 */
public class SampleGroupViewExtensionFactory extends ViewFactory {
	public static final String ID = "org.opengda.lde.ui.views.SampleGroupView";
}
