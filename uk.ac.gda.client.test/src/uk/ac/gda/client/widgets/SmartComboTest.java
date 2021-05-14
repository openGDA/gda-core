/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SmartComboTest {

	private Display device;

	@Before
	public void setUp() {
		device = Display.getDefault();
	}

	@After
	public void setDown() {
	}

	/**
	 * An empty items list return an empty selected item
	 */
	@Test
	public void emptyComboTest() {
		SmartCombo<SmartComboTestype> sc = new SmartCombo<>(new Shell(device), SWT.NONE, null, Optional.empty());
		assertFalse(sc.getSelectedItem().isPresent());
	}

	/**
	 * With a single elements returns the only available element data
	 */
	@Test
	public void singleComboTest() {
		SmartCombo<SmartComboTestype> sc = new SmartCombo<>(new Shell(device), SWT.NONE, null, Optional.empty());
		List<ImmutablePair<String, SmartComboTestype>> items = new ArrayList<>();
		items.add(new ImmutablePair<String, SmartComboTest.SmartComboTestype>("one", SmartComboTestype.ONE));
		sc.populateCombo(items);

		assertTrue(sc.getSelectedItem().isPresent());
		assertEquals(SmartComboTestype.ONE, sc.getSelectedItem().get().getValue());
	}

	/**
	 * At the beginning selects the first item by default
	 */
	@Test
	public void multipleComboTest() {
		SmartCombo<SmartComboTestype> sc = new SmartCombo<>(new Shell(device), SWT.NONE, null, Optional.empty());
		List<ImmutablePair<String, SmartComboTestype>> items = new ArrayList<>();
		items.add(new ImmutablePair<String, SmartComboTest.SmartComboTestype>("one", SmartComboTestype.ONE));
		items.add(new ImmutablePair<String, SmartComboTest.SmartComboTestype>("two", SmartComboTestype.TWO));
		items.add(new ImmutablePair<String, SmartComboTest.SmartComboTestype>("three", SmartComboTestype.THREE));
		sc.populateCombo(items);

		assertTrue(sc.getSelectedItem().isPresent());
		assertEquals(SmartComboTestype.ONE, sc.getSelectedItem().get().getValue());
	}

	/**
	 * At the beginning selects the first item by default
	 */
	@Test
	public void multipleComboWithSelectionTest() {
		SmartCombo<SmartComboTestype> sc = new SmartCombo<>(new Shell(device), SWT.NONE, null, Optional.empty());
		List<ImmutablePair<String, SmartComboTestype>> items = new ArrayList<>();
		items.add(new ImmutablePair<String, SmartComboTest.SmartComboTestype>("one", SmartComboTestype.ONE));
		items.add(new ImmutablePair<String, SmartComboTest.SmartComboTestype>("two", SmartComboTestype.TWO));
		items.add(new ImmutablePair<String, SmartComboTest.SmartComboTestype>("three", SmartComboTestype.THREE));
		sc.populateCombo(items);

		assertTrue(sc.getSelectedItem().isPresent());
		assertEquals(SmartComboTestype.ONE, sc.getSelectedItem().get().getValue());

		sc.select(2);
		assertTrue(sc.getSelectedItem().isPresent());
		assertEquals(SmartComboTestype.THREE, sc.getSelectedItem().get().getValue());
	}

	private enum SmartComboTestype {
		ONE, TWO, THREE
	}
}
