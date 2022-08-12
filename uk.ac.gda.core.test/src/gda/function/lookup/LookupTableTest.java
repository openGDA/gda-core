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

package gda.function.lookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import java.util.stream.IntStream;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.junit.jupiter.api.Test;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.util.QuantityFactory;

public class LookupTableTest {
	private static final double DELTA = 1e-6;
	private static final String TEST_FILE_DIRECTORY = "testfiles/gda/function/lookup/";
	private static final String TEST_FILE = "test.lookup";
	private static final String HEADING_CHANGE_FILE = "heading_change.lookup";
	private static final String ALTERNATE_FILE = "alternate.lookup";
	private static final String UNITLESS = "unitless.lookup";
	private static final String EMPTY = "empty.lookup";
	private static final String MISSING = "missing.lookup";
	LookupTable lt;

	private LookupTable getFile(String filename) throws Exception {
		LookupTable file = new LookupTable();
		file.setDirectory(TEST_FILE_DIRECTORY);
		file.setFilename(filename);
		file.configure();
		return file;
	}

	@Test
	public void getUnits() throws Exception {
		lt = getFile(TEST_FILE);
		assertThat(lt.lookupUnit("energy"), is(unit("keV")));
		assertThat(lt.lookupUnit("foo"), is(unit("mm")));
		assertThat(lt.lookupUnit("bar"), is(unit("deg")));
		assertThat(lt.lookupUnit("baz"), is(unit("")));
	}

	@Test
	public void getUnitStrings() throws Exception {
		lt = getFile(TEST_FILE);
		assertThat(lt.lookupUnitString("energy"), is("keV"));
		assertThat(lt.lookupUnitString("foo"), is("mm"));
		assertThat(lt.lookupUnitString("bar"), is("deg"));
		assertThat(lt.lookupUnitString("baz"), is("one"));
	}

	@Test
	public void numberOfRows() throws Exception {
		lt = getFile(TEST_FILE);
		assertThat(lt.getNumberOfRows(), is(20));
	}

	@Test
	public void configureCanBeCalledTwice() throws Exception {
		lt = getFile(TEST_FILE);
		lt.configure();
	}

	@Test
	public void getDecimalPlaces() throws Exception {
		lt = getFile(TEST_FILE);
		assertThat(lt.lookupDecimalPlaces("energy"), is(2));
		assertThat(lt.lookupDecimalPlaces("foo"), is(0));
		assertThat(lt.lookupDecimalPlaces("bar"), is(4));
		assertThat(lt.lookupDecimalPlaces("baz"), is(3));
	}

	@Test
	public void getLookupValues() throws Exception {
		lt = getFile(TEST_FILE);
		assertThat(lt.getLookupKeys(), is(IntStream.range(0, 20).mapToDouble(i -> (double)i/100).toArray()));
	}

	@Test(expected = FactoryException.class)
	public void missingFileThrowsFactoryException() throws Exception {
		lt = getFile(MISSING);
	}

	@Test
	public void lookupValues() throws Exception {
		lt = getFile(TEST_FILE);
		assertThat(lt.lookupValue("0.12", "foo"), is(closeTo(15.0, DELTA)));
		assertThat(lt.lookupValue("0.12", "bar"), is(closeTo(0.7954, DELTA)));
		assertThat(lt.lookupValue("0.12", "baz"), is(closeTo(11.463, DELTA)));
	}

	@Test(expected = DeviceException.class)
	public void lookupValueBeforeConfiguring() throws Exception {
		lt = new LookupTable();
		lt.lookupValue(0.12, "helloWorld");
	}

	@Test(expected = IllegalArgumentException.class)
	public void lookupMissingScannable() throws Exception {
		lt = getFile(TEST_FILE);
		lt.lookupValue(0.12, "missing");
	}

	@Test(expected = IllegalStateException.class)
	public void lookupMissingValue() throws Exception {
		lt = getFile(TEST_FILE);
		lt.lookupValue(0.5, "foo");
	}


	private static Unit<? extends Quantity<?>> unit(String unit) {
		return QuantityFactory.createUnitFromString(unit);
	}

	@Test
	public void reloadNewFile() throws Exception {
		lt = getFile(TEST_FILE);
		assertThat(lt.lookupValue(0.18, "foo"), is(26.0));
		lt.setFilename(ALTERNATE_FILE);
		lt.reload();
		assertThat(lt.lookupValue(0.18, "foo"), is(40.0));
	}

	@Test
	public void unitlessTableUsesDimensionlessUnit() throws Exception {
		lt = getFile(UNITLESS);
		assertThat(lt.lookupUnit("foo"), is(unit("")));
		assertThat(lt.lookupUnitString("foo"), is("one"));
	}

	@Test
	public void useAlternateHeadings() throws Exception {
		lt = new LookupTable();
		lt.setDirectory(TEST_FILE_DIRECTORY);
		lt.setFilename(HEADING_CHANGE_FILE);
		lt.setColumnHead("scannables");
		lt.setColumnUnit("units");
		lt.configure();
		assertThat(lt.lookupValue(0.04, "foo"), is(11.0));
		assertThat(lt.lookupUnitString("foo"), is("mm"));
	}

	@Test(expected = FactoryException.class)
	public void emptyFileThrowsException() throws Exception {
		lt = getFile(EMPTY);
	}
}
