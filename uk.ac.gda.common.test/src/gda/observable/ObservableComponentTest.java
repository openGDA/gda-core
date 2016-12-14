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

package gda.observable;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests {@link ObservableComponent}.
 */
public class ObservableComponentTest {

	/**
	 * Test notifyIObservers swallows and does not cause any exceptions
	 */
	@Test
	public void testSwallowedExceptionLoggingDoesNotCauseException() {
		ObservableComponent oc = new ObservableComponent();
		oc.addIObserver(new IObserver() {
			@Override
			public void update(Object source, Object arg) {
				throw new RuntimeException("should be swallowed");
			}
		});
		// notifyIObservers previously caused a NullPointerException
		// when it logged an exception and theObserved was null by
		// calling toString on theObserved
		try {
			oc.notifyIObservers(null, "\"theObserved is null\"");
			assertTrue("toStringSubstitute should be null", true);

			oc.notifyIObservers(new Integer(1), "theObserved is not null");
			assertTrue("toStringSubstitute should be <Integer>", true);
		}
		catch (RuntimeException ex) {
			fail("notifyIObservers should swallow exceptions");
		}
	}

	/**
	 * Tests that all observers of an observable component receive an update,
	 * even if one of the observers deletes itself from the list of
	 * observers when it gets an update.
	 */
	@Test
	public void testAllObserversGetUpdateIfAnObserverDeletesItself() {

		TestObserver[] observers = new TestObserver[] {
			new TestObserver("1"),
			new DeleteSelfObserver("2"),
			new TestObserver("3"),
			new TestObserver("4")
		};

		ObservableComponent oc = new ObservableComponent();
		for (IObserver observer : observers) {
			oc.addIObserver(observer);
		}

		oc.notifyIObservers(oc, "test");

		for (TestObserver observer : observers) {
			assertTrue("Observer '" + observer.getName() + "' didn't receive an update", observer.receivedUpdate());
		}
	}

	/**
	 * Implementation of {@link IObserver} for testing, which has an ID and a
	 * flag for indicating whether an update was received.
	 */
	static class TestObserver implements IObserver {

		protected String name;

		protected boolean receivedUpdate;

		public TestObserver(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public void update(Object theObserved, Object changeCode) {
			System.out.println("[" + name + "] received update: " + changeCode);
			receivedUpdate = true;
		}

		public boolean receivedUpdate() {
			return receivedUpdate;
		}
	}

	/**
	 * An {@link IObserver} that deletes itself from the observable's observers
	 * when it receives an update.
	 */
	static class DeleteSelfObserver extends TestObserver {

		public DeleteSelfObserver(String name) {
			super(name);
		}

		@Override
		public void update(Object theObserved, Object changeCode) {
			super.update(theObserved, changeCode);
			System.out.println("[" + name + "] deleting self...");
			((IObservable) theObserved).deleteIObserver(this);
		}
	}

}
