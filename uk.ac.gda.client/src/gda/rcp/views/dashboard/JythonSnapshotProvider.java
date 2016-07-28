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

package gda.rcp.views.dashboard;

import gda.device.scannable.ScannableSnapshot;
import gda.device.scannable.ScannableUtils;
import gda.jython.JythonServerFacade;

public class JythonSnapshotProvider implements ScannableSnapshotProvider {

	@Override
	public ScannableSnapshot getSnapshot(String name) throws Exception {
		String serialized = JythonServerFacade.getInstance().evaluateCommand(
				"gda.device.scannable.ScannableUtils.getSerializedScannableSnapshot(" + name + ")");
		return ScannableUtils.deserializeScannableSnapshot(serialized);
	}

}
