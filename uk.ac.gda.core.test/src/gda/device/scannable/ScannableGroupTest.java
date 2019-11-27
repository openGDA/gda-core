/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import gda.MockFactory;
import gda.TestHelpers;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Finder;
import junit.framework.TestCase;

/**
 * Tests {@link ScannableGroup}.
 */
public class ScannableGroupTest extends TestCase {

	protected static final List<Scannable> EMPTY_SCANNABLE_LIST = Collections.emptyList();

	protected Scannable s1;
	protected Scannable s2;
	protected IScannableGroup sg;

	@Override
	protected void setUp() throws Exception {
		s1 = new DummyScannable("s1");
		s2 = new DummyScannable("s2");
		sg = new ScannableGroup();

		final Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(s1);
		factory.addFindable(s2);

		Finder.getInstance().removeAllFactories();
		Finder.getInstance().addFactory(factory);
	}

	@Override
	protected void tearDown() throws Exception {
		// Remove factories from Finder so they do not affect other tests
		Finder.getInstance().removeAllFactories();
	}

	/**
	 * Tests that a newly-instantiated {@link ScannableGroup} initially has no group members.
	 *
	 * @throws DeviceException
	 */
	@Test
	public void testNewlyInstantiatedScannableGroup() throws DeviceException {
		System.out.println(sg.getGroupMembersAsArray());
		assertEquals(EMPTY_SCANNABLE_LIST, sg.getGroupMembers());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testScanHooks() throws Exception {
		final Scannable s1 = MockFactory.createMockScannable("s1", new String[] { "input1", "input2" },
				new String[] { "extra1" }, new String[] { "%5.5g", "%5.5g", "%1d" }, 5, new Double[] { 10., 20. });
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[] { "input1" }, new String[0],
				new String[] { "%5.5g" }, 5, 10.);
		final Scannable s3 = MockFactory.createMockScannable("s2", new String[0], new String[] { "extra1" },
				new String[] { "%5.5g" }, 5, null);

		sg = new ScannableGroup("group", new Scannable[] { s1, s2, s3 });

		sg.atPointEnd();
		sg.atPointStart();
		sg.atScanLineEnd();
		sg.atScanEnd();
		sg.atLevelMoveStart();
		sg.atCommandFailure();
		sg.atScanStart();
		sg.atScanLineStart();
		sg.stop();

		for (Scannable scannable : new Scannable[] { s1, s2, s3 }) {
			verify(scannable, times(1)).atPointEnd();
			verify(scannable, times(1)).atPointStart();
			verify(scannable, times(1)).atScanLineEnd();
			verify(scannable, times(1)).atScanEnd();
			verify(scannable, times(1)).atLevelMoveStart();
			verify(scannable, times(1)).atCommandFailure();
			verify(scannable, times(1)).atScanStart();
			verify(scannable, times(1)).atScanLineStart();
			verify(scannable, times(1)).stop();
		}

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testInputAndExtraOrder() throws Exception {

		final Scannable s1 = MockFactory.createMockScannable("s1", new String[] { "input1", "input2" },
				new String[] { "extra1" }, new String[] { "%1.1g", "%2.2g", "%3.3f" }, 5,
				new Double[] { 10., 20., 3. });
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[] { "input3" }, new String[0],
				new String[] { "%4.4g" }, 5, 10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] { "extra2" },
				new String[] { "%5.5g" }, 5, .901);

		sg = new ScannableGroup("group", new Scannable[] { s1, s2, s3 });

		final String[] inputNames = sg.getInputNames();
		final String[] extraNames = sg.getExtraNames();
		final String[] outputFormat = sg.getOutputFormat();
		final Object[] position = (Object[]) sg.getPosition();

		assertArrayEquals(new String[] { "input1", "input2", "input3" }, inputNames);
		assertArrayEquals(new String[] { "extra1", "extra2" }, extraNames);
		// GDA-5794 from here
		assertArrayEquals(new Object[] { 10., 20., 10., 3., .901 }, position);
		assertArrayEquals(new String[] { "%1.1g", "%2.2g", "%4.4g", "%3.3f", "%5.5g" }, outputFormat);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testInputAndExtraOrderWithZIEScannable() throws Exception {
		final Scannable zie1 = MockFactory.createMockZieScannable("zie1", 5); // arbitrary level
		final Scannable zie2 = MockFactory.createMockZieScannable("zie2", 5); // arbitrary level
		final Scannable s1 = MockFactory.createMockScannable("s1", new String[] { "input1", "input2" },
				new String[] { "extra1" }, new String[] { "%1.1g", "%2.2g", "%3.3f" }, 5,
				new Double[] { 10., 20., 3. });
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[] { "input3" }, new String[0],
				new String[] { "%4.4g" }, 5, 10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] { "extra2" },
				new String[] { "%5.5g" }, 5, .901);

		sg = new ScannableGroup("group", new Scannable[] { s1, zie1, s2, zie2, s3 });

		final String[] inputNames = sg.getInputNames();
		final String[] extraNames = sg.getExtraNames();
		final String[] outputFormat = sg.getOutputFormat();
		final Object[] position = (Object[]) sg.getPosition();

		assertArrayEquals(new String[] { "input1", "input2", "input3" }, inputNames);
		assertArrayEquals(new String[] { "extra1", "extra2" }, extraNames);
		// GDA-5794 from here
		assertArrayEquals(new Object[] { 10., 20., 10., 3., .901 }, position);
		assertArrayEquals(new String[] { "%1.1g", "%2.2g", "%4.4g", "%3.3f", "%5.5g" }, outputFormat);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testIsPositionValid() throws Exception {
		final Scannable s1 = MockFactory.createMockScannable("s1", new String[] { "input1", "input2" },
				new String[] { "extra1" }, new String[] { "%5.5g", "%5.5g", "%1d" }, 5, new Double[] { 10., 20. });
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[] { "input1" }, new String[0],
				new String[] { "%5.5g" }, 5, 10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] { "extra1" },
				new String[] { "%5.5g" }, 5, null);

		sg = new ScannableGroup("group", new Scannable[] { s1, s2, s3 });
		assertTrue(sg.checkPositionValid(new Double[] { 1., 2., 3. }) == null);
		assertTrue(sg.checkPositionValid(new Double[] { 1., 2., 3., 4. }) != null);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testArrays() throws Exception {

		final Scannable s1 = MockFactory.createMockScannable("s1", new String[] { "input1", "input2" },
				new String[] { "extra1" }, new String[] { "%5.5g", "%5.5g", "%1d" }, 5, new Double[] { 10., 20. });
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[] { "input1" }, new String[0],
				new String[] { "%5.5g" }, 5, 10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] { "extra1" },
				new String[] { "%5.5g" }, 5, null);

		sg = new ScannableGroup("group", new Scannable[] { s1, s2, s3 });
		assertEquals(3, sg.getInputNames().length);
		assertEquals(2, sg.getExtraNames().length);
		assertEquals(5, sg.getOutputFormat().length);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testMoveTo() throws Exception {
		final Scannable zie1 = MockFactory.createMockZieScannable("zie1", 5); // arbitrary level
		final Scannable s1 = MockFactory.createMockScannable("s1", new String[] { "input1", "input2" },
				new String[] { "extra1" }, new String[] { "%1.1g", "%2.2g", "%3.3f" }, 5,
				new Double[] { 10., 20., 3. });
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[] { "input3" }, new String[0],
				new String[] { "%4.4g" }, 5, 10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] { "extra2" },
				new String[] { "%5.5g" }, 5, .901);

		sg = new ScannableGroup("group", new Scannable[] { s1, zie1, s2, s3 });

		sg.asynchronousMoveTo(new Object[] { 10., 20., 10. });
		verify(s1).asynchronousMoveTo(new Object[] { 10., 20. });
		verify(s2).asynchronousMoveTo(10.);
		verify(zie1, never()).asynchronousMoveTo(any(Object.class));
		verify(s3).asynchronousMoveTo(new Object[] {});
		Object[] position = (Object[]) sg.getPosition();

		assertArrayEquals(new Object[] { 10., 20., 10., 3., .901 }, position);
	}

	@Test
	public void testToFormattedStringNoErrors() throws FactoryException {
		final Scannable s1 = new DummyScannable("s1");
		final Scannable s2 = new DummyScannable("s2");
		sg = new ScannableGroup("sg1", new Scannable[] { s1, s2 });
		final String expectedResult = "sg1 ::\n  s1 : 0.0000 (-1.7977e+308:1.7977e+308)\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

		final String result = sg.toFormattedString();
		assertEquals(expectedResult, result);
	}

	@Test
	public void testToFormattedStringWithError() throws FactoryException {
		// In the event of an exception in one scannable, the ScannableGroup should show the value as dashes
		// and continue with the remaining scannables.
		s1 = new DummyScannable("s1") {
			@Override
			public String toFormattedString() {
				throw new RuntimeException("failure in toFormattedString()");
			}
		};
		s2 = new DummyScannable("s2");
		sg = new ScannableGroup("sg1", new Scannable[] { s1, s2 });
		final String expectedResult = "sg1 ::\n  s1 : UNAVAILABLE\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

		final String result = sg.toFormattedString();
		assertEquals(expectedResult, result);
	}

	protected static <T> T[] arrayOf(@SuppressWarnings("unchecked") T... items) {
		return items;
	}

}
