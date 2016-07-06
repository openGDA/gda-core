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

package gda.scan;

import org.eclipse.january.dataset.Dataset;

import gda.TestHelpers;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.Detector;
import gda.device.Scannable;

public class ScanDataPointProvider {
	ScanDataPoint[] points;
	Scannable scannable1 = TestHelpers.createTestScannable("scannable1",
			new double[] { 1, 2, 3 }, new String[] { "s1_E1" }, new String[] { "s1_I1", "s1_I2" },
			1, new String[] { "%5.1g", "%5.1g", "%5.1g" }, null);

	Scannable scannable2 = TestHelpers.createTestScannable("scannable2",
			new double[] { 4, 5, 6 }, new String[] { "s2_E1", "s2_E2", "s2_E3"}, new String[] {  },
			1, new String[] { "%5.1g", "%5.1g", "%5.1g" }, null);

	Scannable scannable3 = TestHelpers.createTestScannable("scannable3",
			new double[]  { 7}, new String[] { }, new String[] { "s3_I1" },
			1, new String[] { "%5.1g" }, null);
	int[] data2In;


	public void preparePoints(int num){
		int[] dims2 = new int[] { 2, 3 };

		NexusGroupData ngd = TestHelpers.createTestNexusGroupData(dims2, Dataset.INT32, true);
		data2In = (int[]) ngd.getBuffer();
		Detector det = TestHelpers.createTestDetector("SimpleDetector2", 0., new String[] {},
				new String[] { "simpleDetector2" }, 0, new String[] { "%5.2g" }, ngd, null, "description2", "detectorID2",
				"detectorType2");

		points = new ScanDataPoint[num];
		String uniqueName = Long.toString(System.currentTimeMillis());

		for (int i = 0; i < num; i++) {

			ScanDataPoint sdp = new ScanDataPoint();
			sdp.setUniqueName(uniqueName);
			sdp.addScannable(scannable1);
			sdp.addScannable(scannable2);
			sdp.addScannable(scannable3);
			sdp.addDetector(det);
			sdp.setCurrentPointNumber(i);
			sdp.setNumberOfPoints(num);
			sdp.setScanDimensions(new int[]{num});
			points[i] = sdp;
		}
	}

	public void publishPoints(ScanDataPointPipeline publisher) {
		try {
			double x=0;
			int ix=0;
			for (ScanDataPoint point : points) {
				data2In[0]= ix*10;
				scannable1.asynchronousMoveTo(new double[] { x, x*2, 3*x });
				x = x-1.0;
				ix+=1;
				publisher.put(point);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
