/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device;

import org.eclipse.scanning.api.AbstractNameable;

/**
 * A field that can be part of a metadata device.
 */
public abstract class AbstractMetadataField extends AbstractNameable implements MetadataNode {

	protected AbstractMetadataField() {
		// no-arg constructor for spring initialization
	}

	protected AbstractMetadataField(String name) {
		setName(name);
	}

	// no additional fields or method are needed so far

}
