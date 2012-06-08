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

package gda.device.detector.areadetector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gda.device.detector.areadetector.impl.AreaDetectorLiveViewImpl;
import gda.device.detector.areadetector.impl.EpicsAreaDetectorROIElementImpl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
@Ignore
/**
 *
 */
public class AreaDetectorLiveViewTest {

	private EPICSAreaDetectorImage epicsAreaDetectorImage;
	private AreaDetectorLiveView areaDetectorLiveView;

	/**
	 */
	@Before
	public void setUp() {
		areaDetectorLiveView = new AreaDetectorLiveViewImpl();
		//areaDetectorLiveView.setName("PCOPreview");
		areaDetectorLiveView.setRefreshTime(1000);
		areaDetectorLiveView.setPlotName("PCOPlot");

		EPICSAreaDetectorImage epicsAreaDetectorImage2 = createEPICSAreaDetectorImage();

		areaDetectorLiveView.setImage(epicsAreaDetectorImage2);
		
		EpicsAreaDetectorROIElement epicsAreaDetectorROIElement = new EpicsAreaDetectorROIElementImpl();
		epicsAreaDetectorROIElement.setBasePVName("TEST:ROI:1:");
		epicsAreaDetectorROIElement.setInitialDataType("UInt32");
		epicsAreaDetectorROIElement.setInitialMinX(0);
		epicsAreaDetectorROIElement.setInitialMinY(0);
		epicsAreaDetectorROIElement.setInitialSizeX(4008);
		epicsAreaDetectorROIElement.setInitialSizeY(2672);
		epicsAreaDetectorROIElement.setInitialBinX(4);
		epicsAreaDetectorROIElement.setInitialBinY(4);
		
		areaDetectorLiveView.setImageROI(epicsAreaDetectorROIElement);
	}

	private EPICSAreaDetectorImage createEPICSAreaDetectorImage() {
		if (epicsAreaDetectorImage == null) {
			epicsAreaDetectorImage = null;//new EPICSAreaDetectorImage();
			epicsAreaDetectorImage.setBasePVName("TEST:ARR1:");
			epicsAreaDetectorImage.setInitialArrayPort("pcoROI");
			epicsAreaDetectorImage.setInitialArrayAddress("1");
		}
		return epicsAreaDetectorImage;
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorLiveView#getImage()}.
	 */
	@Test
	public final void testGetImage() {
			assertNotNull("EPICSAreaDetector Image is null", areaDetectorLiveView.getImage());
	}

	/**
	 * Test method for
	 * {@link gda.device.detector.areadetector.AreaDetectorLiveView#setImage(gda.device.detector.areadetector.EPICSAreaDetectorImage)}
	 * .
	 */
	@Test
	public final void testSetImage() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorLiveView#getRefreshTime()}.
	 */
	@Test
	public final void testGetRefreshTime() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorLiveView#setRefreshTime(int)}.
	 */
	@Test
	public final void testSetRefreshTime() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorLiveView#getPlotName()}.
	 */
	@Test
	public final void testGetPlotName() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorLiveView#setPlotName(java.lang.String)}.
	 */
	@Test
	public final void testSetPlotName() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorLiveView#getImageROI()}.
	 */
	@Test
	public final void testGetImageROI() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link gda.device.detector.areadetector.AreaDetectorLiveView#setImageROI(gda.device.detector.areadetector.EpicsAreaDetectorROIElement)}
	 * .
	 */
	@Test
	public final void testSetImageROI() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for configure
	 */
	@Test
	public final void testConfigure() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorLiveView#start()}.
	 */
	@Test
	public final void testStart() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorLiveView#stop()}.
	 */
	@Test
	public final void testStop() {
		fail("Not yet implemented");
	}

}
