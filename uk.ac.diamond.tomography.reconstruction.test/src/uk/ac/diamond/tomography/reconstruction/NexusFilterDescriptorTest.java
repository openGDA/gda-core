/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.junit.Test;

import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor;
import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor.Operation;
import uk.ac.diamond.tomography.reconstruction.views.NexusFilterDescriptor;

public class NexusFilterDescriptorTest {

	@Test
	public void testBasic() {
		NexusFilterDescriptor descriptor = new NexusFilterDescriptor("/kichwa1", Operation.CONTAINS, null);
		assertEquals("/kichwa1", descriptor.getNexusFilterPath());
		assertEquals(Operation.CONTAINS, descriptor.getNexusFilterOperation());
		assertEquals(0, descriptor.getNexusFilterOperands().length);
		descriptor = new NexusFilterDescriptor("/kichwa1", Operation.EQUALS, new String[] { "123" });
		assertEquals(1, descriptor.getNexusFilterOperands().length);
		assertEquals("123", descriptor.getNexusFilterOperands()[0]);
		descriptor = new NexusFilterDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "123", "456" });
		assertEquals(2, descriptor.getNexusFilterOperands().length);
		assertEquals("123", descriptor.getNexusFilterOperands()[0]);
		assertEquals("456", descriptor.getNexusFilterOperands()[1]);
	}

	@Test
	public void testToString() {
		// In this test we want to make sure that toString does not throw exceptions, we are
		// not attempting to test accuracy of the returned string
		new NexusFilterDescriptor("/kichwa1", Operation.CONTAINS, null).toString();
		new NexusFilterDescriptor("/kichwa1", Operation.EQUALS, new String[] { "123" }).toString();
		new NexusFilterDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "123", "456" }).toString();
	}

	@Test
	public void testHashCode() {
		// In this test we want to make sure that hashCode does not throw exceptions, we are
		// not attempting to test accuracy of the hashCode
		new NexusFilterDescriptor("/kichwa1", Operation.CONTAINS, null).hashCode();
		new NexusFilterDescriptor("/kichwa1", Operation.EQUALS, new String[] { "123" }).hashCode();
		new NexusFilterDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "123", "456" }).hashCode();
	}

	@SuppressWarnings("unused")
	private void checkRaisesException(String nexusFilterPath, INexusFilterDescriptor.Operation nexusFilterOperation,
			String[] nexusFilterOperands) {
		try {
			new NexusFilterDescriptor(nexusFilterPath, nexusFilterOperation, nexusFilterOperands);
			fail("failed to raise exception");
		} catch (NullPointerException | IllegalArgumentException e) {
			// pass
		}

	}

	@Test
	public void testBad() {
		checkRaisesException(null, Operation.CONTAINS, null);
		checkRaisesException("/kichwa1", null, null);
		checkRaisesException("/kichwa1", Operation.EQUALS, null);
		checkRaisesException("/kichwa1", Operation.EQUALS, new String[0]);
		checkRaisesException("/kichwa1", Operation.EQUALS, new String[] { "123", "456" });
		checkRaisesException("/kichwa1", Operation.EQUALS, new String[] { null });
	}

	@Test
	public void testMementoAndEquals() throws WorkbenchException, NullPointerException, IllegalArgumentException {
		NexusFilterDescriptor descriptorIn = new NexusFilterDescriptor("/kichwa1", Operation.CONTAINS, null);
		String mementoString = descriptorIn.getMementoString();
		NexusFilterDescriptor descriptorOut = new NexusFilterDescriptor(mementoString);
		assertEquals("/kichwa1", descriptorOut.getNexusFilterPath());
		assertEquals(Operation.CONTAINS, descriptorOut.getNexusFilterOperation());
		assertEquals(0, descriptorOut.getNexusFilterOperands().length);
		assertEquals(descriptorIn, descriptorOut);

		descriptorIn = new NexusFilterDescriptor("/kichwa1", Operation.EQUALS, new String[] { "123" });
		mementoString = descriptorIn.getMementoString();
		descriptorOut = new NexusFilterDescriptor(mementoString);
		assertEquals(1, descriptorOut.getNexusFilterOperands().length);
		assertEquals("123", descriptorOut.getNexusFilterOperands()[0]);
		assertEquals(descriptorIn, descriptorOut);

		descriptorIn = new NexusFilterDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "123", "456" });
		mementoString = descriptorIn.getMementoString();
		descriptorOut = new NexusFilterDescriptor(mementoString);
		assertEquals(2, descriptorOut.getNexusFilterOperands().length);
		assertEquals("123", descriptorOut.getNexusFilterOperands()[0]);
		assertEquals("456", descriptorOut.getNexusFilterOperands()[1]);
		assertEquals(descriptorIn, descriptorOut);
	}

	private String createBadMemento(String nexusFilterPath, Operation nexusFilterOperation, String[] nexusFilterOperands) {
		XMLMemento memento = XMLMemento.createWriteRoot("root");
		if (nexusFilterPath != null)
			memento.putString("nexusFilterPath", nexusFilterPath);
		if (nexusFilterOperation != null)
			memento.putString("nexusFilterOperation", nexusFilterOperation.toString());
		if (nexusFilterOperands != null) {
			IMemento ops = memento.createChild("nexusFilterOperands");
			for (int i = 0; i < nexusFilterOperands.length; i++) {
				if (nexusFilterOperands[i] != null) {
					ops.putString("op" + i, nexusFilterOperands[i]);
				}
			}
		}
		try (StringWriter stringWriter = new StringWriter()) {
			memento.save(stringWriter);
			return stringWriter.toString();
		} catch (IOException e) {
			// unreachable because we are using a StringWriter
			return "";
		}
	}

	@SuppressWarnings("unused")
	private void checkMementoRaisesException(String nexusFilterPath,
			INexusFilterDescriptor.Operation nexusFilterOperation, String[] nexusFilterOperands)
			throws WorkbenchException {
		try {
			new NexusFilterDescriptor(createBadMemento(nexusFilterPath, nexusFilterOperation, nexusFilterOperands));
			fail("failed to raise exception");
		} catch (NullPointerException | IllegalArgumentException e) {
			// pass
		}

	}

	@Test
	public void testMementoBadFields() throws WorkbenchException, NullPointerException, IllegalArgumentException {
		checkMementoRaisesException(null, Operation.CONTAINS, null);
		checkMementoRaisesException("/kichwa1", null, null);
		checkMementoRaisesException("/kichwa1", Operation.EQUALS, null);
		checkMementoRaisesException("/kichwa1", Operation.EQUALS, new String[0]);
		checkMementoRaisesException("/kichwa1", Operation.EQUALS, new String[] { "123", "456" });
		checkMementoRaisesException("/kichwa1", Operation.EQUALS, new String[] { null });
	}

	@SuppressWarnings("unused")
	private void checkMementoBadFormatRaisesException(String format) throws WorkbenchException {
		try {
			new NexusFilterDescriptor(format);
			fail("failed to raise exception");
		} catch (WorkbenchException e) {
			// pass
		}

	}

	@SuppressWarnings("unused")
	@Test
	public void testMementoBadFormat() throws WorkbenchException, NullPointerException, IllegalArgumentException {
		try {
			new NexusFilterDescriptor(null);
			fail("failed to raise exception");
		} catch (NullPointerException e) {
			// pass
		}
		checkMementoBadFormatRaisesException("");
	}

}
