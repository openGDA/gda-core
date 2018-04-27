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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import gda.MockFactory;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.factory.MapFactory;
import junit.framework.TestCase;

/**
 * Tests {@link ScannableGroup}.
 */
public class ScannableGroupTest extends TestCase {

	private static final String[] EMPTY_NAME_ARRAY = new String[0];

	private static final List<Scannable> EMPTY_SCANNABLE_LIST = Collections.emptyList();

	private Scannable s1;

	private Scannable s2;

	@Override
	protected void setUp() throws Exception {
		s1 = new DummyScannable("s1");
		s2 = new DummyScannable("s2");

		final Factory factory = new MapFactory("test", null);
		factory.addFindable(s1);
		factory.addFindable(s2);

		Finder.getInstance().removeAllFactories();
		Finder.getInstance().addFactory(factory);
	}

	/**
	 * Tests that a newly-instantiated {@link ScannableGroup} initially has no
	 * group members or group member names.
	 */
	@Test
	public void testNewlyInstantiatedScannableGroup() {
		final ScannableGroup sg = new ScannableGroup();
		assertEquals(EMPTY_SCANNABLE_LIST, sg.getGroupMembers());
		assertArrayEquals(EMPTY_NAME_ARRAY, sg.getGroupMemberNames());
	}

	/**
	 * Tests configuring a {@link ScannableGroup} using only group members.
	 *
	 * @throws Exception
	 */
	@Test
	public void testConfiguringScannableGroupUsingMembers() throws Exception {
		final ScannableGroup sg = new ScannableGroup();
		sg.setGroupMembers(asList(s1));

		assertFalse("Expected ScannableGroup to be unconfigured", sg.isConfigured());
		assertEquals(asList(s1), sg.getGroupMembers());
		assertArrayEquals(EMPTY_NAME_ARRAY, sg.getGroupMemberNames());

		sg.configure();

		assertTrue("Expected ScannableGroup to be configured", sg.isConfigured());
		assertEquals(asList(s1), sg.getGroupMembers());
		assertArrayEquals(arrayOf("s1"), sg.getGroupMemberNames());
	}

	/**
	 * Tests configuring a {@link ScannableGroup} using only group member names.
	 *
	 * @throws Exception
	 */
	@Test
	public void testConfiguringScannableGroupUsingMemberNames() throws Exception {
		final ScannableGroup sg = new ScannableGroup();
		sg.setGroupMemberNames(asList("s2"));

		assertFalse("Expected ScannableGroup to be unconfigured", sg.isConfigured());
		assertEquals(EMPTY_SCANNABLE_LIST, sg.getGroupMembers());
		assertArrayEquals(arrayOf("s2"), sg.getGroupMemberNames());

		sg.configure();

		assertTrue("Expected ScannableGroup to be configured", sg.isConfigured());
		assertEquals(asList(s2), sg.getGroupMembers());
		assertArrayEquals(arrayOf("s2"), sg.getGroupMemberNames());
	}

	/**
	 * Tests configuring a {@link ScannableGroup} using both members and member
	 * names.
	 *
	 * @throws Exception
	 */
	@Test
	public void testConfiguringScannableGroupUsingMembersAndNames() throws Exception {
		final ScannableGroup sg = new ScannableGroup();
		sg.setGroupMembers(asList(s1));
		sg.setGroupMemberNames(asList("s2"));

		assertFalse("Expected ScannableGroup to be unconfigured", sg.isConfigured());
		assertEquals(asList(s1), sg.getGroupMembers());
		assertArrayEquals(arrayOf("s2"), sg.getGroupMemberNames());

		sg.configure();

		assertTrue("Expected ScannableGroup to be configured", sg.isConfigured());
		assertEquals(asList(s1, s2), sg.getGroupMembers());
		assertArrayEquals(arrayOf("s1", "s2"), sg.getGroupMemberNames());
	}

	/**
	 * Tests adding a group member to a group after the group has been
	 * configured. Should update the group members and the group member names.
	 *
	 * @throws Exception
	 */
	@Test
	public void testAddingGroupMemberAfterConfiguration() throws Exception {
		final ScannableGroup sg = createAndCheckScannableGroupContaining(s1);
		sg.addGroupMember(s2);
		assertEquals(asList(s1, s2), sg.getGroupMembers());
		assertArrayEquals(arrayOf("s1", "s2"), sg.getGroupMemberNames());
	}

	/**
	 * Tests setting a group's members after it has been configured. Should
	 * update the group members and the group member names.
	 *
	 * @throws Exception
	 */
	@Test
	public void testSettingGroupMembersAfterConfiguration() throws Exception {
		final ScannableGroup sg = createAndCheckScannableGroupContaining(s1);
		sg.setGroupMembers(asList(s2));
		assertEquals(asList(s2), sg.getGroupMembers());
		assertArrayEquals(arrayOf("s2"), sg.getGroupMemberNames());
	}

	/**
	 * Tests adding a group member name to a group after the group has been
	 * configured. Should only update the group member names, not the group
	 * members.
	 *
	 * @throws Exception
	 */
	@Test
	public void testAddingGroupMemberNameAfterConfiguration() throws Exception {
		final ScannableGroup sg = createAndCheckScannableGroupContaining(s1);
		sg.addGroupMemberName("s2");
		assertEquals(asList(s1), sg.getGroupMembers());
		assertArrayEquals(arrayOf("s1", "s2"), sg.getGroupMemberNames());
	}

	/**
	 * Tests setting a group's member names after it has been configured.
	 * Should only update the group member names, not the group members.
	 *
	 * @throws Exception
	 */
	@Test
	public void testSettingGroupMemberNamesAfterConfiguration() throws Exception {
		final ScannableGroup sg = createAndCheckScannableGroupContaining(s1);
		sg.setGroupMemberNames(asList("s2"));
		assertEquals(asList(s1), sg.getGroupMembers());
		assertArrayEquals(arrayOf("s2"), sg.getGroupMemberNames());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testScanHooks() throws Exception {
		final Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"}, new String[] {"%5.5g","%5.5g","%1d"},5,new Double[]{10.,20.});
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input1"}, new String[0], new String[] {"%5.5g"},5,10.);
		final Scannable s3 = MockFactory.createMockScannable("s2", new String[0], new String[] {"extra1"}, new String[] {"%5.5g"},5,null);

		final ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1,s2,s3});

		group.atPointEnd();
		group.atPointStart();
		group.atScanLineEnd();
		group.atScanEnd();
		group.atLevelMoveStart();
		group.atCommandFailure();
		group.atScanStart();
		group.atScanLineStart();
		group.stop();


		for (Scannable scannable : new Scannable[] {s1,s2,s3}) {
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

		final Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"},
				new String[] {"%1.1g","%2.2g","%3.3f"},5,new Double[]{10.,20.,3.});
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input3"}, new String[0], new String[] {"%4.4g"},5,10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] {"extra2"}, new String[] {"%5.5g"},5,.901);

		final ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1,s2,s3});

		final String[] inputNames = group.getInputNames();
		final String[] extraNames = group.getExtraNames();
		final String[] outputFormat = group.getOutputFormat();
		final Object[] position = (Object[]) group.getPosition();

		assertArrayEquals(new String[] { "input1", "input2", "input3" }, inputNames);
		assertArrayEquals(new String[] { "extra1", "extra2" }, extraNames);
		// GDA-5794 from here
		assertArrayEquals(new Object[] {10., 20., 10., 3., .901}, position);
		assertArrayEquals(new String[] {"%1.1g", "%2.2g","%4.4g","%3.3f","%5.5g"}, outputFormat);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testInputAndExtraOrderWithZIEScannable() throws Exception {
		final Scannable zie1 = MockFactory.createMockZieScannable("zie1", 5);  // arbitrary level
		final Scannable zie2 = MockFactory.createMockZieScannable("zie2", 5);  // arbitrary level
		final Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"},
				new String[] {"%1.1g","%2.2g","%3.3f"},5,new Double[]{10.,20.,3.});
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input3"}, new String[0], new String[] {"%4.4g"},5,10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] {"extra2"}, new String[] {"%5.5g"},5,.901);

		final ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1, zie1, s2, zie2, s3});

		final String[] inputNames = group.getInputNames();
		final String[] extraNames = group.getExtraNames();
		final String[] outputFormat = group.getOutputFormat();
		final Object[] position = (Object[]) group.getPosition();

		assertArrayEquals(new String[] { "input1", "input2", "input3" }, inputNames);
		assertArrayEquals(new String[] { "extra1", "extra2" }, extraNames);
		// GDA-5794 from here
		assertArrayEquals(new Object[] {10., 20., 10., 3., .901}, position);
		assertArrayEquals(new String[] {"%1.1g", "%2.2g","%4.4g","%3.3f","%5.5g"}, outputFormat);
	}
	/**
	 * @throws Exception
	 */
	@Test
	public void testIsPositionValid() throws Exception {
		final Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"}, new String[] {"%5.5g","%5.5g","%1d"},5,new Double[]{10.,20.});
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input1"}, new String[0], new String[] {"%5.5g"},5,10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] {"extra1"}, new String[] {"%5.5g"},5,null);

		final ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1,s2,s3});
		assertTrue(group.checkPositionValid(new Double[]{1.,2.,3.}) == null);
		assertTrue(group.checkPositionValid(new Double[]{1.,2.,3.,4.}) != null);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testArrays() throws Exception {

		final Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"}, new String[] {"%5.5g","%5.5g","%1d"},5,new Double[]{10.,20.});
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input1"}, new String[0], new String[] {"%5.5g"},5,10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] {"extra1"}, new String[] {"%5.5g"},5,null);

		final ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1,s2,s3});
		assertEquals(3,group.getInputNames().length);
		assertEquals(2,group.getExtraNames().length);
		assertEquals(5,group.getOutputFormat().length);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testMoveTo() throws Exception {
		final Scannable zie1 = MockFactory.createMockZieScannable("zie1", 5);  // arbitrary level
		final Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"},
				new String[] {"%1.1g","%2.2g","%3.3f"},5,new Double[]{10.,20.,3.});
		final Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input3"}, new String[0], new String[] {"%4.4g"},5,10.);
		final Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] {"extra2"}, new String[] {"%5.5g"},5,.901);

		final ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1, zie1, s2, s3});

		group.asynchronousMoveTo(new Object[] {10., 20., 10.});
		verify(s1).asynchronousMoveTo(new Object[] {10., 20.});
		verify(s2).asynchronousMoveTo(10.);
		verify(zie1, never()).asynchronousMoveTo(any(Object.class));
		verify(s3).asynchronousMoveTo(new Object[] {});
		Object[] position = (Object[]) group.getPosition();

		assertArrayEquals(new Object[] {10., 20., 10., 3., .901}, position);
	}

	@Test
	public void testToFormattedStringNoErrors() {
		final Scannable s1 = new DummyScannable("s1");
		final Scannable s2 = new DummyScannable("s2");
		final ScannableGroup group = new ScannableGroup("sg1", new Scannable[] { s1, s2 });
		final String expectedResult = "sg1 ::\n  s1 : 0.0000 (-1.7977e+308:1.7977e+308)\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

		final String result = group.toFormattedString();
		assertEquals(expectedResult, result);
	}

	@Test
	public void testToFormattedStringWithError() {
		// In the event of an exception in one scannable, the ScannableGroup should show the value as dashes
		// and continue with the remaining scannables.
		final Scannable s1 = new DummyScannable("s1") {
			@Override
			public String toFormattedString() {
				throw new RuntimeException("failure in toFormattedString()");
			}
		};
		final Scannable s2 = new DummyScannable("s2");
		final ScannableGroup group = new ScannableGroup("sg1", new Scannable[] { s1, s2 });
		final String expectedResult = "sg1 ::\n  s1 : UNAVAILABLE\n  s2 : 0.0000 (-1.7977e+308:1.7977e+308)";

		final String result = group.toFormattedString();
		assertEquals(expectedResult, result);
	}

	private static ScannableGroup createAndCheckScannableGroupContaining(Scannable scannable) throws Exception {
		ScannableGroup sg = new ScannableGroup();
		sg.setGroupMembers(asList(scannable));
		sg.configure();
		assertTrue(sg.isConfigured());
		assertEquals(1, sg.getGroupMembers().size());
		assertSame(scannable, sg.getGroupMembers().get(0));
		assertEquals(1, sg.getGroupMemberNames().length);
		assertEquals(scannable.getName(), sg.getGroupMemberNames()[0]);
		return sg;
	}
	private static <T> T[] arrayOf(@SuppressWarnings("unchecked") T... items) {
		return items;
	}

}
