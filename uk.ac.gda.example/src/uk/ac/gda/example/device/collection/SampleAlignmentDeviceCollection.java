/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.example.device.collection;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.observable.IObserver;

import java.util.LinkedList;
import java.util.List;

import uk.ac.gda.client.device.collection.IDeviceCollection;

public class SampleAlignmentDeviceCollection implements IDeviceCollection {

	private List<ISampleAlignmentDeviceCollectionListener> listeners = new LinkedList<ISampleAlignmentDeviceCollectionListener>();
	private Scannable scannable;
	private ScannableMotionUnits scannablemotorunits;
	private String name;

	/**
	 * @return Returns the scannable.
	 */
	public Scannable getScannable() {
		return scannable;
	}

	/**
	 * @return Returns the scannablemotorunits.
	 */
	public ScannableMotionUnits getScannablemotorunits() {
		return scannablemotorunits;
	}

	/**
	 * @param scannablemotorunits The scannablemotorunits to set.
	 */
	public void setScannablemotorunits(ScannableMotionUnits scannablemotorunits) {
		this.scannablemotorunits = scannablemotorunits;
	}

	/**
	 * @param scannable The scannable to set.
	 */
	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		
	}

	public void addListener(ISampleAlignmentDeviceCollectionListener collectionListener) {
		if (collectionListener == null)
			throw new NullPointerException();
		
		if (listeners.size() == 0) {
			scannablemotorunits.addIObserver(new IObserver() {
				
				@Override
				public void update(Object source, Object arg) {
					if (source.equals(scannablemotorunits)){
						for (ISampleAlignmentDeviceCollectionListener listener : listeners) {
							try {
								listener.motorYHasMovedTo(((Double)(scannable.getPosition())).doubleValue());
							} catch (DeviceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}	
					
				}
			});
			scannable.addIObserver(new IObserver() {
				
				@Override
				public void update(Object source, Object arg) {
					
					if (source.equals(scannable)){
						for (ISampleAlignmentDeviceCollectionListener listener : listeners) {
							try {
								listener.motorXHasMovedTo(((Double)(scannable.getPosition())).doubleValue());
							} catch (DeviceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				
				}
			});
		}
		
		listeners.add(collectionListener);
	}
}
