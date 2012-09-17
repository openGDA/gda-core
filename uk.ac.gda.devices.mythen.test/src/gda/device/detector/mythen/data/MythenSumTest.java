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

package gda.device.detector.mythen.data;

import java.util.List;

import java.util.Map;
import java.util.TreeMap;

import gda.device.detector.mythen.data.MythenSum.AlignedData;
import gda.device.detector.mythen.data.MythenSum.ModuleBoundary;

import org.junit.Test;
import static org.junit.Assert.*;

public class MythenSumTest {
	
	@Test
	public void testDetermineModuleBoundariesWithNoBadChannels() {
		BadChannelProvider badChannels = new SimpleBadChannelProvider();
		ModuleBoundary[] boundaries = MythenSum.determineModuleBoundaries(2, badChannels);
		assertEquals(0,    boundaries[0].first);
		assertEquals(1279, boundaries[0].last);
		assertEquals(1280, boundaries[1].first);
		assertEquals(2559, boundaries[1].last);
	}
	
	@Test
	public void testDetermineModuleBoundariesWithBadChannels() {
		BadChannelProvider badChannels = new SimpleBadChannelProvider(100); // in 1st module
		ModuleBoundary[] boundaries = MythenSum.determineModuleBoundaries(2, badChannels);
		assertEquals(0,    boundaries[0].first);
		assertEquals(1278, boundaries[0].last);
		assertEquals(1279, boundaries[1].first);
		assertEquals(2558, boundaries[1].last);
		
		badChannels = new SimpleBadChannelProvider(1400); // in 2nd module
		boundaries = MythenSum.determineModuleBoundaries(2, badChannels);
		assertEquals(0,    boundaries[0].first);
		assertEquals(1279, boundaries[0].last);
		assertEquals(1280, boundaries[1].first);
		assertEquals(2558, boundaries[1].last);
	}
	
	@Test
	public void testModuleForChannel() {
		assertEquals(0, MythenSum.moduleForChannel(0));
		assertEquals(0, MythenSum.moduleForChannel(1279));
		assertEquals(1, MythenSum.moduleForChannel(1280));
		assertEquals(17, MythenSum.moduleForChannel(18 * 1280 - 1));
	}
	
	@Test
	public void testInterpolateWithZeroCountsForBothAngles() {
		double[] lowerData = new double[] {20, 0, 0};
		double[] upperData = new double[] {30, 0, 0};
		double interpolatedCount = MythenSum.interpolate(25, lowerData, upperData);
		assertEquals(0.0, interpolatedCount, 0);
	}
	
	@Test
	public void testInterpolateWithPositiveGradient() {
		double[] lowerData = new double[] {20, 10, 0};
		double[] upperData = new double[] {30, 20, 0};
		double interpolatedCount = MythenSum.interpolate(25, lowerData, upperData);
		assertEquals(15.0, interpolatedCount, 0.000001);
	}
	
	@Test
	public void testInterpolateWithNegativeGradient() {
		double[] lowerData = new double[] {20, 20, 0};
		double[] upperData = new double[] {30, 10, 0};
		double interpolatedCount = MythenSum.interpolate(25, lowerData, upperData);
		assertEquals(15.0, interpolatedCount, 0.000001);
	}
	
	@Test
	public void testAddAlignedDataForNewGridIndex() {
		Map<Integer, List<AlignedData>> alignedData = new TreeMap<Integer, List<AlignedData>>();
		
		assertEquals(0, alignedData.size());
		
		AlignedData newDataPoint = new AlignedData();
		newDataPoint.count = 1234;
		newDataPoint.source = 1;
		final int gridIndex = 500;
		MythenSum.addAlignedData(newDataPoint, gridIndex, alignedData);
		
		assertEquals(1, alignedData.size());
		final int theOnlyGridIndex = alignedData.entrySet().iterator().next().getKey();
		final List<AlignedData> dataPointsForTheOnlyGridIndex = alignedData.get(theOnlyGridIndex);
		assertEquals(1, dataPointsForTheOnlyGridIndex.size());
		assertSame(newDataPoint, dataPointsForTheOnlyGridIndex.get(0));
	}
	
	@Test
	public void testAddAlignedDataForExistingGridIndex() {
		Map<Integer, List<AlignedData>> alignedData = new TreeMap<Integer, List<AlignedData>>();
		
		assertEquals(0, alignedData.size());
		
		AlignedData firstDataPoint = new AlignedData();
		firstDataPoint.count = 1234;
		firstDataPoint.source = 1;
		final int gridIndex = 500;
		MythenSum.addAlignedData(firstDataPoint, gridIndex, alignedData);
		
		assertEquals(1, alignedData.size());
		
		AlignedData secondDataPoint = new AlignedData();
		secondDataPoint.count = 5678;
		secondDataPoint.source = 2;
		MythenSum.addAlignedData(secondDataPoint, gridIndex, alignedData);
		
		assertEquals(1, alignedData.size());
		final int theOnlyGridIndex = alignedData.entrySet().iterator().next().getKey();
		final List<AlignedData> dataPointsForTheOnlyGridIndex = alignedData.get(theOnlyGridIndex);
		assertEquals(2, dataPointsForTheOnlyGridIndex.size());
		assertSame(firstDataPoint, dataPointsForTheOnlyGridIndex.get(0));
		assertSame(secondDataPoint, dataPointsForTheOnlyGridIndex.get(1));
	}
}
