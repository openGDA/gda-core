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

package gda.scan;

import gda.device.DeviceException;
import gda.factory.Finder;
import gda.util.simpleServlet.corba.impl.SimpleServletAdapter;

/**
 *
 */
public class ScanDataPointClient {
	public static final String SCAN_DATA_STORE = "ScanDataStore";
	static ScanData lastScanData;

	/**
	 * @param sdpt
	 * @return A ScanDataPoint generated from the given ScanDataPointToken
	 * @throws DeviceException
	 */
	public static IScanDataPoint convertToken(ScanDataPointVar sdpt) throws DeviceException {
		if (lastScanData == null || !lastScanData.getUniqueName().equals(sdpt.getToken().getId())) {
			
			final IScanDataPointServer server = Finder.getInstance().findNoWarn(ScanDataPointServer.class.getSimpleName());
			
			if (server != null) {
				// we have a client-side proxy to the real server
				lastScanData = server.___convertTokenId(sdpt.getToken().getId());
			}
			
			else {
				// use the SimpleServlet
				lastScanData = (ScanData) SimpleServletAdapter.runServlet(
					SCAN_DATA_STORE,
					ScanDataPointServer.class.getName(),
					"__convertTokenId",
					sdpt.getToken().getId());
			}
		}
		return new ScanDataPoint(lastScanData, sdpt);
	}

}
