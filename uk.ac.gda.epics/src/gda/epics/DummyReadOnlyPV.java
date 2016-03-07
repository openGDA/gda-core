/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.epics;

import gda.observable.Observer;
import gda.observable.Predicate;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.event.MonitorListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class DummyReadOnlyPV<T> implements ReadOnlyPV<T> {

	private Map<Observer<T>, Predicate<T>> observers = new HashMap<>();
	private Set<MonitorListener> monitorListeners = new HashSet<>();
	private boolean valueMonitoring = false;
	private String pvName;
	private T value;

	public DummyReadOnlyPV(final String pvName, final T value) {
		this.pvName = pvName;
		this.value = value;
	}

	@Override
	public void addObserver(Observer<T> observer) throws Exception {
		observers.put(observer, null);
	}

	@Override
	public void addObserver(Observer<T> observer, Predicate<T> predicate) throws Exception {
		observers.put(observer, predicate);
	}

	@Override
	public void removeObserver(Observer<T> observer) {
		observers.remove(observer);
	}

	@Override
	public String getPvName() {
		return pvName;
	}

	@Override
	public T get() throws IOException {
		return value;
	}

	@Override
	public T get(int numElements) throws IOException {
		return value;
	}

	@Override
	public T getLast() throws IOException {
		return value;
	}

	@Override
	public T waitForValue(Predicate<T> predicate, double timeoutS) throws IllegalStateException, TimeoutException, IOException, InterruptedException {
		return value;
	}

	@Override
	public void setValueMonitoring(boolean shouldMonitor) throws IOException {
		valueMonitoring = shouldMonitor;
	}

	@Override
	public boolean isValueMonitoring() {
		return valueMonitoring;
	}

	@Override
	public void addMonitorListener(MonitorListener listener) throws IOException {
		monitorListeners.add(listener);
	}

	@Override
	public void removeMonitorListener(MonitorListener listener) {
		monitorListeners.remove(listener);
	}

	@Override
	public T extractValueFromDbr(DBR dbr) {
		return value;
	}

}
