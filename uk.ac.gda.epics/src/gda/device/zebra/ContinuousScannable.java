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

package gda.device.zebra;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.factory.FactoryException;
import gda.observable.IObserver;

public class ContinuousScannable implements ContinuouslyScannableViaController {
	private Scannable delegate;
	private boolean operatingContinuously=false;
	private ContinuousMoveController continuousMoveController;
	private Object posAtScanStart;

	@Override
	public void setName(String name) {
		delegate.setName(name);
	}

	@Override
	public void reconfigure() throws FactoryException {
		delegate.reconfigure();
	}

	@Override
	public Object getPosition() throws DeviceException {
		return posAtScanStart;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public void addIObserver(IObserver observer) {
		delegate.addIObserver(observer);
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		delegate.setAttribute(attributeName, value);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		delegate.deleteIObserver(observer);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public void deleteIObservers() {
		delegate.deleteIObservers();
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return delegate.getAttribute(attributeName);
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		delegate.moveTo(position);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		delegate.asynchronousMoveTo(position);
	}

	@Override
	public void close() throws DeviceException {
		delegate.close();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
		delegate.setProtectionLevel(newLevel);
	}

	@Override
	public String checkPositionValid(Object position) throws DeviceException {
		return delegate.checkPositionValid(position);
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return delegate.getProtectionLevel();
	}

	@Override
	public void stop() throws DeviceException {
		delegate.stop();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return delegate.isBusy();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		return;
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return delegate.isAt(positionToTest);
	}

	@Override
	public void setLevel(int level) {
		delegate.setLevel(level);
	}

	@Override
	public int getLevel() {
		return delegate.getLevel();
	}

	@Override
	public String[] getInputNames() {
		return delegate.getInputNames();
	}

	@Override
	public void setInputNames(String[] names) {
		delegate.setInputNames(names);
	}

	@Override
	public String[] getExtraNames() {
		return delegate.getExtraNames();
	}

	@Override
	public void setExtraNames(String[] names) {
		delegate.setExtraNames(names);
	}

	@Override
	public void setOutputFormat(String[] names) {
		delegate.setOutputFormat(names);
	}

	@Override
	public String[] getOutputFormat() {
		return delegate.getOutputFormat();
	}

	@Override
	public void atStart() throws DeviceException {
		delegate.atStart();
	}

	@Override
	public void atEnd() throws DeviceException {
		delegate.atEnd();
	}

	@Override
	public void atScanStart() throws DeviceException {
		delegate.atScanStart();
		posAtScanStart = delegate.getPosition();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		delegate.atScanEnd();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		delegate.atScanLineStart();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		delegate.atScanLineEnd();
	}

	@Override
	public void atPointStart() throws DeviceException {
		delegate.atPointStart();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		delegate.atPointEnd();
	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		delegate.atLevelMoveStart();
	}
	
	@Override
	public void atLevelStart() throws DeviceException {
		delegate.atLevelStart();
	}
	
	@Override
	public void atLevelEnd() throws DeviceException {
		delegate.atLevelEnd();
	}


	@Override
	public void atCommandFailure() throws DeviceException {
		delegate.atCommandFailure();
	}

	@Override
	public String toFormattedString() {
		return delegate.toFormattedString();
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		operatingContinuously = b;
	}

	@Override
	public boolean isOperatingContinously() {
		return operatingContinuously;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return continuousMoveController;
	}

	public Scannable getDelegate() {
		return delegate;
	}

	public void setDelegate(Scannable delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController continuousMoveController) {
		this.continuousMoveController = continuousMoveController;
	}

}
