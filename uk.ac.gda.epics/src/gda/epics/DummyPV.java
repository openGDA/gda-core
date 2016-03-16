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

import gov.aps.jca.event.PutListener;

import java.io.IOException;

public class DummyPV<T> extends DummyReadOnlyPV<T> implements PV<T> {

	public DummyPV(String pvName, T value) {
		super(pvName, value);
	}

	@Override
	public void putNoWait(T value) throws IOException {
	}

	@Override
	public void putNoWait(T value, PutListener pl) throws IOException {
	}

	@Override
	public void putWait(T value) throws IOException {
	}

	@Override
	public void putWait(T value, double timeoutS) throws IOException {
	}

	@Override
	public void putAsyncStart(T value) throws IOException {
	}

	@Override
	public void putAsyncWait() throws IOException {
	}

	@Override
	public boolean putAsyncIsWaiting() {
		return false;
	}

	@Override
	public void putAsyncCancel() {
	}

	@Override
	public void putAsyncWait(double timeoutS) throws IOException {
	}

	@Override
	public PV.PVValues putWait(T value, ReadOnlyPV<?>... toReturn) throws IOException {
		return pvValues;
	}

	@Override
	public PV.PVValues putWait(T value, double timeoutS, ReadOnlyPV<?>... toReturn) throws IOException {
		return pvValues;
	}

	private PV.PVValues pvValues = new PV.PVValues() {
		@SuppressWarnings("unchecked")
		@Override
		public <N> N get(ReadOnlyPV<N> pv) throws IllegalArgumentException {
			// For the moment, just return whatever value is set for this DummyPV
			return (N) DummyPV.this.value;
		}
	};
}
