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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.IntStream;

import org.eclipse.january.dataset.IntegerDataset;

import gda.TestHelpers;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.Detector;
import gda.device.Scannable;

public class ScanDataPointProvider {
	private ScanDataPoint[] points;

	private Scannable scannable1 = TestHelpers.createTestScannable("scannable1",
			new double[] { 1, 2, 3 }, new String[] { "s1_E1" }, new String[] { "s1_I1", "s1_I2" },
			1, new String[] { "%5.1g", "%5.1g", "%5.1g" }, null);

	private Scannable scannable2 = TestHelpers.createTestScannable("scannable2",
			new double[] { 4, 5, 6 }, new String[] { "s2_E1", "s2_E2", "s2_E3"}, new String[] {  },
			1, new String[] { "%5.1g", "%5.1g", "%5.1g" }, null);

	private Scannable scannable3 = TestHelpers.createTestScannable("scannable3",
			new double[]  { 7}, new String[] { }, new String[] { "s3_I1" },
			1, new String[] { "%5.1g" }, null);

	private int[] data2In;

	public void preparePoints(int num) {
		int[] dims2 = new int[] { 2, 3 };

		NexusGroupData ngd = TestHelpers.createTestNexusGroupData(IntegerDataset.class, dims2, true);
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

	/**
	 * simulate 2 scannable groups used in scan command may contain the same scannable.
	 * @param number
	 * @param totalPoints
	 * @param scannableData
	 * @param detectorData
	 * @return Scan Data Point
	 */
	public static IScanDataPoint getPointWithDuplicatedHeader(int number, int totalPoints,
			Collection<Double> scannableData, Collection<Object> detectorData) {
		final ScanDataPoint point = (ScanDataPoint) getPoint(number, totalPoints, scannableData, detectorData);
		point.setScannableHeader(scannableData.stream().map(s -> "scan").toArray(String[]::new));
		point.setScannablePositions(new ArrayList<>(scannableData));
		return point;
	}

	public static IScanDataPoint getPoint(int number, int totalPoints, Collection<Double> scannableData, Collection<Object> detectorData) {
		final ScanDataPoint point = new ScanDataPoint();
		point.setCurrentPointNumber(number);
		point.setDetectorHeader(getHeader("det", detectorData.size()));
		final String[][] format = detectorData.stream().map(d -> new String[] {"%f"}).toArray(String[][]::new);
		point.setDetectorData(new ArrayList<>(detectorData), format);
		point.setScannableHeader(getHeader("scan", scannableData.size()));
		point.setScannablePositions(new ArrayList<>(scannableData));
		point.setNumberOfPoints(totalPoints);
		return point;
	}

	private static String[] getHeader(String prefix, int size) {
		return switch (size) { // shortcuts for small sizes
			case 0 -> new String[0];
			case 1 -> new String[] { prefix + "0" };
			case 2 -> new String[] { prefix + "0", prefix + "1" };
			default -> IntStream.range(0, size).mapToObj(i -> prefix + i).toArray(String[]::new);
		};
	}

}
