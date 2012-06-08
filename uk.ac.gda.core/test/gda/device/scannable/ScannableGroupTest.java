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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import gda.MockFactory;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.factory.MapFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

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
		
		Factory factory = new MapFactory("test", null);
		factory.addFindable(s1);
		factory.addFindable(s2);
		
		Finder.getInstance().clear();
		Finder.getInstance().addFactory(factory);
	}

	/**
	 * Tests that a newly-instantiated {@link ScannableGroup} initially has no
	 * group members or group member names.
	 */
	@Test
	public void testNewlyInstantiatedScannableGroup() {
		ScannableGroup sg = new ScannableGroup();
		assertEquals(EMPTY_SCANNABLE_LIST, sg.getGroupMembers());
		assertArraysEqual(EMPTY_NAME_ARRAY, sg.getGroupMemberNames());
	}
	
	/**
	 * Tests configuring a {@link ScannableGroup} using only group members.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfiguringScannableGroupUsingMembers() throws Exception {
		ScannableGroup sg = new ScannableGroup();
		sg.setGroupMembers(arrayListOf(s1));
		
		assertFalse("Expected ScannableGroup to be unconfigured", sg.isConfigured());
		assertEquals(arrayListOf(s1), sg.getGroupMembers());
		assertArraysEqual(EMPTY_NAME_ARRAY, sg.getGroupMemberNames());
		
		sg.configure();
		
		assertTrue("Expected ScannableGroup to be configured", sg.isConfigured());
		assertEquals(arrayListOf(s1), sg.getGroupMembers());
		assertArraysEqual(arrayOf("s1"), sg.getGroupMemberNames());
	}
	
	/**
	 * Tests configuring a {@link ScannableGroup} using only group member names.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfiguringScannableGroupUsingMemberNames() throws Exception {
		ScannableGroup sg = new ScannableGroup();
		sg.setGroupMemberNames(arrayListOf("s2"));

		assertFalse("Expected ScannableGroup to be unconfigured", sg.isConfigured());
		assertEquals(EMPTY_SCANNABLE_LIST, sg.getGroupMembers());
		assertArraysEqual(arrayOf("s2"), sg.getGroupMemberNames());

		sg.configure();
		
		assertTrue("Expected ScannableGroup to be configured", sg.isConfigured());
		assertEquals(arrayListOf(s2), sg.getGroupMembers());
		assertArraysEqual(arrayOf("s2"), sg.getGroupMemberNames());
	}
	
	/**
	 * Tests configuring a {@link ScannableGroup} using both members and member
	 * names.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfiguringScannableGroupUsingMembersAndNames() throws Exception {
		ScannableGroup sg = new ScannableGroup();
		sg.setGroupMembers(arrayListOf(s1));
		sg.setGroupMemberNames(arrayListOf("s2"));
		
		assertFalse("Expected ScannableGroup to be unconfigured", sg.isConfigured());
		assertEquals(arrayListOf(s1), sg.getGroupMembers());
		assertArraysEqual(arrayOf("s2"), sg.getGroupMemberNames());
		
		sg.configure();
		
		assertTrue("Expected ScannableGroup to be configured", sg.isConfigured());
		assertEquals(arrayListOf(s1, s2), sg.getGroupMembers());
		assertArraysEqual(arrayOf("s1", "s2"), sg.getGroupMemberNames());
	}
	
	/**
	 * Tests adding a group member to a group after the group has been
	 * configured. Should update the group members and the group member names.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddingGroupMemberAfterConfiguration() throws Exception {
		ScannableGroup sg = createAndCheckScannableGroupContaining(s1);
		sg.addGroupMember(s2);
		assertEquals(arrayListOf(s1, s2), sg.getGroupMembers());
		assertArraysEqual(arrayOf("s1", "s2"), sg.getGroupMemberNames());
	}
	
	/**
	 * Tests setting a group's members after it has been configured. Should
	 * update the group members and the group member names.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSettingGroupMembersAfterConfiguration() throws Exception {
		ScannableGroup sg = createAndCheckScannableGroupContaining(s1);
		sg.setGroupMembers(arrayListOf(s2));
		assertEquals(arrayListOf(s2), sg.getGroupMembers());
		assertArraysEqual(arrayOf("s2"), sg.getGroupMemberNames());
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
		ScannableGroup sg = createAndCheckScannableGroupContaining(s1);
		sg.addGroupMemberName("s2");
		assertEquals(arrayListOf(s1), sg.getGroupMembers());
		assertArraysEqual(arrayOf("s1", "s2"), sg.getGroupMemberNames());
	}
	
	/**
	 * Tests setting a group's member names after it has been configured.
	 * Should only update the group member names, not the group members.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSettingGroupMemberNamesAfterConfiguration() throws Exception {
		ScannableGroup sg = createAndCheckScannableGroupContaining(s1);
		sg.setGroupMemberNames(arrayListOf("s2"));
		assertEquals(arrayListOf(s1), sg.getGroupMembers());
		assertArraysEqual(arrayOf("s2"), sg.getGroupMemberNames());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testScanHooks() throws Exception {
		
		Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"}, new String[] {"%5.5g","%5.5g","%1d"},5,new Double[]{10.,20.});
		Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input1"}, new String[0], new String[] {"%5.5g"},5,10.);
		Scannable s3 = MockFactory.createMockScannable("s2", new String[0], new String[] {"extra1"}, new String[] {"%5.5g"},5,null);
		
		ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1,s2,s3});
		
		group.atPointEnd();
		group.atPointStart();
		group.atScanLineEnd();
		group.atScanEnd();
		group.atLevelMoveStart();
		group.atCommandFailure();
		group.atScanStart();
		group.atScanLineStart();
		group.stop();
		
		
		for (Scannable scannable : new Scannable[] {s1,s2,s3})
		{
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
	public void testIsPositionValid() throws Exception {
		
		Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"}, new String[] {"%5.5g","%5.5g","%1d"},5,new Double[]{10.,20.});
		Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input1"}, new String[0], new String[] {"%5.5g"},5,10.);
		Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] {"extra1"}, new String[] {"%5.5g"},5,null);
		
		ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1,s2,s3});
		assertTrue(group.checkPositionValid(new Double[]{1.,2.,3.}) == null);
		assertTrue(group.checkPositionValid(new Double[]{1.,2.,3.,4.}) != null);

	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testArrays() throws Exception {
		
		Scannable s1 = MockFactory.createMockScannable("s1", new String[]{"input1", "input2"}, new String[] {"extra1"}, new String[] {"%5.5g","%5.5g","%1d"},5,new Double[]{10.,20.});
		Scannable s2 = MockFactory.createMockScannable("s2", new String[]{"input1"}, new String[0], new String[] {"%5.5g"},5,10.);
		Scannable s3 = MockFactory.createMockScannable("s3", new String[0], new String[] {"extra1"}, new String[] {"%5.5g"},5,null);
		
		ScannableGroup group = new ScannableGroup("group", new Scannable[] {s1,s2,s3});
		assertEquals(3,group.getInputNames().length);
		assertEquals(2,group.getExtraNames().length);
		assertEquals(5,group.getOutputFormat().length);

	}



	
	

	private ScannableGroup createAndCheckScannableGroupContaining(Scannable... scannables) throws Exception {
		ScannableGroup sg = new ScannableGroup();
		sg.setGroupMembers(new ArrayList<Scannable>(Arrays.asList(scannables)));
		sg.configure();
		assertTrue(sg.isConfigured());
		assertEquals(1, sg.getGroupMembers().size());
		assertSame(s1, sg.getGroupMembers().get(0));
		assertEquals(1, sg.getGroupMemberNames().length);
		assertEquals("s1", sg.getGroupMemberNames()[0]);
		return sg;
	}
	
	private static <T> ArrayList<T> arrayListOf(T... items) {
		return new ArrayList<T>(Arrays.asList(items));
	}
	
	private static <T> T[] arrayOf(T... items) {
		return items;
	}
	
	private static <T> void assertArraysEqual(T[] one, T[] two) {
		assertEquals(one.length, two.length);
		for (int i=0; i<one.length; i++) {
			assertEquals(one[i], two[i]);
		}
	}
	
}
