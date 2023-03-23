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

package uk.ac.gda.core.virtualaxis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.api.virtualaxis.IVirtualAxisCombinedCalculator;

@ServiceInterface(Scannable.class)
public class CombinedManipulator extends ScannableBase implements IObserver {

	private List<Scannable> scannables = new ArrayList<>();
	private IVirtualAxisCombinedCalculator calculator;
	private IObserver iobserver = this::update;

	/**
	 * @return The calculator in use
	 */
	public IVirtualAxisCombinedCalculator getCalculator() {
		return calculator;
	}

	/**
	 * @param calculator Sets the calculator to be used
	 */
	public void setCalculator(IVirtualAxisCombinedCalculator calculator) {
		this.calculator = calculator;
	}

	/**
	 * @return The list of scannables moved by this combined scannable
	 */
	public List<Scannable> getScannables() {
		return new ArrayList<>(scannables);
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
		if (isConfigured()) {
			return;
		}
		setInputNames(new String[] {getName()});
		setupExtraNames();

		// Setup observers to pass through events of the "real" scannables
		// This allow the detection of this scannable starting to move when
		// one of its "real" scannables moves.
		for (Scannable s : scannables) {
			s.addIObserver(iobserver);
		}
		setConfigured(true);
	}

	private void setupExtraNames() {
		if (scannables == null) {
			setExtraNames(new String[0]);
			setOutputFormat(new String[] { "%5.3f" });
		} else {
			setExtraNames(scannables.stream().map(Scannable::getName).toArray(String[]::new));
			setOutputFormat(Collections.nCopies(scannables.size() + 1, "%5.3f").toArray(String[]::new));
		}
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

		if (position instanceof Number number) {
			doublePosition = number.doubleValue();
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

	private List<Double> getPositions() throws DeviceException {
		List<Double> pos = new ArrayList<>();
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
		final List<Double> pos = getPositions();
		pos.add(0, calculator.getRBV(pos));
		return pos.toArray(new Double[]{});
	}

	@Override
	public void update(Object source, Object arg) {
		notifyIObservers(source, arg);
	}
}