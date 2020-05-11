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

import uk.ac.diamond.daq.application.persistence.factory.impl.InMemoryPersistenceServiceFactory;
import uk.ac.diamond.daq.application.persistence.service.PersistenceException;
import uk.ac.diamond.daq.application.persistence.service.PersistenceService;
import uk.ac.diamond.daq.persistence.classloader.PersistenceClassLoader;
import uk.ac.diamond.daq.persistence.data.AbstractItem;
import uk.ac.diamond.daq.persistence.data.ConcreteItemA;
import uk.ac.diamond.daq.persistence.data.ConcreteItemB;
import uk.ac.diamond.daq.persistence.data.ConcreteItemBSubA;

public class PersistenceManagerTest {

	private static final String CONCRETE_ITEM_B_NAME_1 = "Tomo Scan 1";

	private PersistenceService persistenceService;

	private ConcreteItemB concreteItemB = new ConcreteItemB(CONCRETE_ITEM_B_NAME_1, 100, 360.0);

	@Before
	public void setUp() {

		 persistenceService = new InMemoryPersistenceServiceFactory(new PersistenceClassLoader(),
				new TestVisitService("current")).getPersistenceService();
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
		ConcreteItemBSubA concreteItemB_A = new ConcreteItemBSubA("Name", 1, 1, 1);

		persistenceService.save(concreteItemB_A);

		final ConcreteItemBSubA retrievedSameClass = persistenceService.get(concreteItemB_A.getId(),
				ConcreteItemBSubA.class);
		final ConcreteItemB retrievedSuperClass = persistenceService.get(concreteItemB_A.getId(), ConcreteItemB.class);
		final AbstractItem retrievedAbstractSuperClass = persistenceService.get(concreteItemB_A.getId(),
				AbstractItem.class);

		assertNotSame("Different references required", concreteItemB_A, retrievedSameClass);
		assertEquals("Retrieved item must equals()", concreteItemB_A, retrievedSameClass);

		assertNotSame("Different references required", concreteItemB_A, retrievedSuperClass);
		assertEquals("Retrieved item must equals()", concreteItemB_A, retrievedSuperClass);

		assertNotSame("Different references required", concreteItemB_A, retrievedAbstractSuperClass);
		assertEquals("Retrieved item must equals()", concreteItemB_A, retrievedAbstractSuperClass);

		assertNotSame("Different references required", retrievedSameClass, retrievedSuperClass);
		assertNotSame("Different references required", retrievedSameClass, retrievedAbstractSuperClass);
		assertNotSame("Different references required", retrievedSuperClass, retrievedAbstractSuperClass);

		assertTrue("Not deserialised as B_A", retrievedSuperClass instanceof ConcreteItemBSubA);
		assertTrue("Not deserialised as B_A", retrievedAbstractSuperClass instanceof ConcreteItemBSubA);

	}


	@Test(expected = PersistenceException.class)
	public void testWrongClassResults() throws PersistenceException {
		// Searches for wrong class, so no objects found (but object with that ID does
		// exist in database)
		persistenceService.get(concreteItemB.getId(), ConcreteItemA.class);
	}
}
