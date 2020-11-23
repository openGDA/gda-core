/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scanning.api.event.EventException;
import org.junit.Test;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Tests {@link LiveStreamConnection} operations
 *
 * @author Maurizio Nagni
 */
public class LiveStreamConnectionTest {

	/**
	 * Tests {@link LiveStreamConnection#sameConfiguration(CameraConfiguration, StreamType)}
	 * Compares two empty configurations.
	 *
	 * @throws EventException
	 */
	@Test
	public void emptyCameraConfigurationTest() throws EventException {
		final CameraConfiguration firstConfig = new CameraConfiguration();
		final StreamType streamType = StreamType.EPICS_ARRAY;
		try (LiveStreamConnection liveStreamconnection = new LiveStreamConnection(firstConfig, streamType)) {
			final CameraConfiguration other = new CameraConfiguration();
			// Camera config & stream type both have to match
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.MJPEG));
			assertTrue(liveStreamconnection.sameConfiguration(other, StreamType.EPICS_ARRAY));
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.EPICS_PVA));
		}
	}

	/**
	 * Tests {@link LiveStreamConnection#sameConfiguration(CameraConfiguration, StreamType)}
	 * Compares two configurations containing only arrayPV, first equal then different
	 *
	 * @throws EventException
	 */
	@Test
	public void hasOnlyArrayPVTest() throws EventException {
		final String firstArrayPV = "arrayPv";
		final String anotherArrayPV = "anotherArrayPv";
		final CameraConfiguration firstConfig = new CameraConfiguration();
		firstConfig.setArrayPv(firstArrayPV);
		final StreamType streamType = StreamType.EPICS_ARRAY;
		try (LiveStreamConnection liveStreamconnection = new LiveStreamConnection(firstConfig, streamType)) {
			final CameraConfiguration other = new CameraConfiguration();
			other.setArrayPv(firstArrayPV);
			// Is the same epics configuration
			assertTrue(liveStreamconnection.sameConfiguration(other, StreamType.EPICS_ARRAY));

			other.setArrayPv(anotherArrayPV);
			// Is another epics configuration
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.EPICS_ARRAY));
		}
	}

	/**
	 * Tests {@link LiveStreamConnection#sameConfiguration(CameraConfiguration, StreamType)}
	 * Compares two configurations containing only URL, first equal then different
	 *
	 * @throws EventException
	 */
	@Test
	public void hasOnlyURLTest() throws EventException {
		final String firstURL = "http://gda.ac.uk";
		final String anotherURL = "http://dawn.ac.uk";
		final CameraConfiguration firstConfig = new CameraConfiguration();
		firstConfig.setUrl(firstURL);
		final StreamType streamType = StreamType.MJPEG;
		try (LiveStreamConnection liveStreamconnection = new LiveStreamConnection(firstConfig, streamType)) {
			final CameraConfiguration other = new CameraConfiguration();
			other.setUrl(firstURL);
			// Is the same url configuration
			assertTrue(liveStreamconnection.sameConfiguration(other, StreamType.MJPEG));

			other.setUrl(anotherURL);
			// Is another url configuration
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.MJPEG));
		}
	}

	/**
	 * Tests {@link LiveStreamConnection#sameConfiguration(CameraConfiguration, StreamType)}
	 * Compares two configurations containing both URL and arrayPV, alternatively equal
	 *
	 * @throws EventException
	 */
	@Test
	public void hasURLAndPVArrayTest() throws EventException {
		final String firstURL = "http://gda.ac.uk";
		final String anotherURL = "http://dawn.ac.uk";
		final String firstArrayPV = "arrayPv";
		final String anotherArrayPV = "anotherArrayPv";

		final CameraConfiguration firstConfig = new CameraConfiguration();
		firstConfig.setUrl(firstURL);
		firstConfig.setArrayPv(firstArrayPV);
		try (LiveStreamConnection liveStreamconnection = new LiveStreamConnection(firstConfig, StreamType.EPICS_ARRAY)) {
			final CameraConfiguration other = new CameraConfiguration();

			// Same URL & array PV
			other.setUrl(firstURL);
			other.setArrayPv(firstArrayPV);
			// Different stream type
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.MJPEG));
			// Same stream type
			assertTrue(liveStreamconnection.sameConfiguration(other, StreamType.EPICS_ARRAY));

			// Same URL, different array PV
			other.setUrl(firstURL);
			other.setArrayPv(anotherArrayPV);
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.MJPEG));
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.EPICS_ARRAY));

			// Different URL, same array PV
			other.setUrl(anotherURL);
			other.setArrayPv(firstArrayPV);
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.MJPEG));
			assertFalse(liveStreamconnection.sameConfiguration(other, StreamType.EPICS_ARRAY));
		}
	}
}
