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

package uk.ac.gda.beamline.i05.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

import java.util.Collection;
import java.util.Vector;

/**
 * This is a compound scannable that operates on a bragg (wavelength determining) scannable and a
 * number of other scannables (id_gap, detector thresholds, etc.)
 * 
 * It is also used as metadata provider for images plotted 
 */
@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class EnergyScannable extends ScannableBase {

	private Scannable pgm;
	private I05Apple id;
	private Collection<Scannable> scannables = new Vector<Scannable>();
	
	@Override
	public void configure() throws FactoryException {
		setInputNames(new String[] {getName()});
		setupExtraNames();
	}
			
	private void setupExtraNames() {
		Vector<String> en = new Vector<String>();
		Vector<String> of = new Vector<String>();
		of.add("%5.3f"); //us - pgm
//		of.add("%5.3f"); //id (gap)
//		of.add("%s"); //id(polarisation) //FIXME
		for (Scannable s : scannables) {
			en.add(s.getName());
			of.add("%5.3f");
		}
		setExtraNames(en.toArray(new String[]{}));
		setOutputFormat(of.toArray(new String[]{}));
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (pgm.isBusy() || id.isBusy()) {
			return true;
		}
		for(Scannable s : scannables) {
			if (s.isBusy()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		pgm.asynchronousMoveTo(externalPosition);
		id.asynchronousMoveTo(new Object[] {externalPosition, null});
		for(Scannable s : scannables) {
			s.asynchronousMoveTo(externalPosition);
		}
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		Object[] pos = new Object[scannables.size()+1];
		int i = 0;
		pos[i] = pgm.getPosition();
		for (Scannable s : scannables) {
			i++;
			pos[i] = s.getPosition();
		}
		return pos;
	}
	
	public Scannable getPgm() {
		return pgm;
	}

	public void setPgm(Scannable pgm) {
		this.pgm = pgm;
	}

	public I05Apple getId() {
		return id;
	}

	public void setId(I05Apple id) {
		this.id = id;
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
}