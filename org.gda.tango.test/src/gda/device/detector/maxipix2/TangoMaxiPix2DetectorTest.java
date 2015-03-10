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

package gda.device.detector.maxipix2;

import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.esrf.TangoApi.DeviceData;
import gda.device.TangoDeviceProxy;
import gda.util.TestUtils;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("2011/08/30 Test ignored since it requires i13-maxipix2 which is not generally available")
public class TangoMaxiPix2DetectorTest {


	@Before
	public void setUp() throws Exception {
		// this test can only run on certain machines
		String hostname;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			return;
		}
		if (hostname.startsWith("p99-ws100") || hostname.startsWith("ws002")) {
			TestUtils.skipTest(
					"TangoMaxiPix2DetectorTest requires access to a tango server which does not appear to be accessible from this machine (" + hostname + ")");
		}

		TangoDeviceProxy dev = new TangoDeviceProxy("tango://172.23.4.19:20000/dls/limaccd/mpx");
		DeviceData argin = new DeviceData();
		int k=0;
		argin.insert(k);
		DeviceData argout = dev.command_inout("getImage", argin);
		@SuppressWarnings("unused")
		byte[] byteData = argout.extractByteArray();
		argout.toString();
	}

	@Test
	public void testSetup(){
		
	}
}
