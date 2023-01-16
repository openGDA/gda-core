/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.scannable;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This is a compound scannable that operates on a bragg (wavelength determining) scannable and a
 * number of other scannables (id_gap, detector thresholds, etc.)
 *
 * It is also used as metadata provider for images plotted
 */
@ServiceInterface(Scannable.class)
public class EnergyScannable extends ScannableBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(EnergyScannable.class);
	private Scannable bragg;
	private Collection<Scannable> scannables = new Vector<Scannable>();
	private DiffractionCrystalEnvironment dce = null;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		setInputNames(new String[] {getName()});
		setupExtraNames();
		setConfigured(true);
	}

	private void setupExtraNames() {
		Vector<String> en = new Vector<String>();
		Vector<String> of = new Vector<String>();
		of.add("%5.3f");
		for (Scannable s : scannables) {
			en.add(s.getName());
			of.add("%5.3f");
		}
		setExtraNames(en.toArray(new String[]{}));
		setOutputFormat(of.toArray(new String[]{}));
	}

	@Override
	public boolean isBusy() throws DeviceException {
		for(Scannable s : scannables) {
			if (s.isBusy()) {
				dce = null;
				return true;
			}
		}
		if (bragg.isBusy()) {
			dce = null;
			return true;
		}
		return false;
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		dce = null;
		bragg.asynchronousMoveTo(externalPosition);
		for(Scannable s : scannables) {
			try {
				s.asynchronousMoveTo(externalPosition);
			} catch (Exception e) {
				InterfaceProvider.getTerminalPrinter().print(String.format("Could not move %s to %s (%s)", s.getName(), externalPosition, e.getMessage()));
				logger.warn("Could not move scannable {} to {}", s.getName(), externalPosition, e);
			}
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		Object[] pos = new Object[scannables.size()+1];
		int i = 0;
		pos[i] = bragg.getPosition();
		for (Scannable s : scannables) {
			i++;
			try {
				pos[i] = s.getPosition();
			} catch (Exception e) {
				logger.warn("Can't access scannable {}", s.getName(), e);
				pos[i] = Double.NaN;
			}
		}
		return pos;
	}

	public void addScannable(Scannable s) {
		if (!scannables.contains(s)) {
			scannables.add(s);
			setupExtraNames();
		}
	}

	public void addScannables(Collection<Scannable> s) {
		s.forEach(this::addScannable);
	}

	public void removeScannable(Scannable s) {
		scannables.remove(s);
		setupExtraNames();
	}

	public void clearScannables() {
		scannables.clear();
		setupExtraNames();
	}

	public Scannable getBragg() {
		return bragg;
	}

	/**
	 * this bragg scannable is expected to work in keV and return double value only
	 *
	 * @param bragg
	 */
	public void setBragg(Scannable bragg) {
		if (this.bragg != null)
			this.bragg.deleteIObserver(this);
		this.bragg = bragg;
		this.bragg.addIObserver(this);
	}

	public DiffractionCrystalEnvironment getDiffractionCrystalEnvironment() throws DeviceException {
		if (dce == null) {
			dce = new DiffractionCrystalEnvironment(getBraggWavelength());
		}
		return dce;

	}

	public double getBraggWavelength() throws DeviceException {
		return DiffractionCrystalEnvironment.calculateWavelength((Double) bragg.getPosition());
	}

	@Override
	public void update(Object source, Object arg) {
		dce = null;
	}
}
