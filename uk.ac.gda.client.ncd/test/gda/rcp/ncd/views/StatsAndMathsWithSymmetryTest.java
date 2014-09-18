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

package gda.rcp.ncd.views;

import static org.junit.Assert.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import gda.rcp.ncd.views.StatsAndMathsWithSymmetry;
import gda.rcp.ncd.views.StatsAndMathsWithSymmetry.DatasetWithCentre;

import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class StatsAndMathsWithSymmetryTest {

	@Test
	@Parameters({
		"begin, 10 , 0, 10",
		"end, 10 , 10, 0",
		"before, 10 , -1, 12",
		"after, 10 , 11, 0",
		"middle, 10 , 5, 0",
		"side, 10 , 4, 2",  // TODO check that is correct
		"other side, 10, 6, 0",
		"double centre, 3, 1.5, 0"
		})
	public void testGetUnflippedOffset(String label, int len, double centre, int result) {
		StatsAndMathsWithSymmetry samws = new StatsAndMathsWithSymmetry();
		assertEquals(label+" failed", result, samws.getUnflippedOffsetInNewDataset(len, centre));
	}

	@Test
	@Parameters({
		"begin, 10 , 0, 0",
		"end, 10 , 10, 10",
		"before, 10 , -1, 0",
		"after, 10 , 11, 12",
		"middle, 10 , 5, 0",
		"side, 10 , 4, 0",  // TODO check that is correct
		"other side, 10, 6, 2",
		"double centre, 3, 1.5, 0"
		})
	public void testGetFlippedOffset(String label, int len, double centre, int result) {
		StatsAndMathsWithSymmetry samws = new StatsAndMathsWithSymmetry();
		assertEquals(label+" failed", result, samws.getFlippedOffsetInNewDataset(len, centre));
	}
	
	@Test
	@Parameters({
		"begin, 10 , 0, 20",
		"end, 10 , 10, 20",
		"before, 10 , -1, 22",
		"after, 10 , 11, 22",
		"middle, 10 , 5, 10",
		"side, 10 , 4, 12",
		"double centre, 3, 1.5, 3"
		})
	public void testGetNewLength(String label, int len, double centre, int result) {
		StatsAndMathsWithSymmetry samws = new StatsAndMathsWithSymmetry();
		assertEquals(label+" failed", result, samws.getNewLength(len, centre));
	}
	
	@Test
	public void testgetUpDown() {
		StatsAndMathsWithSymmetry samws = new StatsAndMathsWithSymmetry();
		DatasetWithCentre dwc = samws.new DatasetWithCentre(0.5, 1.5, 
						new IntegerDataset(new int[] {
				0, 1, 
				2, 0, 
				1, 3 
						}, 3, 2));
		DatasetWithCentre upDown = samws.getUpDown(dwc);
		assertEquals("x center", 0.5, upDown.x, 0.001);
		assertEquals("y center", 1.5, upDown.y, 0.001);
		assertArrayEquals("data", new int[] {3,2}, upDown.dataset.getShape());
		assertArrayEquals("data", new int[] {
				1, 4,
				4, 0, 
				1, 4
											}, ((IntegerDataset) upDown.dataset).getData());
	}
	
	@Test
	public void testgetLR() {
		StatsAndMathsWithSymmetry samws = new StatsAndMathsWithSymmetry();
		DatasetWithCentre dwc = samws.new DatasetWithCentre(0.5, 1.5, 
						new IntegerDataset(new int[] {
				0, 1, 
				2, 0, 
				1, 3 
						}, 3, 2));
		DatasetWithCentre leftRight = samws.getLeftRight(dwc);
		assertEquals("x center", 1.5, leftRight.x, 0.001);
		assertEquals("y center", 1.5, leftRight.y, 0.001);
		assertArrayEquals("data", new int[] {3,3}, leftRight.dataset.getShape());
		assertArrayEquals("data", new int[] {
				1, 0, 1,
				0, 4, 0,
				3, 2, 3
											}, ((IntegerDataset) leftRight.dataset).getData());
	}
	
	
	@Test
	public void testgetLongLR() {
		StatsAndMathsWithSymmetry samws = new StatsAndMathsWithSymmetry();
		DatasetWithCentre dwc = samws.new DatasetWithCentre(3.5, 0.5, 
						new IntegerDataset(new int[] {
				7, 1, 0, 4, 3 
						}, 1, 5));
		DatasetWithCentre leftRight = samws.getLeftRight(dwc);
		assertEquals("x center", 3.5, leftRight.x, 0.001);
		assertEquals("y center", 0.5, leftRight.y, 0.001);
		assertArrayEquals("data", new int[] {1,7}, leftRight.dataset.getShape());
		assertArrayEquals("data", new int[] {
				7, 1, 3, 8, 3, 1, 7
											}, ((IntegerDataset) leftRight.dataset).getData());
	}
	
	@Test
	public void testgetLongUD() {
		StatsAndMathsWithSymmetry samws = new StatsAndMathsWithSymmetry();
		DatasetWithCentre dwc = samws.new DatasetWithCentre(0.5, 3.5, 
						new IntegerDataset(new int[] {
				7, 1, 0, 4, 3 
						}, 5, 1));
		DatasetWithCentre upDown = samws.getUpDown(dwc);
		assertEquals("x center", 0.5, upDown.x, 0.001);
		assertEquals("y center", 3.5, upDown.y, 0.001);
		assertArrayEquals("data", new int[] {7, 1}, upDown.dataset.getShape());
		assertArrayEquals("data", new int[] {
				7, 1, 3, 8, 3, 1, 7
											}, ((IntegerDataset) upDown.dataset).getData());
	}
}