/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import gda.observable.IObserver;

/**
 * Wraps and then delegates many method calls to a ScannableMotionUnits instance. If the name, 
 * inputNames or extraNames fields are set in the wrapper, calls to get these will not be
 * delegated (this list may be extended in the future). This class is useful for presenting a user
 * with a Scannable that may operate one of many underlying scannables.
 */
public class ScannableMotionUnitsWrapper extends ScannableMotionUnitsBase {

	private ScannableMotionUnits delegate = null;

	private boolean extraNamesOveridden = false;
	private boolean inputNamesOveridden = false;
	private boolean nameOveridden = false;

	public void setDelegate(ScannableMotionUnits delegate) {
		this.delegate = delegate;
	}

	public ScannableMotionUnits getDelegate() {
		if (delegate == null) {
			throw new IllegalStateException("No delegate Scannable has been set");
		}
		return delegate;
	}

	@Override
	public String[] getExtraNames() {
		return extraNamesOveridden ? super.getExtraNames() : getDelegate().getExtraNames();
	}

	@Override
	public String[] getInputNames() {
		return inputNamesOveridden ? super.getInputNames() : getDelegate().getInputNames();
	}

	@Override
	public String getName() {
		return nameOveridden ? super.getName() : getDelegate().getName();
	}

	@Override
	public void setExtraNames(String[] names) {
		super.setExtraNames(names);
		extraNamesOveridden = true;
	}

	@Override
	public void setInputNames(String[] names) {
		super.setInputNames(names);
		inputNamesOveridden = true;
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		nameOveridden = true;
	}

	@Override
	public void a(Object position) throws DeviceException {
		getDelegate().a(position);
	}

	@Override
	public void addAcceptableUnit(String newUnit) throws DeviceException {
		getDelegate().addAcceptableUnit(newUnit);
	}

	@Override
	public void addIObserver(IObserver observer) {
		getDelegate().addIObserver(observer);
	}

	@Override
	public void ar(Object position) throws DeviceException {
		getDelegate().ar(position);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		getDelegate().asynchronousMoveTo(position);
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		getDelegate().atCommandFailure();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void atEnd() throws DeviceException {
		getDelegate().atEnd();
	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		getDelegate().atLevelMoveStart();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		getDelegate().atPointEnd();
	}

	@Override
	public void atPointStart() throws DeviceException {
		getDelegate().atPointStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		getDelegate().atScanEnd();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		getDelegate().atScanLineEnd();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		getDelegate().atScanLineStart();
	}

	@Override
	public void atScanStart() throws DeviceException {
		getDelegate().atScanStart();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void atStart() throws DeviceException {
		getDelegate().atStart();
	}

	@Override
	public String checkPositionValid(Object position) throws DeviceException {
		return getDelegate().checkPositionValid(position);
	}

	@Override
	public String checkPositionWithinGdaLimits(Double[] externalPosition) {
		return getDelegate().checkPositionWithinGdaLimits(externalPosition);
	}

	@Override
	public String checkPositionWithinGdaLimits(Object externalPosition) {
		return getDelegate().checkPositionWithinGdaLimits(externalPosition);
	}

	@Override
	public void close() throws DeviceException {
		getDelegate().close();
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		getDelegate().deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		getDelegate().deleteIObservers();
	}

	@Override
	public String[] getAcceptableUnits() {
		return getDelegate().getAcceptableUnits();
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return getDelegate().getAttribute(attributeName);
	}

	@Override
	public String getHardwareUnitString() {
		return getDelegate().getHardwareUnitString();
	}

	@Override
	public int getLevel() {
		return getDelegate().getLevel();
	}

	@Override
	public Double[] getLowerGdaLimits() {
		return getDelegate().getLowerGdaLimits();
	}

	@Override
	public int getNumberTries() {
		return getDelegate().getNumberTries();
	}

	@Override
	public Double[] getOffset() {
		return getDelegate().getOffset();
	}

	@Override
	public String[] getOutputFormat() {
		return getDelegate().getOutputFormat();
	}

	@Override
	public Object getPosition() throws DeviceException {
		return getDelegate().getPosition();
	}

	@Override
	public Quantity[] getPositionAsQuantityArray() throws DeviceException {
		return getDelegate().getPositionAsQuantityArray();
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return getDelegate().getProtectionLevel();
	}

	@Override
	public Double[] getScalingFactor() {
		return getDelegate().getScalingFactor();
	}

	@Override
	public Double[] getTolerances() throws DeviceException {
		return getDelegate().getTolerances();
	}

	@Override
	public Double[] getUpperGdaLimits() {
		return getDelegate().getUpperGdaLimits();
	}

	@Override
	public String getUserUnits() {
		return getDelegate().getUserUnits();
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return getDelegate().isAt(positionToTest);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return getDelegate().isBusy();
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		getDelegate().moveTo(position);
	}

	@Override
	public void r(Object position) throws DeviceException {
		getDelegate().r(position);
	}

	@Override
	public void reconfigure() throws FactoryException {
		getDelegate().reconfigure();
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		getDelegate().setAttribute(attributeName, value);
	}

	@Override
	public void setHardwareUnitString(String hardwareUnitString) throws DeviceException {
		getDelegate().setHardwareUnitString(hardwareUnitString);
	}

	@Override
	public void setLevel(int level) {
		getDelegate().setLevel(level);
	}

	@Override
	public void setLowerGdaLimits(Double externalLowerLim) throws Exception {
		getDelegate().setLowerGdaLimits(externalLowerLim);
	}

	@Override
	public void setLowerGdaLimits(Double[] externalLowerLim) throws Exception {
		getDelegate().setLowerGdaLimits(externalLowerLim);
	}

	@Override
	public void setNumberTries(int numberTries) {
		getDelegate().setNumberTries(numberTries);
	}

	@Override
	public void setOffset(Double... offsetArray) {
		getDelegate().setOffset(offsetArray);
	}

	@Override
	public void setOffset(Object offsetPositionInExternalUnits) {
		getDelegate().setOffset(offsetPositionInExternalUnits);
	}

	@Override
	public void setOutputFormat(String[] names) {
		getDelegate().setOutputFormat(names);
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
		getDelegate().setProtectionLevel(newLevel);
	}

	@Override
	public void setScalingFactor(Double... scaleArray) {
		getDelegate().setScalingFactor(scaleArray);
	}

	@Override
	public void setTolerance(Double tolerence) throws DeviceException {
		getDelegate().setTolerance(tolerence);
	}

	@Override
	public void setTolerances(Double[] tolerence) throws DeviceException {
		getDelegate().setTolerances(tolerence);
	}

	@Override
	public void setUpperGdaLimits(Double externalUpperLim) throws Exception {
		getDelegate().setUpperGdaLimits(externalUpperLim);
	}

	@Override
	public void setUpperGdaLimits(Double[] externalUpperLim) throws Exception {
		getDelegate().setUpperGdaLimits(externalUpperLim);
	}

	@Override
	public void setUserUnits(String userUnitsString) throws DeviceException {
		getDelegate().setUserUnits(userUnitsString);
	}

	@Override
	public void stop() throws DeviceException {
		getDelegate().stop();
	}

	@Override
	public String toFormattedString() {
		return getDelegate().toFormattedString();
	}

	@Override
	public String toString() {
		return getDelegate().toString();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		getDelegate().waitWhileBusy();
	}

}
