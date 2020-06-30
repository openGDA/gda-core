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
	 * Tests {@link LiveStreamConnection#similarConfiguration(CameraConfiguration, StreamType)}
	 * Compares two empty configurations.
	 *
	 * @throws EventException
	 */
	@Test
	public void emptyCameraConfigurationTest() throws EventException {
		CameraConfiguration firstConfig = new CameraConfiguration();
		StreamType streamType = StreamType.EPICS_ARRAY;
		try (LiveStreamConnection liveStreamconnection = new LiveStreamConnection(firstConfig, streamType)) {
			CameraConfiguration other = new CameraConfiguration();
			// Return true because both are null.
			assertTrue(liveStreamconnection.similarConfiguration(other, StreamType.MJPEG));
			assertTrue(liveStreamconnection.similarConfiguration(other, StreamType.EPICS_ARRAY));
			assertTrue(liveStreamconnection.similarConfiguration(other, StreamType.EPICS_PVA));
		}
	}

	/**
	 * Tests {@link LiveStreamConnection#similarConfiguration(CameraConfiguration, StreamType)}
	 * Compares two configurations containing only arrayPV, first equal then different
	 *
	 * @throws EventException
	 */
	@Test
	public void hasOnlyArrayPVTest() throws EventException {
		String firstArrayPV = "arrayPv";
		String anotherArrayPV = "anotherArrayPv";
		CameraConfiguration firstConfig = new CameraConfiguration();
		firstConfig.setArrayPv(firstArrayPV);
		StreamType streamType = StreamType.EPICS_ARRAY;
		try (LiveStreamConnection liveStreamconnection = new LiveStreamConnection(firstConfig, streamType)) {
			CameraConfiguration other = new CameraConfiguration();
			other.setArrayPv(firstArrayPV);
			// Is the same epics configuration
			assertTrue(liveStreamconnection.similarConfiguration(other, StreamType.EPICS_ARRAY));

			other.setArrayPv(anotherArrayPV);
			// Is another epics configuration
			assertTrue(!liveStreamconnection.similarConfiguration(other, StreamType.EPICS_ARRAY));
		}
	}

	/**
	 * Tests {@link LiveStreamConnection#similarConfiguration(CameraConfiguration, StreamType)}
	 * Compares two configurations containing only URL, first equal then different
	 *
	 * @throws EventException
	 */
	@Test
	public void hasOnlyURLTest() throws EventException {
		String firstURL = "http://gda.ac.uk";
		String anotherURL = "http://dawn.ac.uk";
		CameraConfiguration firstConfig = new CameraConfiguration();
		firstConfig.setUrl(firstURL);
		StreamType streamType = StreamType.MJPEG;
		try (LiveStreamConnection liveStreamconnection = new LiveStreamConnection(firstConfig, streamType)) {
			CameraConfiguration other = new CameraConfiguration();
			other.setUrl(firstURL);
			// Is the same url configuration
			assertTrue(liveStreamconnection.similarConfiguration(other, StreamType.MJPEG));

			other.setUrl(anotherURL);
			// Is another url configuration
			assertTrue(!liveStreamconnection.similarConfiguration(other, StreamType.MJPEG));
		}
	}

	/**
	 * Tests {@link LiveStreamConnection#similarConfiguration(CameraConfiguration, StreamType)}
	 * Compares two configurations containing both URL and arrayPV, alternatively equal
	 *
	 * @throws EventException
	 */
	@Test
	public void hasURLAndPVArrayTest() throws EventException {
		String firstURL = "http://gda.ac.uk";
		String anotherURL = "http://dawn.ac.uk";
		String firstArrayPV = "arrayPv";
		String anotherArrayPV = "anotherArrayPv";

		CameraConfiguration firstConfig = new CameraConfiguration();
		firstConfig.setUrl(firstURL);
		firstConfig.setArrayPv(firstArrayPV);
		StreamType streamType = StreamType.EPICS_ARRAY;
		try (LiveStreamConnection liveStreamconnection = new LiveStreamConnection(firstConfig, streamType)) {
			CameraConfiguration other = new CameraConfiguration();
			other.setUrl(firstURL);
			other.setArrayPv(anotherArrayPV);
			// Another camera configuration has the same URL
			assertTrue(liveStreamconnection.similarConfiguration(other, StreamType.MJPEG));
			// Another camera configuration has another PVArray
			assertTrue(!liveStreamconnection.similarConfiguration(other, StreamType.EPICS_ARRAY));

			other.setUrl(anotherURL);
			other.setArrayPv(firstArrayPV);
			// Another camera configuration has the same URL
			assertTrue(!liveStreamconnection.similarConfiguration(other, StreamType.MJPEG));
			// Another camera configuration has another PVArray
			assertTrue(liveStreamconnection.similarConfiguration(other, StreamType.EPICS_ARRAY));
		}
	}
}
