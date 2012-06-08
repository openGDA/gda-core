/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.batoncontrol;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Sent to clients regularly. In response the client must call amBatonHolder less the lease is given up.
 */
public class BatonLeaseRenewRequest implements Serializable{
	
	private final long timestamp = System.currentTimeMillis();
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		return String.format("%s(%s)", getClass().getSimpleName(), sdf.format(new Date(timestamp)));
	}

}
