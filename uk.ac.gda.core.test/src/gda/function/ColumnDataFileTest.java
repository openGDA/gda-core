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

package gda.function;

import static gda.function.lookup.AbstractColumnFile.LOOKUP_TABLE_DIRECTORY_PROPERTY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;

public class ColumnDataFileTest {
	private static final String TEST_FILE_DIRECTORY = "testfiles/gda/function/";
	private static final String TEST_FILE = "test.lookup";
	private static final String UNITLESS = "unitless.lookup";
	private static final String EMPTY = "empty.lookup";
	private static final String MISSING = "missing.lookup";
	ColumnDataFile cdf;

	private ColumnDataFile getFile(String filename) throws Exception {
		ColumnDataFile file = new ColumnDataFile();
		file.setFilename(Paths.get(TEST_FILE_DIRECTORY, filename).toRealPath().toString(), true);
		file.configure();
		return file;
	}

	@Test
	public void numberOfXValues() throws Exception {
		assertThat("Incorrect number of rows", getFile(TEST_FILE).getNumberOfXValues(), is(2));
		assertThat("Incorrect number of rows", getFile(UNITLESS).getNumberOfXValues(), is(3));
	}

	@Test
	public void columnUnits() throws Exception {
		cdf = getFile(TEST_FILE);
		assertThat(cdf.getColumnUnits(0), is("mm"));
		assertThat(cdf.getColumnUnits(1), is(""));
		assertThat(cdf.getColumnUnits(2), is("rad"));
		assertThat(cdf.getColumnUnits(3), is(""));
	}

	@Test
	public void unitlessColumnUnits() throws Exception {
		cdf = getFile(UNITLESS);
		assertThat(cdf.getColumnUnits(0), is(""));
		assertThat(cdf.getColumnUnits(1), is(""));
		assertThat(cdf.getColumnUnits(2), is(""));
		assertThat(cdf.getColumnUnits(3), is(""));
	}

	@Test
	public void columnDecimalPlaces() throws Exception {
		cdf = getFile(TEST_FILE);
		assertThat(cdf.getColumnDecimalPlaces(0), is(1));
		assertThat(cdf.getColumnDecimalPlaces(1), is(2));
		assertThat(cdf.getColumnDecimalPlaces(2), is(3));
		assertThat(cdf.getColumnDecimalPlaces(3), is(0));
	}

	@Test
	public void getColumns() throws Exception {
		cdf = getFile(TEST_FILE);
		assertThat(cdf.getColumn(0), is(new double[] {0.1, 0.2}));
		assertThat(cdf.getColumn(1), is(new double[] {3.33, 4.44}));
		assertThat(cdf.getColumn(2), is(new double[] {2.222, 5.555}));
		assertThat(cdf.getColumn(3), is(new double[] {1, 6}));
	}

	@Test
	public void useDefaultDirectory() throws FactoryException {
		LocalProperties.set(LOOKUP_TABLE_DIRECTORY_PROPERTY, new File("testfiles/gda/function").getAbsolutePath());
		cdf = new ColumnDataFile();
		cdf.setFilename(TEST_FILE);
		try {
			cdf.configure();
			assertThat(cdf.getNumberOfXValues(), is(2));
		} finally {
			LocalProperties.clearProperty(LOOKUP_TABLE_DIRECTORY_PROPERTY);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyFile() throws Exception {
		cdf = getFile(EMPTY);
		assertThat(cdf.getNumberOfXValues(), is(0));
	}

	@Test(expected = FactoryException.class)
	public void missingFile() throws FactoryException {
		cdf = new ColumnDataFile();
		cdf.setFilename(new File(TEST_FILE_DIRECTORY, MISSING).getAbsolutePath(), true);
		cdf.configure();
	}
}
