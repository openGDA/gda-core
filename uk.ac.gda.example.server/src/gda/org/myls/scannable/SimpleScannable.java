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

package gda.org.myls.scannable;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;

/**
 *
 */
@CorbaImplClass(ScannableImpl.class)
@CorbaAdapterClass(ScannableAdapter.class)
public class SimpleScannable extends ScannableBase {
	String name;
	Object position;
	String[] units;

	/**
	 *
	 */
	public SimpleScannable() {
	}

	/**
	 * @param name
	 * @param position
	 * @param inputNames
	 * @param extraNames
	 * @param level
	 * @param outputFormat
	 * @param units
	 */
	public SimpleScannable(String name, Object position, String[] inputNames,
			String[] extraNames, int level, String[] outputFormat,
			String[] units) {
		this.name = name;
		this.position = position;
		this.inputNames = inputNames;
		this.extraNames = extraNames;
		this.level = level;
		this.outputFormat = outputFormat;
		this.units = units;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#asynchronousMoveTo(java.lang.Object)
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		this.position = position;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#atEnd()
	 */
	@Override
	public void atEnd() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#atPointEnd()
	 */
	@Override
	public void atPointEnd() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#atPointStart()
	 */
	@Override
	public void atPointStart() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#atScanEnd()
	 */
	@Override
	public void atScanEnd() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#atScanLineEnd()
	 */
	@Override
	public void atScanLineEnd() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#atScanLineStart()
	 */
	@Override
	public void atScanLineStart() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#atScanStart()
	 */
	@Override
	public void atScanStart() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#atStart()
	 */
	@Override
	public void atStart() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#getExtraNames()
	 */
	@Override
	public String[] getExtraNames() {
		return extraNames;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#getInputNames()
	 */
	@Override
	public String[] getInputNames() {
		return inputNames;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#getLevel()
	 */
	@Override
	public int getLevel() {
		return level;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#getOutputFormat()
	 */
	@Override
	public String[] getOutputFormat() {
		return outputFormat;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	public Object getPosition() throws DeviceException {
		return position;

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#isAt(java.lang.Object)
	 */
	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return positionToTest.equals(getPosition());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#isBusy()
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#moveTo(java.lang.Object)
	 */
	@Override
	public void moveTo(Object position) throws DeviceException {
		asynchronousMoveTo(position);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#setExtraNames(java.lang.String[])
	 */
	@Override
	public void setExtraNames(String[] names) {
		this.extraNames = names;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#setInputNames(java.lang.String[])
	 */
	@Override
	public void setInputNames(String[] names) {
		this.inputNames = names;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#setLevel(int)
	 */
	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#setOutputFormat(java.lang.String[])
	 */
	@Override
	public void setOutputFormat(String[] names) {
		this.outputFormat = names;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#stop()
	 */
	@Override
	public void stop() throws DeviceException {
}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Scannable#waitWhileBusy()
	 */
	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Device#close()
	 */
	@Override
	public void close() throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Device#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Device#getProtectionLevel()
	 */
	@Override
	public int getProtectionLevel() throws DeviceException {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Device#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String attributeName, Object value)
			throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Device#setProtectionLevel(int)
	 */
	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.factory.Findable#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.factory.Findable#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.observable.IObservable#addIObserver(gda.observable.IObserver)
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.observable.IObservable#deleteIObserver(gda.observable.IObserver)
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.observable.IObservable#deleteIObservers()
	 */
	@Override
	public void deleteIObservers() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.factory.Reconfigurable#reconfigure()
	 */
	@Override
	public void reconfigure() throws FactoryException {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * @param position The position to set.
	 */
	public void setPosition(Object position) {
		this.position = position;
	}

	/**
	 * @return Returns the units.
	 */
	public String[] getUnits() {
		return units;
	}

	/**
	 * @param units The units to set.
	 */
	public void setUnits(String[] units) {
		this.units = units;
	}

}
