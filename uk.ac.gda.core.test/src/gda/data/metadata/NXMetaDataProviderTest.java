/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.data.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.MockFactory;
import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.ConcurrentScan;

/** Note requires HDF plugin on LD_LIBRARY_PATH if not run as Plug-in test/Tycho */
class NXMetaDataProviderTest {

	private Random rand;
	private ScannableMotionUnits bsx;
	private Scannable testScn;
	private Scannable mscnIn3Ex0;
	private Scannable mscnIn3Ex2;
	private Scannable mscnIn0Ex2;
	private ScannableGroup scnGroup;

	private Map<String, String> formattingMap;
	private List<MetaDataUserSuppliedItem> userSuppliedItems;

	@BeforeEach
	public void setUp() throws DeviceException, FactoryException {
		ServiceHolder.getNexusDataWriterConfiguration().setMetadataScannables(new HashSet<>());
		this.rand = new Random();
		this.formattingMap = new HashMap<>();
		this.userSuppliedItems = new ArrayList<>();

		formattingMap.put("preamble", ""); // empty string
		formattingMap.put("lsNextItemSeparator", " "); // single space
		formattingMap.put("llMidConnector", "=");
		formattingMap.put("llNextItemSeparator", " "); // single space
		formattingMap.put("llUnitsSeparator", ""); // empty string
		formattingMap.put("llArrayOpen", "[");
		formattingMap.put("llArrayClose", "]");
		formattingMap.put("llArrayItemSeparator", ", "); // single coma followed by single space
		formattingMap.put("llFloatArrayFormat", "%5.3f");
		formattingMap.put("llIntArrayFormat", "%d");

		bsx = MockFactory.createMockScannableMotionUnits("bsx");
		when(bsx.getUserUnits()).thenReturn("mm");
		when(bsx.getAttribute(ScannableMotionUnits.USERUNITS)).thenReturn("eV");
		when(bsx.getPosition()).thenReturn(6.0);

		testScn = MockFactory.createMockScannable("testScn" // String name
				, new String[] { "testScn_In1" } // String[] inputNames
				, new String[] {} // String[] extraNames
				, new String[] { "%1.1f" } // String[] outputFormat
				, 0 // int level
				, 1331.013 // Object position
				);

		when(testScn.getAttribute(ScannableMotionUnits.USERUNITS)).thenReturn("GeV");

		String name = "mscnIn3Ex0";
		mscnIn3Ex0 = MockFactory.createMockScannable(name // String name
				, new String[] { name + "_in1", name + "_in2", name + "_in3" } // String[] inputNames
				, new String[] {} // String[] extraNames
				, new String[] { "%1.1f", "%1.1f", "%1.1f" } // String[] outputFormat
				, 1 // int level
				, new Double[] { 1.1, 1.2, 1.3 }); // Object position

		name = "mscnIn3Ex2";
		mscnIn3Ex2 = MockFactory.createMockScannable(name // String name
				, new String[] { name + "_in1", name + "_in2", name + "_in3" } // String[] inputNames
				, new String[] { name + "_ex1", name + "_ex2" } // String[] extraNames
				, new String[] { "%1.1f", "%1.1f", "%1.1f", "%1.1f", "%1.1f" } // String[] outputFormat
				, 1 // int level
				, new Double[] { 1.1, 1.2, 1.3, 3.1, 3.2 }); // Object position

		name = "mscnIn0Ex2";
		mscnIn0Ex2 = MockFactory.createMockScannable(name // String name
				, new String[] {} // String[] inputNames
				, new String[] { name + "_ex1", name + "_ex2" } // String[] extraNames
				, new String[] { "%1.1f", "%1.1f" } // String[] outputFormat
				, 1 // int level
				, new Double[] { 3.1, 3.2 }); // Object position

		scnGroup = new ScannableGroup("scnGroup", new Scannable[] { bsx, mscnIn3Ex2 });
	}

	@AfterEach
	public void tearDown() {
		LocalProperties.set("gda.nexus.metadata.provider.name", "");
		// Remove factories from Finder so they do not affect other tests
		Finder.removeAllFactories();
	}

	private void populateNXMetaDataProvider(NXMetaDataProvider metaDataProvider, int numEntries, String entryKeyRoot, String entryValueRoot) {
		if (numEntries > 1) {
			for (int i = 0; i < numEntries; i++) {
				metaDataProvider.add(entryKeyRoot + "_" + i, entryValueRoot + "_" + i);
			}
		} else {
			metaDataProvider.add(entryKeyRoot, entryValueRoot);
		}
	}

	private String generateExpectedUserSupplied(List<MetaDataUserSuppliedItem> userSuppliedItems, boolean isWithValues) {
		String listNextItemSeparator = null;
		String expected = "";
		expected += formattingMap.get("preamble");
		Object value = null;
		String units = null;
		for (MetaDataUserSuppliedItem item : userSuppliedItems) {
			expected += item.getKey();
			if (isWithValues) {
				expected += formattingMap.get("llMidConnector");
				value = item.getValue();
				if (value != null) {
					expected += value.toString();
				}

				units = item.getUnits();
				if (units != null) {
					expected += formattingMap.get("llUnitsSeparator");
					expected += units;
				}
			}

			if (isWithValues) {
				expected += formattingMap.get("llNextItemSeparator");
				listNextItemSeparator = "llNextItemSeparator";
			} else {
				expected += formattingMap.get("lsNextItemSeparator");
				listNextItemSeparator = "lsNextItemSeparator";
			}
		}

		int expectedLen = expected.length();
		int listNextItemSeparatorLen = formattingMap.get(listNextItemSeparator).length();
		if (expectedLen >= listNextItemSeparatorLen) {
			expected = expected.substring(0, expectedLen - listNextItemSeparatorLen);
		}
		return expected;
	}

	@Test
	void testInScan1() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testInScan1", true);

		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		populateNXMetaDataProvider(metaDataProvider, 4, "key", "value");
		metaDataProvider.add("pi", 3.14159);
		metaDataProvider.add("Catch", 22, "JH");

		double[] dblArray = new double[] { 3, 1, 4, 159 };
		metaDataProvider.add("myDoubleArray", dblArray);

		int[] intArray = new int[] { 3, 1, 4, 159 };
		metaDataProvider.add("myIntArray", intArray);

		metaDataProvider.setName("metashop");

		DummyScannable s = new DummyScannable("s");
		s.configure();

		Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(metaDataProvider);
		// Finder.addFactory(factory);

		Scannable smplScn1 = TestHelpers.createTestScannable("mySimpleScannable_In1Ex0", 13., new String[] {},
				new String[] { "mySimpleScannable_In1Ex0_input1" }, 0, new String[] { "%1.1f" },
				new String[] { "\u212B" }); // Angstrom

		factory.addFindable(smplScn1);
		metaDataProvider.add(smplScn1);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mySimpleScannable_In1Ex0", smplScn1);

		Scannable scnIn2Ex1 = TestHelpers.createTestScannable("mySimpleScannable_In2Ex1", new double[] { 1.11, 1.21,
				3.13 }, new String[] { "mySimpleScannable_In2Ex1_extra1" }, new String[] {
				"mySimpleScannable_In2Ex1_input1", "mySimpleScannable_In2Ex1_input2" }, 1, new String[] { "%1.1f",
				"%1.2f", "%3.1f" }, null);

		factory.addFindable(scnIn2Ex1);
		metaDataProvider.add(scnIn2Ex1);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mySimpleScannable_In2Ex1", scnIn2Ex1);

		Scannable scnIn1Ex2 = TestHelpers
				.createTestScannable("mySimpleScannable_In1Ex2", new double[] { 1.11, 3.13, 3.23 }, new String[] {
						"mySimpleScannable_In1Ex2_extra1", "mySimpleScannable_In1Ex2_extra2" },
						new String[] { "mySimpleScannable_In1Ex2_input1" }, 1,
						new String[] { "%1.1f", "%3.1f", "%3.2f" }, null);

		factory.addFindable(scnIn1Ex2);
		metaDataProvider.add(scnIn1Ex2);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mySimpleScannable_In1Ex2", scnIn1Ex2);

		Scannable scnIn1Ex1U = TestHelpers.createTestScannable("mySimpleScannable_In1Ex1U", new int[] { 101, 303 },
				new String[] { "mySimpleScannable_In1Ex1U_extra1" },
				new String[] { "mySimpleScannable_In1Ex1U_input1" }, 1, new String[] { "%i", "%i" }, new String[] {
						"eV", "keV" });

		factory.addFindable(scnIn1Ex1U);
		metaDataProvider.add(scnIn1Ex1U);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mySimpleScannable_In1Ex1U", scnIn1Ex1U);

		String angstrom = "\u212B";
		Scannable ss101 = TestHelpers.createTestScannable("SimpleScannable101" // name
				, 101.2 // position
				, new String[] {} // extraNames
				, new String[] { "SimpleScannable101" } // inputNames
				, 0 // level
				, new String[] { "%f" } // outputFormat
				, new String[] { angstrom } // units
				);
		Scannable ss102 = TestHelpers.createTestScannable("SimpleScannable102", 102.1, new String[] {},
				new String[] { "SimpleScannable102" }, 0, new String[] { "%f" }, new String[] { "eV" });
		Scannable ss103 = TestHelpers.createTestScannable("SimpleScannable103", 103.3, new String[] {},
				new String[] { "SimpleScannable103" }, 0, new String[] { "%f" }, new String[] { "mA" });
		ScannableGroup group = new ScannableGroup("myScannableGroup", new Scannable[] { ss101, ss102, ss103, bsx,
				testScn });
		metaDataProvider.add(group);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("myScannableGroup", group);

		metaDataProvider.add(bsx);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("bsx", bsx);
		metaDataProvider.add(testScn);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("testScn", testScn);

		Finder.addFactory(factory);

		// setup property to be read by NexusDataWriter
		LocalProperties.set("gda.nexus.metadata.provider.name", metaDataProvider.getName());

		ConcurrentScan conScan = new ConcurrentScan(new Object[] { s, 0, 10, 1});
		conScan.runScan();

	}

	@Test
	void testAdd() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAdd", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		populateNXMetaDataProvider(metaDataProvider, 4, "key", "value");

		int randomIntForTestKey = rand.nextInt(181081);
		String randomPostfixForTestKey = Integer.toString(randomIntForTestKey);
		String addedKey = "last_added_key" + "_" + randomPostfixForTestKey;
		String addedValue = "value_mapped_to_" + addedKey;

		// test before adding
		assertThat(metaDataProvider.containsKey(addedKey), is(false));

		// add
		metaDataProvider.add(addedKey, addedValue);

		// test after adding
		assertThat(metaDataProvider.containsKey(addedKey), is(true));

		// String valueMappedToAddedKey = metaDataProvider.get(addedKey).toString();
		Pair<?, ?> valueWithUnitsMappedToAddedKey = (Pair<?, ?>) metaDataProvider.get(addedKey);
		assertThat(valueWithUnitsMappedToAddedKey.getFirst().toString(), is(equalTo(addedValue)));
	}

	@Test
	void testRemove() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testRemove", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		populateNXMetaDataProvider(metaDataProvider, 4, "key", "value");

		int randomIntForTestKey = rand.nextInt(181081);
		String randomPostfixForTestKey = Integer.toString(randomIntForTestKey);
		String removedKey = "removed_key" + "_" + randomPostfixForTestKey;
		String removedValue = "value_mapped_to_" + removedKey;

		// first add
		metaDataProvider.add(removedKey, removedValue);
		assertThat(metaDataProvider.containsKey(removedKey), is(true));

		// then remove
		metaDataProvider.remove(removedKey);

		// test
		assertThat(metaDataProvider.containsKey(removedKey), is(false));
	}

	@Test
	void testAddForUserSuppliedSingleIntWithoutUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleIntWithoutUnitsAgainstList",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		int userSuppliedValue = 22;

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, null));

		for (MetaDataUserSuppliedItem item : userSuppliedItems) {
			metaDataProvider.add(item);
		}

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleIntWithoutUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedSingleIntWithoutUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		int userSuppliedValue = 22;

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, null));

		// list
		boolean withValues = true;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += Integer.toString(userSuppliedValue);

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleIntWithUnitsAgainstList() throws Exception {
		TestHelpers
				.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleIntWithUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		int userSuppliedValue = 22;
		String userSuppliedUnits = "JH";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));

		String xxx = generateExpectedUserSupplied(userSuppliedItems, false);

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected = xxx;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleIntWithUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleIntWithUnitsAgainstLongList",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		int userSuppliedValue = 22;
		String userSuppliedUnits = "JH";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));
		userSuppliedItems.add(new MetaDataUserSuppliedItem("Number", 10, "Downing Street"));

		metaDataProvider.add("Number", 10, "Downing Street");

		String xxx = generateExpectedUserSupplied(userSuppliedItems, true);

		// list
		boolean withValues = true;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += Integer.toString(userSuppliedValue);
		expected += formattingMap.get("llUnitsSeparator");
		expected += userSuppliedUnits;
		expected = xxx;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	// UserSuppliedSingleDouble
	@Test
	void testAddForUserSuppliedSingleDoubleWithoutUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedSingleDoubleWithoutUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		double userSuppliedValue = 22.0;

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleDoubleWithoutUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedSingleDoubleWithoutUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		double userSuppliedValue = 22.0;

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		// list
		boolean withValues = true;

		String defaultFormatForDouble = "%5.3f";

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += String.format(defaultFormatForDouble, userSuppliedValue);

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleDoubleWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleDoubleWithUnitsAgainstList",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		double userSuppliedValue = 22.0;
		String userSuppliedUnits = "JH";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));

		String xxx = generateExpectedUserSupplied(userSuppliedItems, false);

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected = xxx;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleDoubleWithUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedSingleDoubleWithUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		double userSuppliedValue = 22.0;
		String userSuppliedUnits = "JH";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		// list
		boolean withValues = true;

		String defaultFormatForDouble = "%5.3f";

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += String.format(defaultFormatForDouble, userSuppliedValue);
		expected += formattingMap.get("llUnitsSeparator");
		expected += userSuppliedUnits;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	// UserSuppliedString
	@Test
	void testAddForUserSuppliedSingleStringWithoutUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedSingleStringWithoutUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myKey";
		String userSuppliedValue = "myValue";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, null));

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleStringWithoutUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedSingleStringWithoutUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myKey";
		String userSuppliedValue = "myValue";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, null));

		// list
		boolean withValues = true;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += userSuppliedValue;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleStringWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleStringWithUnitsAgainstList",
				true);

		// test particulars
		boolean isWithValues = false;

		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myKey";
		String userSuppliedValue = "myValue";
		String userSuppliedUnits = "myUnits";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));

		String xxx = generateExpectedUserSupplied(userSuppliedItems, isWithValues);

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected = xxx;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleStringWithUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedSingleStringWithUnitsAgainstLongList", true);

		// test particulars
		boolean isWithValues = true;

		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myKey";
		String userSuppliedValue = "myValue";
		String userSuppliedUnits = "myUnits";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));

		String xxx = generateExpectedUserSupplied(userSuppliedItems, isWithValues);

		// list
		boolean withValues = true;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += userSuppliedValue;
		expected += formattingMap.get("llUnitsSeparator");
		expected += userSuppliedUnits;
		expected = xxx;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	// UserSuppliedIntArray
	@Test
	void testAddForUserSuppliedIntArrayWithoutUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedIntArrayWithoutUnitsAgainstList",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myIntArray";
		int[] userSuppliedValue = { 3, 1, 4, 159 };

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedIntArrayWithoutUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedIntArrayWithoutUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myIntArray";
		int[] userSuppliedValue = { 3, 1, 4, 159 };

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		// list
		boolean withValues = true;

		String llArrayItemSeparatorUsed = formattingMap.get("llArrayItemSeparator");

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += formattingMap.get("llArrayOpen");
		int arrLen = userSuppliedValue.length;
		for (int i = 0; i < arrLen; i++) {
			expected += Integer.toString(userSuppliedValue[i]) + formattingMap.get("llArrayItemSeparator");
		}
		int substringLen = expected.length() - llArrayItemSeparatorUsed.length();
		if (substringLen >= 0) {
			expected = expected.substring(0, substringLen);
		}
		expected += formattingMap.get("llArrayClose");

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedIntArrayWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedIntArrayWithUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myIntArray1";
		int[] userSuppliedValue = { 3, 1, 4, 159 };
		String userSuppliedUnits = "eV";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		// list
		boolean withValues = false;

		String preamble = ""; // empty string
		String lsNextItemSeparator = " "; // white space
		String llMidConnector = "=";
		String llNextItemSeparator = " "; // white space

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;

		metaDataProvider.preamble = preamble;
		metaDataProvider.lsNextItemSeparator = lsNextItemSeparator;
		metaDataProvider.llMidConnector = llMidConnector;
		metaDataProvider.llNextItemSeparator = llNextItemSeparator;
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedIntArrayWithUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedIntArrayWithUnitsAgainstLongList",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myIntArray";
		int[] userSuppliedValue = { 3, 1, 4, 159 };
		String userSuppliedUnits = "eV";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		// list
		boolean withValues = true;

		String preamble = ""; // empty string
		String lsNextItemSeparator = " "; // white space
		String llMidConnector = "=";
		String llNextItemSeparator = " "; // white space
		String llUnitsSeparator = ""; // empty string

		String llArrayItemSeparatorUsed = formattingMap.get("llArrayItemSeparator");
		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += llMidConnector;
		expected += formattingMap.get("llArrayOpen");
		int arrLen = userSuppliedValue.length;
		for (int i = 0; i < arrLen; i++) {
			expected += Integer.toString(userSuppliedValue[i]) + formattingMap.get("llArrayItemSeparator");
		}
		int substringLen = expected.length() - llArrayItemSeparatorUsed.length();
		if (substringLen >= 0) {
			expected = expected.substring(0, substringLen);
		}
		expected += formattingMap.get("llArrayClose");
		expected += llUnitsSeparator;
		expected += userSuppliedUnits;

		metaDataProvider.preamble = preamble;
		metaDataProvider.lsNextItemSeparator = lsNextItemSeparator;
		metaDataProvider.llMidConnector = llMidConnector;
		metaDataProvider.llNextItemSeparator = llNextItemSeparator;
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	// UserSuppliedDoubleArray
	@Test
	void testAddForUserSuppliedDoubleArrayWithoutUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedDoubleArrayWithoutUnitsAgainstList",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myDoubleArray";
		double[] userSuppliedValue = { 3.0, 1.0, 4.0, 159.0 };

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedDoubleArrayWithoutUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedDoubleArrayWithoutUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myDoubleArray";
		double[] userSuppliedValue = { 3.0, 1.0, 4.0, 159.0 };

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		// list
		boolean withValues = true;

		String llArrayItemSeparatorUsed = formattingMap.get("llArrayItemSeparator");

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += formattingMap.get("llArrayOpen");
		int arrLen = userSuppliedValue.length;
		for (int i = 0; i < arrLen; i++) {
			Double dbl = userSuppliedValue[i];
			// expected += String.format("%5.3f", Double.toString(userSuppliedValue[i]));
			expected += String.format("%5.3f", (Object) dbl);
			expected += formattingMap.get("llArrayItemSeparator");
		}
		int substringLen = expected.length() - llArrayItemSeparatorUsed.length();
		if (substringLen >= 0) {
			expected = expected.substring(0, substringLen);
		}
		expected += formattingMap.get("llArrayClose");

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		metaDataProvider.modifyFormattingMap(formattingMap);
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedDoubleArrayWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedDoubleArrayWithUnitsAgainstList",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myDoubleArray";
		double[] userSuppliedValue = { 3.0, 1.0, 4.0, 159.0 };
		String userSuppliedUnits = "eV";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedDoubleArrayWithUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class,
				"testAddForUserSuppliedDoubleArrayWithUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myDoubleArray";
		double[] userSuppliedValue = { 3.0, 1.0, 4.0, 159.0 };
		String userSuppliedUnits = "eV";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));

		for (MetaDataUserSuppliedItem item : userSuppliedItems)
			metaDataProvider.add(item);

		// list
		boolean withValues = true;

		String llUnitsSeparator = "";

		String llArrayItemSeparatorUsed = formattingMap.get("llArrayItemSeparator");

		String expected = formattingMap.get("preamble");
		expected += userSuppliedKey;
		expected += formattingMap.get("llMidConnector");
		expected += formattingMap.get("llArrayOpen");
		int arrLen = userSuppliedValue.length;
		for (int i = 0; i < arrLen; i++) {
			Double dbl = userSuppliedValue[i];
			expected += String.format("%5.3f", (Object) dbl);
			expected += formattingMap.get("llArrayItemSeparator");
		}
		int substringLen = expected.length() - llArrayItemSeparatorUsed.length();
		if (substringLen >= 0)
			expected = expected.substring(0, substringLen);
		expected += formattingMap.get("llArrayClose");
		expected += llUnitsSeparator;
		expected += userSuppliedUnits;

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	// ScannableGenerated
	@Test
	void testAddForScannableGeneratedWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = bsx;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("bsx", bsx);

		// list
		boolean withValues = false;

		String expected = formattingMap.get("preamble");
		expected += scannable.getName();

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_testScan() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_testScan",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = testScn;
		scannable = mscnIn3Ex0;
		scannable = mscnIn3Ex2;
		scannable = mscnIn0Ex2;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn0Ex2", mscnIn0Ex2);

		// list
		boolean withValues = true;

		String expected = "mscnIn0Ex2.mscnIn0Ex2_ex1=3.1 mscnIn0Ex2.mscnIn0Ex2_ex2=3.2";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn3Ex0() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn3Ex0",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = mscnIn3Ex0;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn3Ex0", mscnIn3Ex0);

		// list
		boolean withValues = false;

		String expected = "mscnIn3Ex0.mscnIn3Ex0_in1 mscnIn3Ex0.mscnIn3Ex0_in2 mscnIn3Ex0.mscnIn3Ex0_in3";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn3Ex0() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn3Ex0",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = mscnIn3Ex0;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn3Ex0", mscnIn3Ex0);

		// list
		boolean withValues = true;

		String expected = "mscnIn3Ex0.mscnIn3Ex0_in1=1.1 mscnIn3Ex0.mscnIn3Ex0_in2=1.2 mscnIn3Ex0.mscnIn3Ex0_in3=1.3";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn3Ex2() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn3Ex2",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = mscnIn3Ex2;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn3Ex2", mscnIn3Ex2);

		// list
		boolean withValues = false;

		String expected = "mscnIn3Ex2.mscnIn3Ex2_ex1 mscnIn3Ex2.mscnIn3Ex2_ex2 mscnIn3Ex2.mscnIn3Ex2_in1 mscnIn3Ex2.mscnIn3Ex2_in2 mscnIn3Ex2.mscnIn3Ex2_in3";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn3Ex2() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn3Ex2",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = mscnIn3Ex2;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn3Ex2", mscnIn3Ex2);

		// list
		boolean withValues = true;

		String expected = "mscnIn3Ex2.mscnIn3Ex2_ex1=3.1 mscnIn3Ex2.mscnIn3Ex2_ex2=3.2 mscnIn3Ex2.mscnIn3Ex2_in1=1.1 mscnIn3Ex2.mscnIn3Ex2_in2=1.2 mscnIn3Ex2.mscnIn3Ex2_in3=1.3";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn0Ex2() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn0Ex2",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = mscnIn0Ex2;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn0Ex2", mscnIn0Ex2);

		// list
		boolean withValues = false;

		String expected = "mscnIn0Ex2.mscnIn0Ex2_ex1 mscnIn0Ex2.mscnIn0Ex2_ex2";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn0Ex2() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn0Ex2",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = mscnIn0Ex2;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn0Ex2", mscnIn0Ex2);

		// list
		boolean withValues = true;

		String expected = "mscnIn0Ex2.mscnIn0Ex2_ex1=3.1 mscnIn0Ex2.mscnIn0Ex2_ex2=3.2";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstList_scnGroup() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstList_scnGroup",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = scnGroup;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("scnGroup", scnGroup);

		// list
		boolean withValues = false;

		String expected = "scnGroup.bsx scnGroup.mscnIn3Ex2.mscnIn3Ex2_ex1 scnGroup.mscnIn3Ex2.mscnIn3Ex2_ex2 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in1 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in2 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in3";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_scnGroup() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_scnGroup",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		Scannable scannable = scnGroup;
		metaDataProvider.add(scannable);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("scnGroup", scnGroup);

		// list
		boolean withValues = true;

		String expected = "scnGroup.bsx=6mm scnGroup.mscnIn3Ex2.mscnIn3Ex2_ex1=3.1 scnGroup.mscnIn3Ex2.mscnIn3Ex2_ex2=3.2 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in1=1.1 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in2=1.2 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in3=1.3";

		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
		String actual = metaDataProvider.list(withValues);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testDynamicMetadataScannables() throws Exception {
		Scannable scn1 = MockFactory.createMockScannable("scn1",
				new String[] { "in_1" },
				new String[] {},
				new String[] { "%1.1f" },
				0,
				0.0);
		Scannable scn2 = MockFactory.createMockScannable("scn2",
				new String[] { "in_1" },
				new String[] {},
				new String[] { "%1.1f" },
				0,
				0.0);
		HashSet<String> initial = new HashSet<String>();
		initial.add(scn1.getName());
		ServiceHolder.getNexusDataWriterConfiguration().setMetadataScannables(initial);

		NXMetaDataProvider provider = new NXMetaDataProvider();
		provider.add(scn2);
		assertThat(ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables(), containsInAnyOrder("scn1", "scn2"));

		provider.clearDynamicScannableMetadata();
		assertThat(ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables(), containsInAnyOrder("scn1"));
	}
}
