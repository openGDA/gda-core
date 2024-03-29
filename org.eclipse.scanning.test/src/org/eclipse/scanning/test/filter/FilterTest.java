/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.filter.Filter;
import org.eclipse.scanning.api.filter.IFilter;
import org.eclipse.scanning.api.filter.IFilterService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FilterTest {

	private static IScannableDeviceService sservice;
	private static IFilterService          fservice;

	@BeforeAll
	public static void getServices() {
		fservice = IFilterService.DEFAULT;
		sservice = new MockScannableConnector(null);
	}

	@BeforeEach
	public void parseSpring() throws Exception {
		fservice.clear();
		Filter filter = new Filter();
		filter.setName("org.eclipse.scanning.scannableFilter");
		filter.setExcludes(Arrays.asList("qvach", // Should not match anything
				"monitor1", // The later include will override this
				"a", "b", "c", "beam.*", "neXusScannable.*"));
		filter.setIncludes(Arrays.asList("monitor.*", "beamcurrent", "neXusScannable2",
				"neXusScannable", // Should not match anything
				"rubbish")); // Should not match anything
		filter.register();
	}

	@Test
	public void notNull() {
		assertNotNull(IFilterService.DEFAULT);
		assertNotNull(IFilterService.DEFAULT.getFilter("org.eclipse.scanning.scannableFilter"));
		assertEquals(7, IFilterService.DEFAULT.getFilter("org.eclipse.scanning.scannableFilter").getExcludes().size());
		assertEquals(5, IFilterService.DEFAULT.getFilter("org.eclipse.scanning.scannableFilter").getIncludes().size());
	}

	@Test
	public void znoFilter() throws ScanningException {
		assertEquals(sservice.getScannableNames(), fservice.filter("not.there", sservice.getScannableNames()));
	}

	@Test
	public void testFilterSpring() throws Exception {
		check();
	}

	@Test
	public void testFilterManual() throws Exception {

		fservice.clear();

		// Spring does this for us in "test_filters.xml"
		IFilter<String> filter = new Filter();
		filter.setName("org.eclipse.scanning.scannableFilter");
		filter.setExcludes(Arrays.asList("qvach", "monitor1", "a", "b", "c", "beam.*", "neXusScannable.*"));
		filter.setIncludes(Arrays.asList("monitor.*", "beamcurrent", "neXusScannable2", "neXusScannable", "rubbish"));
		fservice.register(filter);

		check();
	}

	@Test
	public void testDuplicatesNoSpring() {

		fservice.clear();

		IFilter<String> dfilter = new Filter();
		dfilter.setName("duplicates");
		dfilter.setIncludes(Arrays.asList("a"));
		dfilter.setExcludes(Arrays.asList("b"));
		fservice.register(dfilter);

		List<String> items = new ArrayList<>(Arrays.asList("a", "a", "b", "b"));
		assertEquals(Arrays.asList("a", "a"), fservice.filter("duplicates", items));

		items = new ArrayList<>(Arrays.asList("a", "b", "a", "b"));
		assertEquals(Arrays.asList("a", "a"), fservice.filter("duplicates", items));

		items = new ArrayList<>(Arrays.asList("b", "b", "1", "a", "a"));
		assertEquals(Arrays.asList("1", "a", "a"), fservice.filter("duplicates", items));

		items = new ArrayList<>(Arrays.asList("b", "b", "a", "a", "1"));
		assertEquals(Arrays.asList("a", "a", "1"), fservice.filter("duplicates", items));

	}

	@Test
	public void testDuplicatesExclude() {

		fservice.clear();

		IFilter<String> dfilter = new Filter();
		dfilter.setName("duplicates");
		dfilter.setIncludes(Arrays.asList("a"));
		dfilter.setExcludes(Arrays.asList("a", "b"));
		fservice.register(dfilter);

		List<String> items = new ArrayList<>(Arrays.asList("a", "a", "b", "b"));
		assertEquals(Arrays.asList("a", "a"), fservice.filter("duplicates", items));

		items = new ArrayList<>(Arrays.asList("a", "b", "a", "b"));
		assertEquals(Arrays.asList("a", "a"), fservice.filter("duplicates", items));

		items = new ArrayList<>(Arrays.asList("b", "b", "1", "a", "a"));
		assertEquals(Arrays.asList("1", "a", "a"), fservice.filter("duplicates", items));

		items = new ArrayList<>(Arrays.asList("b", "b", "a", "a", "1"));

		// Because we excluded it the result changes
		assertEquals(Arrays.asList("1", "a", "a"), fservice.filter("duplicates", items));

	}

	@Test
	@Timeout(value = 10000, unit = TimeUnit.MILLISECONDS)
	public void testDuplicatesLarge() {

		fservice.clear();

		IFilter<String> dfilter = new Filter();
		dfilter.setName("duplicates");

		int copies = 250;
		List<String> as = Collections.nCopies(copies, "a");
		dfilter.setIncludes(as);
		List<String> bs = Collections.nCopies(copies, "b");
		dfilter.setExcludes(bs);
		fservice.register(dfilter);

		List<String> items = new ArrayList<>();
		items.addAll(as);
		items.addAll(bs);
		assertEquals(as, fservice.filter("duplicates", items));

		items = new ArrayList<>();
		for (int i = 0; i < copies; i++) {
		    items.add(as.get(i));
		    items.add(bs.get(i));
		}
		assertEquals(as, fservice.filter("duplicates", items));

		items = new ArrayList<>();
		items.addAll(bs);
		items.add("1");
		items.addAll(as);
		assertEquals("1", fservice.filter("duplicates", items).get(0));
		assertEquals("a", fservice.filter("duplicates", items).get(1));
		assertEquals("a", fservice.filter("duplicates", items).get(2));

		items = new ArrayList<>();
		items.addAll(bs);
		items.addAll(as);
		items.add("1");
		List<String> filtered = fservice.filter("duplicates", items);
		assertEquals("1", filtered.get(filtered.size()-1));
		assertEquals("a", filtered.get(filtered.size()-2));
		assertEquals("a", filtered.get(filtered.size()-3));

	}

	@Test
	public void testNoIncludes() throws ScanningException {
		fservice.clear();

		final IFilter<String> filter = new Filter();
		filter.setName("org.eclipse.scanning.scannableFilter");
		filter.setExcludes(Arrays.asList("qvach", "monitor1", "a", "b", "c", "beam.*", "neXusScannable.*"));
		fservice.register(filter);

		final List<String> names = sservice.getScannableNames();
		final List<String> filtered = fservice.filter("org.eclipse.scanning.scannableFilter", names);

		// Check for items not matched by the exclude filter
		assertTrue(filtered.contains("stage_x"));
		assertTrue(filtered.contains("stage_y"));
		assertTrue(filtered.contains("x"));
		assertTrue(filtered.contains("y"));
		assertTrue(filtered.contains("z"));
		assertTrue(filtered.contains("T0"));
		assertTrue(filtered.contains("T1"));

		// Things which should be excluded, or not there in the first place
		assertFalse(filtered.contains("qvach"));
		assertFalse(filtered.contains("monitor1"));
		assertFalse(filtered.contains("a"));
		assertFalse(filtered.contains("b"));
		assertFalse(filtered.contains("c"));
		assertFalse(filtered.contains("beamcurrent"));
		assertFalse(filtered.contains("rubbish"));
		assertFalse(filtered.contains("neXusScannable"));
		assertFalse(filtered.contains("neXusScannable1"));
		assertFalse(filtered.contains("neXusScannable3"));
		assertFalse(filtered.contains("neXusScannable4"));
	}

	@Test
	public void testNoExcludes() throws ScanningException {
		fservice.clear();

		final IFilter<String> filter = new Filter();
		filter.setName("org.eclipse.scanning.scannableFilter");
		filter.setIncludes(Arrays.asList("monitor.*", "beamcurrent", "neXusScannable2", "neXusScannable", "rubbish"));
		fservice.register(filter);

		final List<String> names = sservice.getScannableNames();
		final List<String> filtered = fservice.filter("org.eclipse.scanning.scannableFilter", names);

		// Nothing excluded, so the two lists should be the same (but we don't care about order)
		Collections.sort(names);
		Collections.sort(filtered);
		assertTrue(filtered.equals(names));
	}

	@Test
	public void testNoIncludesOrExcludes() throws ScanningException {
		fservice.clear();

		final IFilter<String> filter = new Filter();
		filter.setName("org.eclipse.scanning.scannableFilter");
		fservice.register(filter);

		final List<String> names = sservice.getScannableNames();
		final List<String> filtered = fservice.filter("org.eclipse.scanning.scannableFilter", names);

		// Nothing excluded, so the two lists should be the same (but we don't care about order)
		Collections.sort(names);
		Collections.sort(filtered);
		assertTrue(filtered.equals(names));
	}

	private void check() throws ScanningException {

		sservice.getScannable("aa"); // Create an aa scannable
		List<String> names    = sservice.getScannableNames();
		List<String> filtered = fservice.filter("org.eclipse.scanning.scannableFilter", names);

		// Stuff not matched should be there
		assertTrue(filtered.contains("stage_x"));
		assertTrue(filtered.contains("stage_y"));
		assertTrue(filtered.contains("x"));
		assertTrue(filtered.contains("y"));
		assertTrue(filtered.contains("z"));

		// Stuff that got included
		assertTrue(filtered.contains("monitor1"));
		assertTrue(filtered.contains("neXusScannable2"));
		assertTrue(filtered.contains("beamcurrent"));
		assertTrue(filtered.contains("aa")); // The one we created

		// Things which should surely not be around.
		assertFalse(filtered.contains("qvach"));
		assertFalse(filtered.contains("a"));
		assertFalse(filtered.contains("b"));
		assertFalse(filtered.contains("c"));
		assertFalse(filtered.contains("rubbish"));
		assertFalse(filtered.contains("neXusScannable"));
		assertFalse(filtered.contains("neXusScannable1"));
		assertFalse(filtered.contains("neXusScannable3"));
		assertFalse(filtered.contains("neXusScannable4"));
	}

}