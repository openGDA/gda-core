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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;

/**
 * Tests {@link ObservableComponent}.
 */
public class ObservableComponentTest {

	private final ObservableComponent oc = new ObservableComponent();

	/**
	 * Test notifyIObservers swallows and does not cause any exceptions
	 */
	@Test
	public void testSwallowedExceptionLoggingDoesNotCauseException() {
		var observer = mock(IObserver.class);
		doThrow(RuntimeException.class).when(observer).update(any(), any());
		oc.addIObserver(observer);

		// notifyIObservers previously caused a NullPointerException
		// when it logged an exception and theObserved was null by
		// calling toString on theObserved
		oc.notifyIObservers(null, "\"theObserved is null\"");

		oc.notifyIObservers(1, "theObserved is not null");
	}

	/**
	 * Tests that all observers of an observable component receive an update,
	 * even if one of the observers deletes itself from the list of
	 * observers when it gets an update.
	 */
	@Test
	public void testAllObserversGetUpdateIfAnObserverDeletesItself() {

		var ob1 = mock(IObserver.class);

		var ob2 = mock(IObserver.class);
		doAnswer(inv -> {
			inv.<IObservable> getArgument(0).deleteIObserver(ob2);
			return null;
		}).when(ob2).update(eq(oc), any());

		var ob3 = mock(IObserver.class);

		var ob4 = mock(IObserver.class);

		var observers = List.of(ob1, ob2, ob3, ob4);

		observers.forEach(oc::addIObserver);

		assertThat(oc.getNumberOfObservers(), is(4));

		oc.notifyIObservers(oc, "test");

		assertThat(oc.getNumberOfObservers(), is(3));

		observers.forEach(ob -> verify(ob, times(1).description("Observer didn't receive an update")).update(oc, "test"));
	}

	@Test
	public void testAddingNullObserverThrows() {
		var e = assertThrows(IllegalArgumentException.class, () -> oc.addIObserver(null));
		assertEquals("Can't add a null observer", e.getMessage());
	}

	@Test
	public void testRemovingNullObserverThrows() {
		var e = assertThrows(IllegalArgumentException.class, () -> oc.deleteIObserver(null));
		assertEquals("Can't delete a null observer", e.getMessage());
	}

	@Test
	public void testIsBeingObserved() {
		// New shouldn't have any observers
		assertFalse(oc.isBeingObserved());

		// Add an observer
		oc.addIObserver(mock(IObserver.class));

		// Should now be being observed
		assertTrue(oc.isBeingObserved());

		// Remove all observers
		oc.deleteIObservers();

		// Now shouldn't be being observed again
		assertFalse(oc.isBeingObserved());
	}

	@Test
	public void testNumberOfObservers() {
		// Initially no observers
		assertThat(oc.getNumberOfObservers(), is(equalTo(0)));

		// Add one observer
		oc.addIObserver(mock(IObserver.class));

		// Should now have one
		assertThat(oc.getNumberOfObservers(), is(equalTo(1)));
	}

	@Test
	public void testGettingObservers() {
		// Initially no observers
		assertThat(oc.getObservers(), is(empty()));

		// Add one observer
		IObserver observer = mock(IObserver.class);
		oc.addIObserver(observer);

		// Should now have only that one
		assertThat(oc.getObservers(), contains(observer));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testGettingObserversReturnsUnmodifiableView() {
		// Try to add an observer should throw
		oc.getObservers().add(mock(IObserver.class));
	}

}
