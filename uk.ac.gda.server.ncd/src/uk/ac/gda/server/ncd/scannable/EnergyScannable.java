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

import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;

/**
 * This is a compound scannable that operates on a bragg (wavelength determining) scannable and a
 * number of other scannables (id_gap, detector thresholds, etc.)
 * 
 * It is also used as metadata provider for images plotted 
 */
@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class EnergyScannable extends ScannableBase implements IObserver {

	private Scannable bragg;
	private Collection<Scannable> scannables = new Vector<Scannable>();
	private DiffractionCrystalEnvironment dce = null;
	
	@Override
	public void configure() throws FactoryException {
		setInputNames(new String[] {getName()});
		setupExtraNames();
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
			s.asynchronousMoveTo(externalPosition);
		}
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		Object[] pos = new Object[scannables.size()+1];
		int i = 0;
		pos[i] = bragg.getPosition();
		for (Scannable s : scannables) {
			i++;
			pos[i] = s.getPosition();
		}
		return pos;
	}
	
	public void addScannable(Scannable s) {
		if (!scannables.contains(s)) {
			scannables.add(s);
			setupExtraNames();
		}
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
		return 12.3984191/((Double) bragg.getPosition());
	}

	@Override
	public void update(Object source, Object arg) {
		dce = null;
	}
}