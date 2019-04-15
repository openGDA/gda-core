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

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import gda.device.DeviceException;
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
	public void configure() throws FactoryException {
		delegate.configure();
	}

	@Override
	public boolean isConfigured() {
		return delegate.isConfigured();
	}

	@Override
	public void reconfigure() throws FactoryException {
		delegate.reconfigure();
	}

	@Override
	public Object getPosition() throws DeviceException {
		return delegate.getPosition();
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
	public String checkPositionWithinGdaLimits(Double[] externalPosition) {
		return delegate.checkPositionWithinGdaLimits(externalPosition);
	}

	@Override
	public void deleteIObservers() {
		delegate.deleteIObservers();
	}

	@Override
	public String getUserUnits() {
		return delegate.getUserUnits();
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
	public void setUserUnits(String userUnitsString) throws DeviceException {
		delegate.setUserUnits(userUnitsString);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		delegate.asynchronousMoveTo(position);
	}

	@Override
	public String checkPositionWithinGdaLimits(Object externalPosition) {
		return delegate.checkPositionWithinGdaLimits(externalPosition);
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
	public void setLowerGdaLimits(Double[] externalLowerLim) throws Exception {
		delegate.setLowerGdaLimits(externalLowerLim);
	}

	@Override
	public String getHardwareUnitString() {
		return delegate.getHardwareUnitString();
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return delegate.getProtectionLevel();
	}

	@Override
	public void setHardwareUnitString(String hardwareUnitString) throws DeviceException {
		delegate.setHardwareUnitString(hardwareUnitString);
	}

	@Override
	public void setLowerGdaLimits(Double externalLowerLim) throws Exception {
		delegate.setLowerGdaLimits(externalLowerLim);
	}

	@Override
	public void stop() throws DeviceException {
		delegate.stop();
	}

	@Override
	public Double[] getLowerGdaLimits() {
		return delegate.getLowerGdaLimits();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return delegate.isBusy();
	}

	@Override
	public String[] getAcceptableUnits() {
		return delegate.getAcceptableUnits();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		delegate.waitWhileBusy();
	}

	@Override
	public void setUpperGdaLimits(Double[] externalUpperLim) throws Exception {
		delegate.setUpperGdaLimits(externalUpperLim);
	}

	@Override
	public void addAcceptableUnit(String newUnit) throws DeviceException {
		delegate.addAcceptableUnit(newUnit);
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return delegate.isAt(positionToTest);
	}

	@Override
	public Amount<? extends Quantity>[] getPositionAsQuantityArray() throws DeviceException {
		return delegate.getPositionAsQuantityArray();
	}

	@Override
	public void setOffset(Object offsetPositionInExternalUnits) {
		delegate.setOffset(offsetPositionInExternalUnits);
	}

	@Override
	public void setUpperGdaLimits(Double externalUpperLim) throws Exception {
		delegate.setUpperGdaLimits(externalUpperLim);
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
	public Double[] getUpperGdaLimits() {
		return delegate.getUpperGdaLimits();
	}

	@Override
	public String[] getInputNames() {
		return delegate.getInputNames();
	}

	@Override
	public String checkPositionValid(Object position) throws DeviceException {
		return delegate.checkPositionValid(position);
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
	public Double[] getTolerances() throws DeviceException {
		return delegate.getTolerances();
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
	public void setTolerance(Double tolerence) throws DeviceException {
		delegate.setTolerance(tolerence);
	}

	@Override
	public void atStart() throws DeviceException {
		delegate.atStart();
	}

	@Override
	public void setTolerances(Double[] tolerence) throws DeviceException {
		delegate.setTolerances(tolerence);
	}

	@Override
	public int getNumberTries() {
		return delegate.getNumberTries();
	}

	@Override
	public void atEnd() throws DeviceException {
		delegate.atEnd();
	}

	@Override
	public void atScanStart() throws DeviceException {
		delegate.atScanStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		delegate.atScanEnd();
	}

	@Override
	public void setNumberTries(int numberTries) {
		delegate.setNumberTries(numberTries);
	}

	@Override
	public void a(Object position) throws DeviceException {
		delegate.a(position);
	}

	@Override
	public void ar(Object position) throws DeviceException {
		delegate.ar(position);
	}

	@Override
	public void r(Object position) throws DeviceException {
		delegate.r(position);
	}

	@Override
	public void setOffset(Double... offsetArray) {
		delegate.setOffset(offsetArray);
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		delegate.atScanLineStart();
	}

	@Override
	public void setScalingFactor(Double... scaleArray) {
		delegate.setScalingFactor(scaleArray);
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
	public Double[] getOffset() {
		return delegate.getOffset();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		delegate.atPointEnd();
	}

	@Override
	public Double[] getScalingFactor() {
		return delegate.getScalingFactor();
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

}
