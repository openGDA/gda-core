/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.scannable.ScannableMotor;

/**
 * tests the methods which fetch data out of the ScanDataPoint are self-consistent
 */
class ScanDataPointTest {

	private ScanDataPoint sdp;

	static ScanDataPoint createScanDataPoint() throws DeviceException{
		//sdp should not care what mix of input/extra names scannables use
		Scannable scannable1 = mock(ScannableMotor.class);
		when(scannable1.getPosition()).thenReturn(new double[] { 1, 2, 3 });
		when(scannable1.getInputNames()).thenReturn(new String[] { "s1_I1", "s1_I2" });
		when(scannable1.getExtraNames()).thenReturn(new String[] { "s1_E1" });
		when(scannable1.getOutputFormat()).thenReturn(new String[] { "%5.1g", "%5.1g", "%5.1g" });
		when(scannable1.getName()).thenReturn("scannable1");
		Scannable scannable2 = mock(ScannableMotor.class);
		when(scannable2.getPosition()).thenReturn(new double[] { 4, 5, 6 });
		when(scannable2.getInputNames()).thenReturn(new String[] {});
		when(scannable2.getExtraNames()).thenReturn(new String[] { "s2_E1", "s2_E2", "s2_E3" });
		when(scannable2.getOutputFormat()).thenReturn(new String[] { "%5.1g", "%5.1g", "%5.1g" });
		when(scannable2.getName()).thenReturn("scannable2");
		Scannable scannable3 = mock(ScannableMotor.class);
		when(scannable3.getPosition()).thenReturn(7);
		when(scannable3.getInputNames()).thenReturn(new String[] { "s3_I1" });
		when(scannable3.getExtraNames()).thenReturn(new String[] {});
		when(scannable3.getOutputFormat()).thenReturn(new String[] { "%5.1g" });
		when(scannable3.getName()).thenReturn("scannable3");

		// detector output should be defined by their extranames. Input names are ignored.
		Detector det1 = mock(DummyDetector.class);
		when(det1.readout()).thenReturn(new double[] { 1, 2, 3, 4, 5, 6 });
		when(det1.getInputNames()).thenReturn(new String[] { "time" });
		when(det1.getExtraNames()).thenReturn(
				new String[] { "det1_c1", "det1_c2", "det1_c3", "det1_c4", "det1_c5", "det1_c6" });
		when(det1.getOutputFormat()).thenReturn(new String[] { "%2d", "%2d", "%2d", "%2d", "%2d", "%2d", "%2d" });
		when(det1.getName()).thenReturn("det1");

		ScanDataPoint sdp = new ScanDataPoint();
		sdp.addDetector(det1);
		sdp.addDetectorData(det1.readout(),new String[] { "%2d", "%2d", "%2d", "%2d", "%2d", "%2d" });

		sdp.addScannable(scannable1);
		sdp.addScannablePosition(scannable1.getPosition(), scannable1.getOutputFormat());
		sdp.addScannable(scannable2);
		sdp.addScannablePosition(scannable2.getPosition(), scannable2.getOutputFormat());
		sdp.addScannable(scannable3);
		sdp.addScannablePosition(scannable3.getPosition(), scannable3.getOutputFormat());
		return sdp;

	}
	@BeforeEach
	public void setup() {
		try {
			sdp = createScanDataPoint();
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}

	@Test
	void testHeaders() {
		assertThat(sdp.getDetectorHeader().size(), is(6));
		assertThat(sdp.getPositionHeader().size(), is(7));
		assertThat(sdp.getHeaderString(), is(equalTo("s1_I1	s1_I2	s1_E1	s2_E1	s2_E2	s2_E3	s3_I1	det1_c1	det1_c2	det1_c3	det1_c4	det1_c5	det1_c6")));
	}

	@Test
	void testAsDoubles() {
		assertThat(sdp.getPositionsAsDoubles(), is(equalTo(new Double[] { 1., 2., 3., 4., 5., 6.,7. })));
		assertThat(sdp.getDetectorDataAsDoubles(), is(equalTo(new Double[] { 1., 2., 3., 4., 5., 6. })));
	}

	@Test
	void testAsStrings() {
		assertThat(sdp.getPositionsAsFormattedStrings(), is(arrayContaining("    1","    2","    3","    4","    5","    6","    7")));
		assertThat(sdp.toFormattedString(), is(equalTo("    1	    2	    3	    4	    5	    6	    7	    1.0	    2.0	    3.0	    4.0	    5.0	    6.0")));
	}

	@Test
	void testArrayLengths() {
		assertThat(sdp.getPositionHeader().size(), is(sdp.getPositionsAsDoubles().length));
		assertThat(sdp.getDetectorHeader().size(), is(sdp.getDetectorDataAsDoubles().length));
		assertThat(sdp.getDetectorNames().size(), is(sdp.getDetectorData().size()));
		assertThat(sdp.getScannableNames().size(), is(sdp.getPositions().size()));
	}

	@Test
	void testToString()  {
		assertThat(sdp.toString(), is(equalTo("ScanDataPoint [point=0/-1, scan=]")));
	}

}
