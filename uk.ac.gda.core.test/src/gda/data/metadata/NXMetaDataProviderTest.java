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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
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

	private static final String[] NO_FIELDS = new String[0];
	private static final Double[] NO_POSITIONS = new Double[0];

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

		testScn = MockFactory.createMockScannable("testScn",
				new String[] { "testScn_In1" }, NO_FIELDS, new String[] { "%1.1f" }, 0, 1331.013); // 1 input field, no extra fields

		when(testScn.getAttribute(ScannableMotionUnits.USERUNITS)).thenReturn("GeV");

		mscnIn3Ex0 = createMockScannable("mscnIn3Ex0", true, false); // 3 input fields, no extra fields
		mscnIn3Ex2 = createMockScannable("mscnIn3Ex2", true, true); // 3 input fields, 2 extra fields
		mscnIn0Ex2 = createMockScannable("mscnIn0Ex2", false, true); // no input fields, 2 extra fields

		scnGroup = new ScannableGroup("scnGroup", new Scannable[] { bsx, mscnIn3Ex2 });
	}

	private Scannable createMockScannable(String name, boolean hasInputNames, boolean hasExtraNames) throws DeviceException {
		final String[] inputNames = hasInputNames ? createFieldNames(name + "_in", 3) : NO_FIELDS;
		final String[] extraNames = hasExtraNames ? createFieldNames(name + "_ex", 2) : NO_FIELDS;
		final String[] outputFormat = Collections.nCopies((hasInputNames ? 3 : 0) + (hasExtraNames ? 2 : 0), "%1.1f").toArray(String[]::new);
		final Double[] inputPos = hasInputNames ? new Double[] { 1.1, 1.2, 1.3 } : NO_POSITIONS;
		final Double[] position = hasExtraNames ? ArrayUtils.addAll(inputPos, new Double[] { 3.1, 3.2 }) : inputPos;

		return MockFactory.createMockScannable(name, inputNames, extraNames, outputFormat, 1, position);
	}

	private String[] createFieldNames(String prefix, int numFields) {
		return numFields == 0 ? NO_FIELDS : IntStream.rangeClosed(1, numFields).mapToObj(i -> prefix + i).toArray(String[]::new);
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
		StringBuilder expected = new StringBuilder();
		expected.append(formattingMap.get("preamble"));
		Object value = null;
		String units = null;
		for (MetaDataUserSuppliedItem item : userSuppliedItems) {
			expected.append(item.getKey());
			if (isWithValues) {
				expected.append(formattingMap.get("llMidConnector"));
				value = item.getValue();
				if (value != null) {
					expected.append(value);
				}

				units = item.getUnits();
				if (units != null) {
					expected.append(formattingMap.get("llUnitsSeparator"));
					expected.append(units);
				}
			}

			if (isWithValues) {
				expected.append(formattingMap.get("llNextItemSeparator"));
				listNextItemSeparator = "llNextItemSeparator";
			} else {
				expected.append(formattingMap.get("lsNextItemSeparator"));
				listNextItemSeparator = "lsNextItemSeparator";
			}
		}

		int listNextItemSeparatorLen = formattingMap.get(listNextItemSeparator).length();
		if (expected.length() >= listNextItemSeparatorLen) {
			expected.setLength(expected.length() - listNextItemSeparatorLen);
		}
		return expected.toString();
	}

	private void configureFormattingFromFormattingMap(NXMetaDataProvider metaDataProvider) {
		metaDataProvider.preamble = formattingMap.get("preamble");
		metaDataProvider.lsNextItemSeparator = formattingMap.get("lsNextItemSeparator");
		metaDataProvider.llMidConnector = formattingMap.get("llMidConnector");
		metaDataProvider.llNextItemSeparator = formattingMap.get("llNextItemSeparator");
		metaDataProvider.llUnitsSeparator = formattingMap.get("llUnitsSeparator");
	}

	private void configureCustomFormatting(NXMetaDataProvider metaDataProvider, String preamble, String lsNextItemSeparator,
			String llMidConnector, String llNextItemSeparator) {
		metaDataProvider.preamble = preamble;
		metaDataProvider.lsNextItemSeparator = lsNextItemSeparator;
		metaDataProvider.llMidConnector = llMidConnector;
		metaDataProvider.llNextItemSeparator = llNextItemSeparator;
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
		Scannable ss101 = TestHelpers.createTestScannable("SimpleScannable101", 101.2, NO_FIELDS,
				new String[] { "SimpleScannable101" }, 0, new String[] { "%f" }, new String[] { angstrom });
		Scannable ss102 = TestHelpers.createTestScannable("SimpleScannable102", 102.1, NO_FIELDS,
				new String[] { "SimpleScannable102" }, 0, new String[] { "%f" }, new String[] { "eV" });
		Scannable ss103 = TestHelpers.createTestScannable("SimpleScannable103", 103.3, NO_FIELDS,
				new String[] { "SimpleScannable103" }, 0, new String[] { "%f" }, new String[] { "mA" });
		ScannableGroup group = new ScannableGroup("myScannableGroup", new Scannable[] { ss101, ss102, ss103, bsx, testScn });
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
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleIntWithoutUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		int userSuppliedValue = 22;

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, null));

		for (MetaDataUserSuppliedItem item : userSuppliedItems) {
			metaDataProvider.add(item);
		}

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = formattingMap.get("preamble") + userSuppliedKey;
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleIntWithoutUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleIntWithoutUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		int userSuppliedValue = 22;

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, null));

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = formattingMap.get("preamble") + userSuppliedKey +
				formattingMap.get("llMidConnector") + Integer.toString(userSuppliedValue);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleIntWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleIntWithUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		int userSuppliedValue = 22;
		String userSuppliedUnits = "JH";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = generateExpectedUserSupplied(userSuppliedItems, false);
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleIntWithUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleIntWithUnitsAgainstLongList", true);
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

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = generateExpectedUserSupplied(userSuppliedItems, true);
		String actual = metaDataProvider.list(true);

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
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = formattingMap.get("preamble") + userSuppliedKey;
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleDoubleWithoutUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleDoubleWithoutUnitsAgainstLongList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		double userSuppliedValue = 22.0;

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = formattingMap.get("preamble") + userSuppliedKey +
				formattingMap.get("llMidConnector") + String.format("%5.3f", userSuppliedValue);

		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleDoubleWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleDoubleWithUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "Catch";
		double userSuppliedValue = 22.0;
		String userSuppliedUnits = "JH";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = generateExpectedUserSupplied(userSuppliedItems, false);
		String actual = metaDataProvider.list(false);

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
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = formattingMap.get("preamble") +
				userSuppliedKey +
				formattingMap.get("llMidConnector") +
				String.format("%5.3f", userSuppliedValue) +
				formattingMap.get("llUnitsSeparator") +
				userSuppliedUnits;
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	// UserSuppliedString
	@Test
	void testAddForUserSuppliedSingleStringWithoutUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleStringWithoutUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myKey";
		String userSuppliedValue = "myValue";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, null));

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = formattingMap.get("preamble") + userSuppliedKey;
		String actual = metaDataProvider.list(false);

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
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = formattingMap.get("preamble") + userSuppliedKey +
				formattingMap.get("llMidConnector") + userSuppliedValue;
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleStringWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleStringWithUnitsAgainstList", true);

		// test particulars
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myKey";
		String userSuppliedValue = "myValue";
		String userSuppliedUnits = "myUnits";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = generateExpectedUserSupplied(userSuppliedItems, false);
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForUserSuppliedSingleStringWithUnitsAgainstLongList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForUserSuppliedSingleStringWithUnitsAgainstLongList", true);

		// test particulars
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		String userSuppliedKey = "myKey";
		String userSuppliedValue = "myValue";
		String userSuppliedUnits = "myUnits";

		// add
		metaDataProvider.add(userSuppliedKey, userSuppliedValue, userSuppliedUnits);

		userSuppliedItems.clear();
		userSuppliedItems.add(new MetaDataUserSuppliedItem(userSuppliedKey, userSuppliedValue, userSuppliedUnits));


		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = generateExpectedUserSupplied(userSuppliedItems, true);
		String actual = metaDataProvider.list(true);

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
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = formattingMap.get("preamble") + userSuppliedKey;
		String actual = metaDataProvider.list(false);

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
		String llArrayItemSeparatorUsed = formattingMap.get("llArrayItemSeparator");

		StringBuilder expected = new StringBuilder();
		expected.append(formattingMap.get("preamble"));
		expected.append(userSuppliedKey);
		expected.append(formattingMap.get("llMidConnector"));
		expected.append(formattingMap.get("llArrayOpen"));
		int arrLen = userSuppliedValue.length;
		for (int i = 0; i < arrLen; i++) {
			expected.append(Integer.toString(userSuppliedValue[i]));
			expected.append(formattingMap.get("llArrayItemSeparator"));
		}
		int substringLen = expected.length() - llArrayItemSeparatorUsed.length();
		if (substringLen >= 0) {
			expected.setLength(substringLen);
		}
		expected.append(formattingMap.get("llArrayClose"));

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected.toString())));
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
		String preamble = ""; // empty string
		String lsNextItemSeparator = " "; // white space
		String llMidConnector = "=";
		String llNextItemSeparator = " "; // white space

		String expected = formattingMap.get("preamble") + userSuppliedKey;

		configureCustomFormatting(metaDataProvider, preamble, lsNextItemSeparator, llMidConnector, llNextItemSeparator);
		String actual = metaDataProvider.list(false);

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
		String preamble = ""; // empty string
		String lsNextItemSeparator = " "; // white space
		String llMidConnector = "=";
		String llNextItemSeparator = " "; // white space
		String llUnitsSeparator = ""; // empty string

		String llArrayItemSeparatorUsed = formattingMap.get("llArrayItemSeparator");
		StringBuilder expected = new StringBuilder();
		expected.append(formattingMap.get("preamble"));
		expected.append(userSuppliedKey);
		expected.append(llMidConnector);
		expected.append(formattingMap.get("llArrayOpen"));
		int arrLen = userSuppliedValue.length;
		for (int i = 0; i < arrLen; i++) {
			expected.append(Integer.toString(userSuppliedValue[i]) + formattingMap.get("llArrayItemSeparator"));
		}
		int substringLen = expected.length() - llArrayItemSeparatorUsed.length();
		if (substringLen >= 0) {
			expected.setLength(substringLen);
		}
		expected.append(formattingMap.get("llArrayClose"));
		expected.append(llUnitsSeparator);
		expected.append(userSuppliedUnits);

		configureCustomFormatting(metaDataProvider, preamble, lsNextItemSeparator, llMidConnector, llNextItemSeparator);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected.toString())));
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
		String expected = formattingMap.get("preamble") + userSuppliedKey;

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(false);

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
		String llArrayItemSeparatorUsed = formattingMap.get("llArrayItemSeparator");

		StringBuilder expected = new StringBuilder();
		expected.append(formattingMap.get("preamble"));
		expected.append(userSuppliedKey);
		expected.append(formattingMap.get("llMidConnector"));
		expected.append(formattingMap.get("llArrayOpen"));
		int arrLen = userSuppliedValue.length;
		for (int i = 0; i < arrLen; i++) {
			Double dbl = userSuppliedValue[i];
			// expected += String.format("%5.3f", Double.toString(userSuppliedValue[i]));
			expected.append(String.format("%5.3f", (Object) dbl));
			expected.append(formattingMap.get("llArrayItemSeparator"));
		}
		int substringLen = expected.length() - llArrayItemSeparatorUsed.length();
		if (substringLen >= 0) {
			expected.setLength(substringLen);
		}
		expected.append(formattingMap.get("llArrayClose"));

		configureFormattingFromFormattingMap(metaDataProvider);
		metaDataProvider.modifyFormattingMap(formattingMap);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected.toString())));
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
		String expected = formattingMap.get("preamble") + userSuppliedKey;

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(false);

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
		String llUnitsSeparator = "";
		String llArrayItemSeparatorUsed = formattingMap.get("llArrayItemSeparator");

		final StringBuilder expected = new StringBuilder();
		expected.append(formattingMap.get("preamble"));
		expected.append(userSuppliedKey);
		expected.append(formattingMap.get("llMidConnector"));
		expected.append(formattingMap.get("llArrayOpen"));
		int arrLen = userSuppliedValue.length;
		for (int i = 0; i < arrLen; i++) {
			Double dbl = userSuppliedValue[i];
			expected.append(String.format("%5.3f", (Object) dbl));
			expected.append(formattingMap.get("llArrayItemSeparator"));
		}
		int substringLen = expected.length() - llArrayItemSeparatorUsed.length();
		if (substringLen >= 0)
			expected.setLength(substringLen);
		expected.append(formattingMap.get("llArrayClose"));
		expected.append(llUnitsSeparator);
		expected.append(userSuppliedUnits);

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected.toString())));
	}

	// ScannableGenerated
	@Test
	void testAddForScannableGeneratedWithUnitsAgainstList() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithUnitsAgainstList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(bsx);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("bsx", bsx);

		// list
		String expected = formattingMap.get("preamble") + "bsx";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_testScan() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_testScan", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(mscnIn0Ex2);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn0Ex2", mscnIn0Ex2);

		// list
		String expected = "mscnIn0Ex2.mscnIn0Ex2_ex1=3.1 mscnIn0Ex2.mscnIn0Ex2_ex2=3.2";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn3Ex0() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn3Ex0", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(mscnIn3Ex0);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn3Ex0", mscnIn3Ex0);

		// list
		String expected = "mscnIn3Ex0.mscnIn3Ex0_in1 mscnIn3Ex0.mscnIn3Ex0_in2 mscnIn3Ex0.mscnIn3Ex0_in3";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn3Ex0() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn3Ex0", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(mscnIn3Ex0);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn3Ex0", mscnIn3Ex0);

		// list
		String expected = "mscnIn3Ex0.mscnIn3Ex0_in1=1.1 mscnIn3Ex0.mscnIn3Ex0_in2=1.2 mscnIn3Ex0.mscnIn3Ex0_in3=1.3";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn3Ex2() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn3Ex2", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(mscnIn3Ex2);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn3Ex2", mscnIn3Ex2);

		// list
		String expected = "mscnIn3Ex2.mscnIn3Ex2_ex1 mscnIn3Ex2.mscnIn3Ex2_ex2 mscnIn3Ex2.mscnIn3Ex2_in1 mscnIn3Ex2.mscnIn3Ex2_in2 mscnIn3Ex2.mscnIn3Ex2_in3";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn3Ex2() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn3Ex2",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(mscnIn3Ex2);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn3Ex2", mscnIn3Ex2);

		// list
		String expected = "mscnIn3Ex2.mscnIn3Ex2_ex1=3.1 mscnIn3Ex2.mscnIn3Ex2_ex2=3.2 mscnIn3Ex2.mscnIn3Ex2_in1=1.1 mscnIn3Ex2.mscnIn3Ex2_in2=1.2 mscnIn3Ex2.mscnIn3Ex2_in3=1.3";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn0Ex2() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstList_mscnIn0Ex2",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(mscnIn0Ex2);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn0Ex2", mscnIn0Ex2);

		// list
		String expected = "mscnIn0Ex2.mscnIn0Ex2_ex1 mscnIn0Ex2.mscnIn0Ex2_ex2";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn0Ex2() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_mscnIn0Ex2", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(mscnIn0Ex2);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("mscnIn0Ex2", mscnIn0Ex2);

		// list
		String expected = "mscnIn0Ex2.mscnIn0Ex2_ex1=3.1 mscnIn0Ex2.mscnIn0Ex2_ex2=3.2";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstList_scnGroup() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstList_scnGroup", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(scnGroup);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("scnGroup", scnGroup);

		// list
		String expected = "scnGroup.bsx scnGroup.mscnIn3Ex2.mscnIn3Ex2_ex1 scnGroup.mscnIn3Ex2.mscnIn3Ex2_ex2 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in1 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in2 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in3";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(false);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testAddForScannableGeneratedWithoutUnitsAgainstLongList_scnGroup() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAddForScannableGeneratedWithoutUnitsAgainstLongList_scnGroup",
				true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(scnGroup);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("scnGroup", scnGroup);

		// list
		String expected = "scnGroup.bsx=6mm scnGroup.mscnIn3Ex2.mscnIn3Ex2_ex1=3.1 scnGroup.mscnIn3Ex2.mscnIn3Ex2_ex2=3.2 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in1=1.1 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in2=1.2 scnGroup.mscnIn3Ex2.mscnIn3Ex2_in3=1.3";

		configureFormattingFromFormattingMap(metaDataProvider);
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testDynamicMetadataScannables() throws Exception {
		Scannable scn1 = MockFactory.createMockScannable("scn1", new String[] { "in_1" }, NO_FIELDS, new String[] { "%1.1f" }, 0, 0.0);
		Scannable scn2 = MockFactory.createMockScannable("scn2", new String[] { "in_1" }, NO_FIELDS, new String[] { "%1.1f" }, 0, 0.0);
		ServiceHolder.getNexusDataWriterConfiguration().setMetadataScannables(new HashSet<>(Set.of(scn1.getName())));

		NXMetaDataProvider provider = new NXMetaDataProvider();
		provider.add(scn2);
		assertThat(ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables(), containsInAnyOrder("scn1", "scn2"));

		provider.clearDynamicScannableMetadata();
		assertThat(ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables(), containsInAnyOrder("scn1"));
	}

	@Test
	void testSetMetaTexts() {
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		metaDataProvider.setMetaTexts(Map.of("key1", "value1", "key2", "value2"));

		configureFormattingFromFormattingMap(metaDataProvider);
		String expected = "key1=value1 key2=value2";
		String actual = metaDataProvider.list(true);

		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testRemoveString() {
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add("pi", 3.14159);
		metaDataProvider.add("Catch", 22, "JH");

		assertThat(metaDataProvider.keySet(), containsInAnyOrder("pi", "Catch"));

		// remove
		metaDataProvider.remove("pi");
		assertThat(metaDataProvider.keySet(), containsInAnyOrder("Catch"));

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = "Catch=22JH";
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

	@Test
	void testRemoveScannable() throws Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testRemoveScannable", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();

		// add
		metaDataProvider.add(bsx);
		metaDataProvider.add("Catch", 22, "JH");
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("bsx", bsx);
		assertThat(metaDataProvider.keySet(), contains("Catch"));
		assertThat(metaDataProvider.getMetaScannables(), contains(bsx));

		// remove
		metaDataProvider.remove(bsx);
		assertThat(metaDataProvider.keySet(), containsInAnyOrder("Catch"));
		assertThat(metaDataProvider.getMetaScannables(), is(empty()));

		// list
		configureFormattingFromFormattingMap(metaDataProvider);

		String expected = "Catch=22JH";
		String actual = metaDataProvider.list(true);

		// test
		assertThat(actual, is(equalTo(expected)));
	}

}
