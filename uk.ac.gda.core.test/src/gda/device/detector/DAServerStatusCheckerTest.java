/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("This test is intended to be run on a beamline workstation to check DAServerStatusChecker responses are correct")
public class DAServerStatusCheckerTest {

	private DAServerStatusChecker checker;

	//DAServer host for i20-1, i20, b18
	private final List<String> hosts = Arrays.asList("bl20j-ea-tfg2-01.diamond.ac.uk",
			"i20-xspress0.diamond.ac.uk",
			"b18-xspress1.diamond.ac.uk");

	private int port;
	private String host;

	@BeforeEach
	public void setup() {
		checker = new DAServerStatusChecker();
		host = hosts.get(0);
		port = 1972;
	}

	@Test
	public void testDetectsAllOk() {
		assertTrue(checker.checkStatus(host, port));
	}

	@Test
	public void testDetectsWrongIP() {
		assertFalse(checker.checkStatus(host + ".zzz", port));
	}

	@Test
	public void testDetectsWrongPort() {
		assertFalse(checker.checkStatus(host, port + 10));
	}

}
