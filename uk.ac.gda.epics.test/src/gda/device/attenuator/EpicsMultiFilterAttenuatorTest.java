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

package gda.device.attenuator;

import static gda.device.attenuator.EpicsMultiFilterAttenuator.collectionContainsOnly;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

public class EpicsMultiFilterAttenuatorTest extends TestCase {
	
	class A {}
	
	class B extends A {}
	
	class C extends B {}
	
	public void testCollectionContainsOnly() {
		List<A> filters = new Vector<A>();
		assertTrue(collectionContainsOnly(filters, B.class));
		
		// Should be OK with an instance of B itself
		filters.add(new B());
		assertTrue(collectionContainsOnly(filters, B.class));
		
		// Should also be OK with an instance of a subclass of B
		filters.add(new C());
		assertTrue(collectionContainsOnly(filters, B.class));
		
		// But should fail with an instance of a superclass of B
		filters.add(new A());
		assertFalse(collectionContainsOnly(filters, B.class));
	}

}
