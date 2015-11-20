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

package uk.ac.gda.arpes.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 */
@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class CombinedManipulator extends ScannableBase implements IObserver {

	private List<Scannable> scannables = new Vector<Scannable>();
	private CombinedCaculator calculator;
	private IObserver iobserver = new IObserver() {

		@Override
		public void update(Object source, Object arg) {
			updateEvent(source, arg);
		}
	};

	/**
	 * @return The calculator in use
	 */
	public CombinedCaculator getCalculator() {
		return calculator;
	}

	/**
	 * @param calculator Sets the calculator to be used
	 */
	public void setCalculator(CombinedCaculator calculator) {
		this.calculator = calculator;
	}

	/**
	 * @return The list of scannables moved by this combined scannable
	 */
	public List<Scannable> getScannables() {
		return new Vector<Scannable>(scannables);
	}

	/**
	 * @param scannables Sets the list of scannables moved by this combined scannable
	 */
	public void setScannables(List<Scannable> scannables) {
		this.scannables = scannables;
		setupExtraNames();
	}

	@Override
	public void configure() throws FactoryException {
		setInputNames(new String[] {getName()});
		setupExtraNames();

		// Setup observers to pass through events of the "real" scannables
		// This allow the detection of this scannable starting to move when
		// one of its "real" scannables moves.
		for (Scannable s : scannables) {
			s.addIObserver(iobserver);
		}
	}

	private void setupExtraNames() {
		Vector<String> en = new Vector<String>();
		Vector<String> of = new Vector<String>();
		of.add("%5.3f"); //us
		if (scannables != null) {
			for (Scannable s : scannables) {
				en.add(s.getName());
				of.add("%5.3f");
			}
		}
		setExtraNames(en.toArray(new String[]{}));
		setOutputFormat(of.toArray(new String[]{}));
	}

	@Override
	public boolean isBusy() throws DeviceException {
		for(Scannable s : scannables) {
			if (s.isBusy()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		Double doublePosition;

		if (position instanceof Number) {
			doublePosition = ((Number) position).doubleValue();
		} else {
			doublePosition = Double.valueOf(position.toString());
		}

		List<Double> demands = calculator.getDemands(doublePosition, getPositions());

		Iterator<Double> demandsIterator = demands.iterator();
		Iterator<Scannable> scannablesIterator = scannables.iterator();

		// Move each of the scannables to their demanded position.
		while (demandsIterator.hasNext()) {
			scannablesIterator.next().asynchronousMoveTo(demandsIterator.next());
		}
	}

	Vector<Double> getPositions() throws DeviceException {
		Vector<Double> pos = new Vector<Double>();
		for (Scannable s : scannables) {
			Object position = s.getPosition();
			if (position instanceof Object[]) {
				pos.add(((Double[]) s.getPosition())[0]);
			} else {
				pos.add((Double) s.getPosition());
			}

		}
		return pos;
	}

	@Override
	public Object getPosition() throws DeviceException {
		Vector<Double> pos = getPositions();
		Double rbv = calculator.getRBV(pos);
		pos.insertElementAt(rbv, 0);
		return pos.toArray(new Double[]{});
	}

	/**
	 * This should pass the events fired by the underlying scannables and fire events
	 * from the combined scannable.
	 * @param source
	 * @param arg
	 */
	private void updateEvent(Object source, Object arg){
		update(source, arg);
	}

	@Override
	public void update(Object source, Object arg) {
		notifyIObservers(source, arg);
	}
}