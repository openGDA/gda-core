/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.junit.Before;
import org.junit.Test;

public class SymmetryUtilTest {

	private Dataset testData;
	private Dataset testNegativeData;

	@Before
	public void setup() {
		testData = DatasetFactory.createFromObject(IntegerDataset.class,
				new int[] {
						4, 7, 2,
						1, 6, 10,
						8, 0, 11,
						5, 9, 3},
				4, 3);
		testNegativeData = DatasetFactory.createFromObject(IntegerDataset.class,
				new int[] {
						4,  7,  2,  1,
						1, -1, 10,  3,
						8,  0, 11, -1,
						5, -1,  3,  5,
						4,  1,  7,  3},
				5, 4);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testVerticalOverlapTooHigh() {
		SymmetryUtil.reflectVertical(testData, 3);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testVerticalOverlapTooLow() {
		SymmetryUtil.reflectVertical(testData, -1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testHorizontalOverlapTooHigh() {
		SymmetryUtil.reflectHorizontal(testData, 4);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testHorizontalOverlapTooLow() {
		SymmetryUtil.reflectHorizontal(testData, -1);
	}

	@Test
	public void testVerticalReflection() {
		Dataset vert = SymmetryUtil.reflectVertical(testData, 2);
		assertThat(vert.getShape(), is(equalTo(new int[] {4, 5})));
		Dataset expected = DatasetFactory.createFromObject(IntegerDataset.class,
				new int[] {
						4, 7, 2, 7, 4,
						1, 6, 10, 6, 1,
						8, 0, 11, 0, 8,
						5, 9, 3, 9, 5},
				4, 5);
		assertThat(vert, is(equalTo(expected)));
	}

	@Test
	public void testHorizontalReflection() {
		Dataset hori = SymmetryUtil.reflectHorizontal(testData, 2);
		assertThat(hori.getShape(), is(equalTo(new int[] {5, 3})));
		Dataset expected = DatasetFactory.createFromObject(IntegerDataset.class,
				new int[] {
						4, 7, 2,
						3, 7, 6,
						8, 0, 11,
						3, 7, 6,
						4, 7, 2},
				5, 3);
		assertThat(hori, is(equalTo(expected)));
	}

	@Test
	public void testFourFoldReflection() {
		Dataset both = SymmetryUtil.reflectBoth(testData, 2, 2);
		assertThat(both.getShape(), is(equalTo(new int[] {5, 5})));
		Dataset expected = DatasetFactory.createFromObject(IntegerDataset.class,
				new int[] {
						4, 7, 2, 7, 4,
						3, 7, 6, 7, 3,
						8, 0, 11, 0, 8,
						3, 7, 6, 7, 3,
						4, 7, 2, 7, 4},
				5, 5);
		assertThat(both, is(equalTo(expected)));
	}

	@Test
	public void testVerticalNegativeData() {
		Dataset vert = SymmetryUtil.reflectVertical(testNegativeData, 2);
		assertThat(vert.getShape(), is(equalTo(new int[] {5, 5})));
		Dataset expected = DatasetFactory.createFromObject(IntegerDataset.class,
				new int[] {
						4,  4,  2,  4,  4,
						1,  3, 10,  3,  1,
						8,  0, 11,  0,  8,
						5,  5,  3,  5,  5,
						4,  2,  7,  2,  4},
				5, 5);
		assertThat(vert, is(equalTo(expected)));
	}

	@Test
	public void testHorizontalNegativeData() {
		Dataset hori = SymmetryUtil.reflectHorizontal(testNegativeData, 3);
		assertThat(hori.getShape(), is(equalTo(new int[] {7, 4})));
		Dataset expected = DatasetFactory.createFromObject(IntegerDataset.class,
				new int[] {
						4,  7,  2,  1,
						1, -1, 10,  3,
						6,  0,  9,  3, // combined
						5, -1,  3,  5, // pivot,
						6,  0,  9,  3, // combined
						1, -1, 10,  3,
						4,  7,  2,  1,
						},
				7, 4);
		assertThat(hori, is(equalTo(expected)));
	}

	@Test
	public void testFourFoldNegativeData() {
		Dataset both = SymmetryUtil.reflectBoth(testNegativeData, 1, 3);
		assertThat(both.getShape(), is(equalTo(new int[] {7, 5})));
		Dataset expected = DatasetFactory.createFromObject(IntegerDataset.class,
				new int[] {
						1,  3,  7,  3,  1,
						3,  5, -1,  5,  3,
						3,  7,  0,  7,  3,
						5,  4, -1,  4,  5,
						3,  7,  0,  7,  3,
						3,  5, -1,  5,  3,
						1,  3,  7,  3,  1},
				7, 5);
		assertThat(both, is(equalTo(expected)));
	}
}
