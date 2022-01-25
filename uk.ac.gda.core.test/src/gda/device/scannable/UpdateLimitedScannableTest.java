/*-
 * Copyright © 2021 Diamond Light Source Ltd., Science and Technology
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import gda.device.Scannable;
import gda.observable.IObserver;

public class UpdateLimitedScannableTest {

	Scannable mockDelegate;
	UpdateLimitedScannable updateLimitedScannable;
	long mockCurrentTime = 100;

	@Before
	public void setUp() {
		Supplier<Date> mockDateFactory = () -> {
			Date mockDate = mock(Date.class);
			when(mockDate.getTime()).thenReturn(mockCurrentTime);
			return mockDate;
		};
		mockDelegate = mock(Scannable.class);
		updateLimitedScannable = new UpdateLimitedScannable(mockDelegate, mockDateFactory);
	}

	@Test
	public void testWhenInitializedThenDelegateNotObserved() {
		verify(mockDelegate, never()).addIObserver(any());
	}

	@Test
	public void testWhenObserverAddedThenObserverAddedToDelegate() {
		updateLimitedScannable.addIObserver(mock(IObserver.class));
		verify(mockDelegate, times(1)).addIObserver(eq(updateLimitedScannable));
	}

	@Test
	public void testWhenTwoObserversAddedThenOneObserverAddedToDelegate() {
		updateLimitedScannable.addIObserver(mock(IObserver.class));
		updateLimitedScannable.addIObserver(mock(IObserver.class));
		verify(mockDelegate, times(1)).addIObserver(eq(updateLimitedScannable));
	}

	@Test
	public void testGivenOneObserverAddedWhenObserverRemovedThenObserverRemovedFromDelegate() {
		IObserver mockObserver = mock(IObserver.class);
		updateLimitedScannable.addIObserver(mockObserver);
		updateLimitedScannable.deleteIObserver(mockObserver);
		verify(mockDelegate, times(1)).deleteIObserver(eq(updateLimitedScannable));
	}

	@Test
	public void testGivenTwoObserversAddedWhenOneObserverRemovedThenObserverNotRemovedFromDelegate() {
		IObserver mockObserver = mock(IObserver.class);
		updateLimitedScannable.addIObserver(mockObserver);
		updateLimitedScannable.addIObserver(mock(IObserver.class));
		updateLimitedScannable.deleteIObserver(mockObserver);
		verify(mockDelegate, never()).deleteIObserver(any());
	}

	@Test
	public void testGivenTwoObserversAddedWhenAllRemovedThenObserverRemovedFromDelegate() {
		updateLimitedScannable.addIObserver(mock(IObserver.class));
		updateLimitedScannable.addIObserver(mock(IObserver.class));
		updateLimitedScannable.deleteIObservers();
		verify(mockDelegate, times(1)).deleteIObserver(eq(updateLimitedScannable));
	}

	@Test
	public void testNoUpdatesYetWhenUpdateCalledThenObserverNotified() {
		IObserver mockObserver = mock(IObserver.class);
		updateLimitedScannable.addIObserver(mockObserver);

		updateLimitedScannable.update(null, null);
		verify(mockObserver, times(1)).update(any(), any());
	}

	@Test
	public void testGivenTimeBetweenUpdatesAs0WhenTwoUpdatesCalledAndTimeIncreasedThenObserverNotifiedTwice() {
		IObserver mockObserver = mock(IObserver.class);
		updateLimitedScannable.addIObserver(mockObserver);
		updateLimitedScannable.setMsBetweenUpdates(0);

		updateLimitedScannable.update(null, null); // Will notify
		mockCurrentTime += 1;
		updateLimitedScannable.update(null, null); // Will notify
		verify(mockObserver, times(2)).update(any(), any());
	}

	@Test
	public void testWhenTwoUpdatesCalledAndTimeNotIncreasedEnoughThenObserverNotifiedOnce() {
		IObserver mockObserver = mock(IObserver.class);
		updateLimitedScannable.addIObserver(mockObserver);
		updateLimitedScannable.setMsBetweenUpdates(10);

		updateLimitedScannable.update(null, null); // Will notify
		mockCurrentTime += 2;
		updateLimitedScannable.update(null, null); // Will not notify
		verify(mockObserver, times(1)).update(any(), any());
	}

	@Test
	public void testWhenThreeUpdatesCalledAndTimeIncreasedEnoughOnFinalOneThenObserverNotifiedTwice() {
		IObserver mockObserver = mock(IObserver.class);
		updateLimitedScannable.addIObserver(mockObserver);
		updateLimitedScannable.setMsBetweenUpdates(10);

		updateLimitedScannable.update(null, null); // Will notify
		mockCurrentTime += 1;
		updateLimitedScannable.update(null, null); // Will not notify
		mockCurrentTime += 10;
		updateLimitedScannable.update(null, null); // Will notify
		verify(mockObserver, times(2)).update(any(), any());
	}

	@Test
	public void testWhenThreeUpdatesCalledAndTimeNotIncreasedEnoughBetweenSecondTwoThenObserverNotifiedTwice() {
		IObserver mockObserver = mock(IObserver.class);
		updateLimitedScannable.addIObserver(mockObserver);
		updateLimitedScannable.setMsBetweenUpdates(10);

		updateLimitedScannable.update(null, null); // Will notify
		mockCurrentTime += 11;
		updateLimitedScannable.update(null, null); // Will notify
		mockCurrentTime += 2;
		updateLimitedScannable.update(null, null); // Will not notify
		verify(mockObserver, times(2)).update(any(), any());
	}

}