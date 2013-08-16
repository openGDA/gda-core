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

	Scannable delegate;
	private boolean operatingContinuously=false;
	private ContinuousMoveController continuousMoveController;
	private Object posAtScanStart;
	

	@Override
	public void setName(String name) {
		delegate.setName(name);
	}

	public void reconfigure() throws FactoryException {
		delegate.reconfigure();
	}

	public Object getPosition() throws DeviceException {
		return posAtScanStart;
	}

	public String getName() {
		return delegate.getName();
	}

	@Override
	public void addIObserver(IObserver observer) {
		delegate.addIObserver(observer);
	}

	public void setAttribute(String attributeName, Object value) throws DeviceException {
		delegate.setAttribute(attributeName, value);
	}

	public void deleteIObserver(IObserver observer) {
		delegate.deleteIObserver(observer);
	}

	public String toString() {
		return delegate.toString();
	}

	public void deleteIObservers() {
		delegate.deleteIObservers();
	}

	public Object getAttribute(String attributeName) throws DeviceException {
		return delegate.getAttribute(attributeName);
	}

	public void moveTo(Object position) throws DeviceException {
		delegate.moveTo(position);
	}

	public void asynchronousMoveTo(Object position) throws DeviceException {
		delegate.asynchronousMoveTo(position);
	}

	public void close() throws DeviceException {
		delegate.close();
	}

	public void setProtectionLevel(int newLevel) throws DeviceException {
		delegate.setProtectionLevel(newLevel);
	}

	public String checkPositionValid(Object position) throws DeviceException {
		return delegate.checkPositionValid(position);
	}

	public int getProtectionLevel() throws DeviceException {
		return delegate.getProtectionLevel();
	}

	public void stop() throws DeviceException {
		delegate.stop();
	}

	public boolean isBusy() throws DeviceException {
		return delegate.isBusy();
	}

	public void waitWhileBusy() throws DeviceException, InterruptedException {
		return;
	}

	public boolean isAt(Object positionToTest) throws DeviceException {
		return delegate.isAt(positionToTest);
	}

	public void setLevel(int level) {
		delegate.setLevel(level);
	}

	public int getLevel() {
		return delegate.getLevel();
	}

	public String[] getInputNames() {
		return delegate.getInputNames();
	}

	public void setInputNames(String[] names) {
		delegate.setInputNames(names);
	}

	public String[] getExtraNames() {
		return delegate.getExtraNames();
	}

	public void setExtraNames(String[] names) {
		delegate.setExtraNames(names);
	}

	public void setOutputFormat(String[] names) {
		delegate.setOutputFormat(names);
	}

	public String[] getOutputFormat() {
		return delegate.getOutputFormat();
	}

	public void atStart() throws DeviceException {
		delegate.atStart();
	}

	public void atEnd() throws DeviceException {
		delegate.atEnd();
	}

	public void atScanStart() throws DeviceException {
		delegate.atScanStart();
		posAtScanStart = delegate.getPosition();
	}

	public void atScanEnd() throws DeviceException {
		delegate.atScanEnd();
	}

	public void atScanLineStart() throws DeviceException {
		delegate.atScanLineStart();
	}

	public void atScanLineEnd() throws DeviceException {
		delegate.atScanLineEnd();
	}

	public void atPointStart() throws DeviceException {
		delegate.atPointStart();
	}

	public void atPointEnd() throws DeviceException {
		delegate.atPointEnd();
	}

	public void atLevelMoveStart() throws DeviceException {
		delegate.atLevelMoveStart();
	}
	
	public void atLevelStart() throws DeviceException {
		delegate.atLevelStart();
	}

	public void atCommandFailure() throws DeviceException {
		delegate.atCommandFailure();
	}

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

	public void setContinuousMoveController(ContinuousMoveController continuousMoveController) {
		this.continuousMoveController = continuousMoveController;
	}

}
