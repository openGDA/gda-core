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

package gda.rcp.views;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface for Factories that create composites. Part of work to allow views to be configured in Spring
 *
 * @author Jonathan Blakes
 * @author Maurizio Nagni
 */

public interface CompositeFactory {

	/**
	 * Defines a well specific property in {@link Composite#getData(String)} which identifies a composite instance as
	 * parent for the contained children.
	 */
	public static final String COMPOSITE_ROOT = "COMPOSITE_ROOT";

	Composite createComposite(Composite parent, int style);
}
