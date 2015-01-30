/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.scannable;

import org.jscience.physics.quantities.Quantity;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import gda.observable.IObserver;

public class PassthroughScannableMotionUnitsDecorator implements ScannableMotionUnits {

	protected final ScannableMotionUnits delegate;

	public ScannableMotionUnits getDelegate() {
		return delegate;
	}

	public PassthroughScannableMotionUnitsDecorator(ScannableMotionUnits delegate) {
		this.delegate = delegate;
	}

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
		return delegate.getPosition();
	}

	public String getName() {
		return delegate.getName();
	}

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

	public String checkPositionWithinGdaLimits(Double[] externalPosition) {
		return delegate.checkPositionWithinGdaLimits(externalPosition);
	}

	public void deleteIObservers() {
		delegate.deleteIObservers();
	}

	public String getUserUnits() {
		return delegate.getUserUnits();
	}

	public Object getAttribute(String attributeName) throws DeviceException {
		return delegate.getAttribute(attributeName);
	}

	public void moveTo(Object position) throws DeviceException {
		delegate.moveTo(position);
	}

	public void setUserUnits(String userUnitsString) throws DeviceException {
		delegate.setUserUnits(userUnitsString);
	}

	public void asynchronousMoveTo(Object position) throws DeviceException {
		delegate.asynchronousMoveTo(position);
	}

	public String checkPositionWithinGdaLimits(Object externalPosition) {
		return delegate.checkPositionWithinGdaLimits(externalPosition);
	}

	public void close() throws DeviceException {
		delegate.close();
	}

	public void setProtectionLevel(int newLevel) throws DeviceException {
		delegate.setProtectionLevel(newLevel);
	}

	public void setLowerGdaLimits(Double[] externalLowerLim) throws Exception {
		delegate.setLowerGdaLimits(externalLowerLim);
	}

	public String getHardwareUnitString() {
		return delegate.getHardwareUnitString();
	}

	public int getProtectionLevel() throws DeviceException {
		return delegate.getProtectionLevel();
	}

	public void setHardwareUnitString(String hardwareUnitString) throws DeviceException {
		delegate.setHardwareUnitString(hardwareUnitString);
	}

	public void setLowerGdaLimits(Double externalLowerLim) throws Exception {
		delegate.setLowerGdaLimits(externalLowerLim);
	}

	public void stop() throws DeviceException {
		delegate.stop();
	}

	public Double[] getLowerGdaLimits() {
		return delegate.getLowerGdaLimits();
	}

	public boolean isBusy() throws DeviceException {
		return delegate.isBusy();
	}

	public String[] getAcceptableUnits() {
		return delegate.getAcceptableUnits();
	}

	public void waitWhileBusy() throws DeviceException, InterruptedException {
		delegate.waitWhileBusy();
	}

	public void setUpperGdaLimits(Double[] externalUpperLim) throws Exception {
		delegate.setUpperGdaLimits(externalUpperLim);
	}

	public void addAcceptableUnit(String newUnit) throws DeviceException {
		delegate.addAcceptableUnit(newUnit);
	}

	public boolean isAt(Object positionToTest) throws DeviceException {
		return delegate.isAt(positionToTest);
	}

	public Quantity[] getPositionAsQuantityArray() throws DeviceException {
		return delegate.getPositionAsQuantityArray();
	}

	public void setOffset(Object offsetPositionInExternalUnits) {
		delegate.setOffset(offsetPositionInExternalUnits);
	}

	public void setUpperGdaLimits(Double externalUpperLim) throws Exception {
		delegate.setUpperGdaLimits(externalUpperLim);
	}

	public void setLevel(int level) {
		delegate.setLevel(level);
	}

	public int getLevel() {
		return delegate.getLevel();
	}

	public Double[] getUpperGdaLimits() {
		return delegate.getUpperGdaLimits();
	}

	public String[] getInputNames() {
		return delegate.getInputNames();
	}

	public String checkPositionValid(Object position) throws DeviceException {
		return delegate.checkPositionValid(position);
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

	public Double[] getTolerances() throws DeviceException {
		return delegate.getTolerances();
	}

	public void setOutputFormat(String[] names) {
		delegate.setOutputFormat(names);
	}

	public String[] getOutputFormat() {
		return delegate.getOutputFormat();
	}

	public void setTolerance(Double tolerence) throws DeviceException {
		delegate.setTolerance(tolerence);
	}

	public void atStart() throws DeviceException {
		delegate.atStart();
	}

	public void setTolerances(Double[] tolerence) throws DeviceException {
		delegate.setTolerances(tolerence);
	}

	public int getNumberTries() {
		return delegate.getNumberTries();
	}

	public void atEnd() throws DeviceException {
		delegate.atEnd();
	}

	public void atScanStart() throws DeviceException {
		delegate.atScanStart();
	}

	public void atScanEnd() throws DeviceException {
		delegate.atScanEnd();
	}

	public void setNumberTries(int numberTries) {
		delegate.setNumberTries(numberTries);
	}

	public void a(Object position) throws DeviceException {
		delegate.a(position);
	}

	public void ar(Object position) throws DeviceException {
		delegate.ar(position);
	}

	public void r(Object position) throws DeviceException {
		delegate.r(position);
	}

	public void setOffset(Double... offsetArray) {
		delegate.setOffset(offsetArray);
	}

	public void atScanLineStart() throws DeviceException {
		delegate.atScanLineStart();
	}

	public void setScalingFactor(Double... scaleArray) {
		delegate.setScalingFactor(scaleArray);
	}

	public void atScanLineEnd() throws DeviceException {
		delegate.atScanLineEnd();
	}

	public void atPointStart() throws DeviceException {
		delegate.atPointStart();
	}

	public Double[] getOffset() {
		return delegate.getOffset();
	}

	public void atPointEnd() throws DeviceException {
		delegate.atPointEnd();
	}

	public Double[] getScalingFactor() {
		return delegate.getScalingFactor();
	}

	public void atLevelMoveStart() throws DeviceException {
		delegate.atLevelMoveStart();
	}

	public void atLevelStart() throws DeviceException {
		delegate.atLevelStart();
	}

	public void atLevelEnd() throws DeviceException {
		delegate.atLevelEnd();
	}

	public void atCommandFailure() throws DeviceException {
		delegate.atCommandFailure();
	}

	public String toFormattedString() {
		return delegate.toFormattedString();
	}

}
