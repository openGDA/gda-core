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

package uk.ac.gda.client.live.stream.view.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.live.stream.view.StreamViewUtility;

public class StreamViewUtilityTest {

	@Test
	public void testGetStreamType() {
		// If multiple streams are available, the preference order for stream type (for the purposes of the live stream
		// view) is: MJPEG, EPICS array, EPICS PVA
		final CameraConfiguration cameraConfig = new CameraConfiguration();

		assertEquals(StreamType.UNKNOWN, StreamViewUtility.getStreamType(cameraConfig));

		cameraConfig.setPvAccessPv("BLXXI-EA-DET-01:PVA:Image");
		assertEquals(StreamType.EPICS_PVA, StreamViewUtility.getStreamType(cameraConfig));

		cameraConfig.setArrayPv("BLXXB-EA-DET-01:ARR");
		assertEquals(StreamType.EPICS_ARRAY, StreamViewUtility.getStreamType(cameraConfig));

		cameraConfig.setUrl("http://blxxi-mo-serv-01.diamond.ac.uk:8080/det1.mjpg.mjpg");
		assertEquals(StreamType.MJPEG, StreamViewUtility.getStreamType(cameraConfig));
	}

	@Test
	public void testGetSecondaryIdFromCameraConfig() {
		final CameraConfiguration cameraConfig = new CameraConfiguration();
		cameraConfig.setName("CAM01");

		assertEquals("CAM01#UNKNOWN", StreamViewUtility.getSecondaryId(cameraConfig));

		cameraConfig.setPvAccessPv("BLXXI-EA-DET-01:PVA:Image");
		assertEquals("CAM01#EPICS_PVA", StreamViewUtility.getSecondaryId(cameraConfig));

		cameraConfig.setArrayPv("BLXXB-EA-DET-01:ARR");
		assertEquals("CAM01#EPICS_ARRAY", StreamViewUtility.getSecondaryId(cameraConfig));

		cameraConfig.setUrl("http://blxxi-mo-serv-01.diamond.ac.uk:8080/det1.mjpg.mjpg");
		assertEquals("CAM01#MJPEG", StreamViewUtility.getSecondaryId(cameraConfig));
	}

	@Test
	public void testGetSecondaryIdFromCameraConfigAndStreamType() {
		final CameraConfiguration cameraConfig = new CameraConfiguration();
		cameraConfig.setName("CAM01");

		assertEquals("CAM01#UNKNOWN", StreamViewUtility.getSecondaryId(cameraConfig, StreamType.UNKNOWN));
		assertEquals("CAM01#EPICS_PVA", StreamViewUtility.getSecondaryId(cameraConfig, StreamType.EPICS_PVA));
		assertEquals("CAM01#EPICS_ARRAY", StreamViewUtility.getSecondaryId(cameraConfig, StreamType.EPICS_ARRAY));
		assertEquals("CAM01#MJPEG", StreamViewUtility.getSecondaryId(cameraConfig, StreamType.MJPEG));
	}
}
