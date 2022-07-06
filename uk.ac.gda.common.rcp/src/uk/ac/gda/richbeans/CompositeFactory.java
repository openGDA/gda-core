/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.richbeans;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface for Factories that create composites.
 * <p>
 * This interface is intentionally identical to gda.rcp.views.CompositeFactory, in the uk.ac.gda.client plug-in. That
 * one cannot be used by code in the uk.ac.gda.common.rcp plug-in because it would create a cyclic dependency between
 * uk.ac.gda.common.rcp and uk.ac.gda.client. Ideally the classes which really are common should be moved down to the
 * uk.ac.gda.common.rcp plug-in, or perhaps the two plug-ins should just be merged. For now, though, duplicating this
 * interface avoids having to update lots of existing code which uses the version in uk.ac.gda.client.
 */
public interface CompositeFactory {

	public Composite createComposite(Composite parent, int style);
}
