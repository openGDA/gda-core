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

package uk.ac.diamond.daq.persistence.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.persistence.classloader.PersistenceClassLoader;
import uk.ac.diamond.daq.persistence.data.AbstractItem;
import uk.ac.diamond.daq.persistence.data.ConcreteItemA;
import uk.ac.diamond.daq.persistence.data.ConcreteItemB;
import uk.ac.diamond.daq.persistence.data.ConcreteItemBsubA;
import uk.ac.diamond.daq.persistence.implementation.json.impl.DefaultJsonSerialisationFactory;
import uk.ac.diamond.daq.persistence.implementation.service.PersistenceException;

public class PersistenceManagerTest {

	private static final String CONCRETE_ITEM_B_NAME_1 = "Tomo Scan 1";

	private PersistenceManager persistenceManager;

	private ConcreteItemB concreteItemB = new ConcreteItemB(CONCRETE_ITEM_B_NAME_1, 100, 360.0);

	@Before
	public void setUp() {
		persistenceManager = new PersistenceManager(new DefaultJsonSerialisationFactory(),
				PersistenceClassLoader.getInstance(),
				new TestVisitService("current"));
	}

	/**
	 * Searching by an ID returns an object that matches the one that was persisted
	 * but a new memory reference Searching by ID works for both the concrete class,
	 * any super class (Abstract or otherwise) Retrieved items must also have
	 * different references than each other Retrieved items should be deserialised
	 * into the subclass they went into the database as
	 */
	@Test
	public void testSearchById() throws PersistenceException {
		final ConcreteItemBsubA concreteItemB_A = new ConcreteItemBsubA("Name", 1, 1, 1);

		persistenceManager.save(concreteItemB_A);

		final ConcreteItemBsubA retrievedSameClass = persistenceManager.get(concreteItemB_A.getId(), ConcreteItemBsubA.class);
		final ConcreteItemB retrievedSuperClass = persistenceManager.get(concreteItemB_A.getId(), ConcreteItemB.class);
		final AbstractItem retrievedAbstractSuperClass = persistenceManager.get(concreteItemB_A.getId(), AbstractItem.class);

		assertNotSame("Different references required", concreteItemB_A, retrievedSameClass);
		assertEquals("Retrieved item must equals()", concreteItemB_A, retrievedSameClass);

		assertNotSame("Different references required", concreteItemB_A, retrievedSuperClass);
		assertEquals("Retrieved item must equals()", concreteItemB_A, retrievedSuperClass);

		assertNotSame("Different references required", concreteItemB_A, retrievedAbstractSuperClass);
		assertEquals("Retrieved item must equals()", concreteItemB_A, retrievedAbstractSuperClass);

		assertNotSame("Different references required", retrievedSameClass, retrievedSuperClass);
		assertNotSame("Different references required", retrievedSameClass, retrievedAbstractSuperClass);
		assertNotSame("Different references required", retrievedSuperClass, retrievedAbstractSuperClass);

		assertTrue("Not deserialised as B_A", retrievedSuperClass instanceof ConcreteItemBsubA);
		assertTrue("Not deserialised as B_A", retrievedAbstractSuperClass instanceof ConcreteItemBsubA);

	}

	@Test(expected = PersistenceException.class)
	public void testWrongClassResults() throws PersistenceException {
		// Searches for wrong class, so no objects found (but object with that ID does
		// exist in database)
		persistenceManager.get(concreteItemB.getId(), ConcreteItemA.class);
	}
}
