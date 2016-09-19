/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.util;

import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.spi.PropertyDefiner;
import gda.util.Version;

/**
 * A Logback {@link PropertyDefiner} that returns the GDA version, as defined by {@link Version#getRelease()}.
 *
 * @author James Mudd
 */
public class GdaVersionPropertyDefiner extends PropertyDefinerBase {

	@Override
	public String getPropertyValue() {
		return Version.getRelease();
	}

}
