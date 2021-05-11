/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable.scannablegroup;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.component.PositionValidator;
import gda.factory.FactoryException;
import gda.observable.IObserver;
/**
 * Works with the ICoordinateChildScannable interface to trigger a coordinated move.
 */
interface ICoordinatedParent {

	/**
	 * @return True if the group is in targeting mode. (Elements to move have been added, but not all targets have been
	 *         set, therefore the move has not yet been triggered.)
	 */
	boolean isTargeting();

	/**
	 * Puts the group into targeting mode if it is not already, and tells the group to await for a setElementTarget()
	 * call from that element before triggering the coordinated move.
	 *
	 * @param childScannable
	 */
	void addChildToMove(ICoordinatedChildScannable childScannable);

	/**
	 * Set the target for a given element. addElementToMove() must first have been called.
	 *
	 * @param childScannable
	 * @param position
	 * @throws DeviceException
	 */
	void setChildTarget(ICoordinatedChildScannable childScannable, Object position)
			throws DeviceException;

	/**
	 * Get the position for a given element
	 *
	 * @param childScannable
	 * @return current position (used
	 * @throws DeviceException
	 */
	Object getPositionWhileMovingContinuousely(ICoordinatedScannableGroupChildScannable childScannable) throws DeviceException;

}


interface ICoordinatedParentScannable extends ICoordinatedParent, Scannable {
}

interface ICoordinatedParentContinuouslyScannable extends ICoordinatedParentScannable, ContinuouslyScannableViaController {
}


/**
 * Extends ScannableGroup to provide coordinated moves. When the group's members are moved via the scan or pos command
 * (see below for mechanism), the move requests will be stored up and sent via a single call to the group's
 * asynchronousMoveTo() method.
 * <p>
 * Note that even though the group may be in the internal state TARGETING, it is still possible that some of the
 * components may have been asked to move externally, and that they may be moving, i.e. the group performs no locking.
 * <p>
 * Members passed in to be added to the group will be wrapped in one of CoordinatedElementScannableMotionUnits,
 * CoordinatedElementScannableMotion or CoordinatedElementScannable determined by the interface they support. This means
 * that any methods not in one of these interfaces will not be accessible.
 */

public class CoordinatedScannableGroup extends ScannableGroup implements ICoordinatedParentScannable {

	protected CoordinatedParentScannableComponent coordinatedScannableComponent;

	private LinkedHashMap<String, PositionValidator> additionalPositionValidators = new LinkedHashMap<String, PositionValidator>();

	/**
	 * Create the group. Members must be added for the group to do anything useful.
	 */
	public CoordinatedScannableGroup() {
		super();
		coordinatedScannableComponent = new CoordinatedParentScannableComponent(this);
	}
	// /// ICoordinatedScannableGroup interface /////

	@Override
	public String checkPositionValid(Object pos) throws DeviceException {
		String msg = super.checkPositionValid(pos); // Checks with each motor who's position element is non-null
		if (msg !=null) {
			return msg;
		}
		return checkAdditionalPositionValidators(pos); // Checks with each motor who's position element is non-null
	}

	@Override
	public void setGroupMembers(List<Scannable> groupMembers) throws FactoryException {
		ArrayList<Scannable> wrappedGroupMembers = new ArrayList<Scannable>();
		for (Scannable scannable : groupMembers) {
			wrappedGroupMembers.add(wrapScannable(scannable));
		}
		super.setGroupMembers(wrappedGroupMembers);
		setMembersInCoordinatedScannableComponent();
	}

	@Override
	public void addGroupMember(Scannable groupMember) throws FactoryException {
		super.addGroupMember(wrapScannable(groupMember));
		setMembersInCoordinatedScannableComponent();
	}

	private void setMembersInCoordinatedScannableComponent() {
		List<ICoordinatedChildScannable> coordinated = new ArrayList<ICoordinatedChildScannable>();
		for (Scannable scn : getGroupMembersAsArray()) {
			coordinated.add((ICoordinatedChildScannable) scn);

		}
		coordinatedScannableComponent.setMembers(coordinated);
	}

	/**
	 * Wraps a scannable with an ICoordinatedScannableElement to mediates access to the 'real' delegate scannable.
	 * coordinated moves. Note that only methods on ScannableMotionUnits, ScannableMotion or Scannable will be brought
	 * out.
	 * @param delegate
	 * @return the wrapped scannable
	 */
	protected ICoordinatedChildScannable wrapScannable(Scannable delegate) {
		if (ScannableMotionUnits.class.isAssignableFrom(delegate.getClass())) {
			return new CoordinatedChildScannableMotionUnits((ScannableMotionUnits) delegate, this);
		} else if (ScannableMotion.class.isAssignableFrom(delegate.getClass())) {
			return new CoordinatedChildScannableMotion((ScannableMotion) delegate, this);
		}
		// else it is at least Scannable
		return new CoordinatedChildScannable(delegate, this);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		String msg = checkPositionValid(position);
		if (msg != null) {
			String posRepresentation;
			try {
				posRepresentation = representPositionToUser(PositionConvertorFunctions.toDoubleArray(position));
			} catch (IllegalArgumentException e) {
				posRepresentation = position.toString();
			}
			throw new DeviceException(MessageFormat.format(
					"Did not move {0} to {1} because:\n{2}",
					getName(), posRepresentation, msg));
		}
		super.asynchronousMoveTo(position);
	}

	private String representPositionToUser(Double[] position) {
		StringBuilder sb= new StringBuilder();
		for (int i = 0; i < position.length; i++) {
			if (position[i] != null) {
				sb.append(getGroupMembers().get(i).getName() + " = " + position[i] + ",");
			}
		}
		if (sb.length() > 0){
			sb.delete(sb.lastIndexOf(","), sb.length());
		}
		return sb.toString();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		for (Scannable member : groupMembers) {
			if (((ICoordinatedScannableGroupChildScannable)member).physicalIsBusy()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.stop();
		}
		coordinatedScannableComponent.resetState();
	}
	// Delegate to coordinatedScannableComponent

	@Override
	public void atLevelMoveStart() {
		// Do Nothing - override default behaviour to prevent targeting on a full group move
	}

	@Override
	public void addChildToMove(ICoordinatedChildScannable element) {
		coordinatedScannableComponent.addChildToMove(element);
	}

	@Override
	public boolean isTargeting() {
		return coordinatedScannableComponent.isTargeting();
	}

	@Override
	public void setChildTarget(ICoordinatedChildScannable element, Object position) throws DeviceException {
		coordinatedScannableComponent.setChildTarget(element, position);
	}

	@Override
	public Object getPositionWhileMovingContinuousely(ICoordinatedScannableGroupChildScannable childScannable) throws DeviceException {
		int index = groupMembers.indexOf(childScannable);
		return PositionConvertorFunctions.toObjectArray(getPosition())[index];
	}

	//
	//TODO: This class should be made to extend ScannableMotion and this functionality moved out into
	//      a generic 'additional-validator' (However this is quite involved!). RobW
	protected String checkAdditionalPositionValidators(Object externalPos) throws DeviceException {

		if (getAdditionalPositionValidators().size() < 1) {
			return null;
		}
		Double[] externalPositionArray = PositionConvertorFunctions.toDoubleArray(externalPos);
		Double[] internalPositionArray = new Double[externalPositionArray.length];

		// check there is one element per (assumed to be single input) scannable group member
		if (externalPositionArray.length != getGroupMembers().size()) {
			throw new DeviceException("Position does not have correct number of fields. Expected = " + getGroupMembers().size()
					+ " actual = " + externalPositionArray.length + " position= " + externalPos.toString());
		}
		// Replace any nulls with corresponding scannables current position
		for (int i = 0; i < externalPositionArray.length; i++) {
			if (externalPositionArray[i] == null) {
				externalPositionArray[i] = PositionConvertorFunctions.toDouble(getGroupMembers().get(i).getPosition());
			}
		}

		// change to internal positions
		for (int i = 0; i < externalPositionArray.length; i++) {
			ScannableBase physicalScannable = (ScannableBase) ((ICoordinatedScannableGroupChildScannable) getGroupMembers().get(i)).getPhysicalScannable();
			internalPositionArray[i] = PositionConvertorFunctions.toDouble(physicalScannable.externalToInternal(externalPositionArray[i]));
		}

		// perform the check
		for (PositionValidator validator : additionalPositionValidators.values()) {
			String msg = validator.checkInternalPosition(internalPositionArray);
			if (msg != null) {
				return msg;
			}
		}

		return null;
	}

	/**
	 * Set the additional position validators. An ordered map is used so that the validators
	 * can be compared in the same order each time. NOTE: Spring uses ordered maps by default when
	 * injecting maps.
	 * @param additionalPositionValidators
	 */
	public void setAdditionalPositionValidators(LinkedHashMap<String, PositionValidator> additionalPositionValidators) {
		this.additionalPositionValidators = additionalPositionValidators;
	}

	/**
	 * Validators will be tested in the order that they have been added.
	 * @param name
	 * @param validator
	 */
	public void addPositionValidator(String name, PositionValidator validator) {
		additionalPositionValidators.put(name, validator);
	}

	public LinkedHashMap<String, PositionValidator> getAdditionalPositionValidators() {
		return additionalPositionValidators;
	}

	@Override
	public String __str__() {
		String output = getName() + " :: (collision avoidance rules)\n";
		for (Entry<String, PositionValidator> validatorEntry : getAdditionalPositionValidators().entrySet()) {
			if (!validatorEntry.getValue().equals("")){
				output += "* " + validatorEntry.getValue().toString() + "\n";
			}
		}
		output += "\n";
		output += toFormattedString();
//		getName() + " ::\n";
//		for (Scannable member : groupMembers) {
//			output += member.toFormattedString() + "\n";
//		}
		return output.trim();
	}

	@Override
	public String __repr__() {
		return __str__();
	}

}


/**
 * A component delegated to by classes that implement ICoordinatedScannable. Make sure to call resetState() if the
 * scannable calling this delegate receives an atCommandFailure.
 */
class CoordinatedParentScannableComponent implements ICoordinatedParent {

	private Map<ICoordinatedChildScannable, Object> targetMap; // existence indicates targeting
	public Scannable delegator;
	private List<ICoordinatedChildScannable> children = null;

	/**
	 * @param delegator
	 *            The ICoordinated object that is using this component.
	 */
	public CoordinatedParentScannableComponent(Scannable delegator) {
		this.delegator = delegator;
	}

	/**
	 * Must be called with the list of ICoordinatedScannableElements that map to the fields in the containing scannable.
	 *
	 * @param members
	 */
	public void setMembers(List<ICoordinatedChildScannable> members) {
		this.children = members;
	}

	/**
	 * Must be called atCommandFailure. Empties the target and moves component/Scannable out of targeting mode.
	 */
	public void resetState() {
		targetMap = null;
	}

	@Override
	public boolean isTargeting() {
		return (targetMap != null);
	}

	@Override
	public void addChildToMove(ICoordinatedChildScannable element) {
		if (targetMap == null) {
			targetMap = new HashMap<ICoordinatedChildScannable, Object>();
		}
		targetMap.put(element, null);
	}

	@Override
	public void setChildTarget(ICoordinatedChildScannable element, Object position) throws DeviceException {
		assert (targetMap.containsKey(element));
		assert (targetMap.get(element) == null);
		assert (position != null);
		targetMap.put(element, position);
		if (isTargetMapFull()) {
			startMove();
		}
	}

	/**
	 * Do not use this method, as the component does not know about the group. // TODO: flakey
	 */
	@Override
	public Object getPositionWhileMovingContinuousely(ICoordinatedScannableGroupChildScannable childScannable) throws DeviceException {
		throw new RuntimeException("CoordinatedParentScannableComponent cannot be deferred to to provde positions");
	}

	private boolean isTargetMapFull() {
		for (ICoordinatedChildScannable key : targetMap.keySet()) {
			if (targetMap.get(key) == null) {
				return false;
			}
		}
		return true;
	}

	private void startMove() throws DeviceException {
		assert (targetMap != null);
		ArrayList<Double> target = new ArrayList<Double>();
		for (ICoordinatedChildScannable member : children) {
			if (member.isInputField()){
				target.add(PositionConvertorFunctions.toDouble(targetMap.get(member)));
			}
		}
		try {
			resetState();
			delegator.asynchronousMoveTo(target.toArray(new Double[] {}));
		} catch (DeviceException e) {
			delegator.atCommandFailure();
			throw e;
		}
	}

}


/**
 * Works with ICoordinatedScannableGroup to trigger a coordinated move. A number of these must be set as the members
 * of the parent ICoordinatedScannableGroup.
 */
interface ICoordinatedChildScannable extends Scannable {

	// /// Overridden behaviours from Scannable /////
	/**
	 * If the parent is targeting calls parent.setElementTarget(), otherwise triggers the move.
	 *
	 * @param position
	 * @throws DeviceException if move threw one, or if this is an input field.
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException;

	/**
	 * Checks if the parent is busy
	 */
	@Override
	public boolean isBusy() throws DeviceException;

	/**
	 * Adds the element to the parent's list of elements to move using parent.addElementToMove()
	 */
	@Override
	public void atLevelMoveStart() throws DeviceException;

	/**
	 * Calls atCommandFailure() on the parent.
	 */
	@Override
	public void atCommandFailure() throws DeviceException;

	// /// New methods /////

	/**
	 * @return true if this ICoordinatedScannableElement represents a (movable) input field, or false if it represents
	 *         a (read-only) output field.
	 */
	public boolean isInputField();

	/**
	 * Sets the element's parent
	 *
	 * @param parent
	 *            the parent ICoordinatedScannable
	 */
	public void setParent(ICoordinatedParent parent);

	/**
	 * Gets the element's parent
	 *
	 * @return the parent CoordinatedScannableGroup
	 */
	public ICoordinatedParent getParent();


}


interface ICoordinatedScannableGroupChildScannable extends ICoordinatedChildScannable {

	/**
	 * Returns true if the wrapped scannable is busy. Required as the wrapped scannable's isBusy() is always
	 * delegated to the parent.
	 * @return true
	 * @throws DeviceException
	 */
	public boolean physicalIsBusy() throws DeviceException;

	/**
	 * Gets the scannable wrapped by the child
	 * @return scannable
	 */
	public Scannable getPhysicalScannable();

	/**
	 * Moves the scannable wrapped by child
	 * @param position
	 * @throws DeviceException
	 */
	public void physicalAsynchronousMoveTo(Object position) throws DeviceException;
}


/**
 * Wraps a Scannable to make it work with an ICoordinatedScannableGroup. Methods not in Scannable will be hidden.
 */
class CoordinatedChildScannable extends ScannableBase implements ICoordinatedScannableGroupChildScannable {

	// ScannableBAse provides Jython functionality, e.g. __call__

	protected ICoordinatedParentScannable group;
	protected Scannable delegate;

	/**
	 * @param delegate
	 * @param group
	 */
	public CoordinatedChildScannable(Scannable delegate, ICoordinatedParentScannable group) {
		this.delegate = delegate;
		this.group = group;
	}

	// /// ICoordinatedScannableElement interface /////

	@Override
	public void setParent(ICoordinatedParent group) {
		this.group = (ICoordinatedParentScannable) group;
	}

	@Override
	public ICoordinatedParent getParent() {
		return group;
	}

	@Override
	public boolean physicalIsBusy() throws DeviceException {
		return delegate.isBusy();
	}

	@Override
	public Scannable getPhysicalScannable() {
		return delegate;
	}

	@Override
	public void physicalAsynchronousMoveTo(Object position) throws DeviceException {
		delegate.asynchronousMoveTo(position);

	}

	// /// Scannable interface methods not directly delegated /////

	@Override
	public void atLevelMoveStart() throws DeviceException {
		group.addChildToMove(this);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (group.isTargeting()) {
			group.setChildTarget(this, position);
		} else {
			delegate.asynchronousMoveTo(position);
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return group.isBusy();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		group.atCommandFailure();
	}

	// /// Directly delegated Scannable interface methods /////

	@Override
	public void addIObserver(IObserver anIObserver) {
		delegate.addIObserver(anIObserver);
	}

	@Override
	public void atPointEnd() throws DeviceException {
		delegate.atPointEnd();
	}

	@Override
	public void atPointStart() throws DeviceException {
		delegate.atPointStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		delegate.atScanEnd();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		delegate.atScanLineEnd();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		delegate.atScanLineStart();
	}

	@Override
	public void atScanStart() throws DeviceException {
		delegate.atScanStart();
	}

	@Override
	public void close() throws DeviceException {
		delegate.close();
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		delegate.deleteIObserver(anIObserver);
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
	public String[] getExtraNames() {
		return delegate.getExtraNames();
	}

	@Override
	public String[] getInputNames() {
		return delegate.getInputNames();
	}

	@Override
	public int getLevel() {
		return delegate.getLevel();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String[] getOutputFormat() {
		return delegate.getOutputFormat();
	}

	@Override
	public Object getPosition() throws DeviceException {
		return delegate.getPosition();
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return delegate.getProtectionLevel();
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return delegate.isAt(positionToTest);
	}

	@Override
	public String checkPositionValid(Object position) throws DeviceException {
		return delegate.checkPositionValid(position);
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		delegate.moveTo(position);
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
	public boolean isConfigureAtStartup() {
		return delegate.isConfigureAtStartup();
	}

	@Override
	public void reconfigure() throws FactoryException {
		delegate.reconfigure();
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		delegate.setAttribute(attributeName, value);
	}

	@Override
	public void setExtraNames(String[] names) {
		delegate.setExtraNames(names);
	}

	@Override
	public void setInputNames(String[] names) {
		delegate.setInputNames(names);
	}

	@Override
	public void setLevel(int level) {
		delegate.setLevel(level);
	}

	@Override
	public void setName(String name) {
		delegate.setName(name);
	}

	@Override
	public void setOutputFormat(String[] names) {
		delegate.setOutputFormat(names);
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
		delegate.setProtectionLevel(newLevel);
	}

	@Override
	public void stop() throws DeviceException {
		delegate.stop();
	}

	@Override
	public String toFormattedString() {
		return delegate.toFormattedString();
	}

	@Override
	public String toString() {
		return delegate.toFormattedString();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		delegate.waitWhileBusy();
	}

	@Override
	public boolean isInputField() {
		return true;
	}

}

/**
 * Wraps a ScannableMotion to make it work with an ICoordinatedScannableGroup. Methods not in ScannableMotion will be
 * hidden.
 */
class CoordinatedChildScannableMotion extends CoordinatedChildScannable implements ScannableMotion {
	private ScannableMotion scannableMotionDelegate;

	/**
	 * @param delegate
	 * @param group
	 */
	public CoordinatedChildScannableMotion(ScannableMotion delegate, ICoordinatedParentScannable group) {
		super(delegate, group);
		scannableMotionDelegate = delegate;
	}

	@Override
	public String checkPositionValid(Object position) throws DeviceException {
		return scannableMotionDelegate.checkPositionValid(position);
	}

	@Override
	public String checkPositionWithinGdaLimits(Double[] pos) {
		return scannableMotionDelegate.checkPositionWithinGdaLimits(pos);
	}

	@Override
	public String checkPositionWithinGdaLimits(Object illDefinedPosObject) {
		return scannableMotionDelegate.checkPositionWithinGdaLimits(illDefinedPosObject);
	}

	@Override
	public Double[] getLowerGdaLimits() {
		return scannableMotionDelegate.getLowerGdaLimits();
	}

	@Override
	public int getNumberTries() {
		return scannableMotionDelegate.getNumberTries();
	}

	@Override
	public Double[] getTolerances() throws DeviceException {
		return scannableMotionDelegate.getTolerances();
	}

	@Override
	public Double[] getUpperGdaLimits() {
		return scannableMotionDelegate.getUpperGdaLimits();
	}

	@Override
	public void setLowerGdaLimits(Double lowerLim) throws Exception {
		scannableMotionDelegate.setLowerGdaLimits(lowerLim);
	}

	@Override
	public void setLowerGdaLimits(Double[] lowerLim) throws Exception {
		scannableMotionDelegate.setLowerGdaLimits(lowerLim);
	}

	@Override
	public void setNumberTries(int numberTries) {
		scannableMotionDelegate.setNumberTries(numberTries);
	}

	@Override
	public void setTolerance(Double tolerence) throws DeviceException {
		scannableMotionDelegate.setTolerance(tolerence);
	}

	@Override
	public void setTolerances(Double[] tolerence) throws DeviceException {
		scannableMotionDelegate.setTolerances(tolerence);
	}

	@Override
	public void setUpperGdaLimits(Double upperLim) throws Exception {
		scannableMotionDelegate.setUpperGdaLimits(upperLim);
	}

	@Override
	public void setUpperGdaLimits(Double[] upperLim) throws Exception {
		scannableMotionDelegate.setUpperGdaLimits(upperLim);
	}

	@Override
	public void a(Object position) throws DeviceException {
		asynchronousMoveTo(position);

	}

	@Override
	public void ar(Object position) throws DeviceException {
		scannableMotionDelegate.ar(position);
	}

	@Override
	public void r(Object position) throws DeviceException {
		scannableMotionDelegate.r(position);
	}

	@Override
	public Double[] getOffset() {
		return scannableMotionDelegate.getOffset();
	}

	@Override
	public Double[] getScalingFactor() {
		return scannableMotionDelegate.getScalingFactor();
	}

	@Override
	public void setOffset(Double... offsetArray) {
		scannableMotionDelegate.setOffset(offsetArray);
	}

	@Override
	public void setScalingFactor(Double... scaleArray) {
		scannableMotionDelegate.setScalingFactor(scaleArray);
	}

}

/**
 * Wraps a ScannableMotionUnits to make it work with an ICoordinatedScannableGroup. Methods not in ScannableMotionUnits
 * will be hidden.
 */
class CoordinatedChildScannableMotionUnits extends CoordinatedChildScannableMotion implements ScannableMotionUnits {
	private ScannableMotionUnits scannableMotionUnitsDelegate;

	/**
	 * @param delegate
	 * @param group
	 */
	public CoordinatedChildScannableMotionUnits(ScannableMotionUnits delegate, ICoordinatedParentScannable group) {
		super(delegate, group);
		scannableMotionUnitsDelegate = delegate;
	}

	@Override
	public void addAcceptableUnit(String newUnit) throws DeviceException {
		scannableMotionUnitsDelegate.addAcceptableUnit(newUnit);
	}

	@Override
	public String[] getAcceptableUnits() {
		return scannableMotionUnitsDelegate.getAcceptableUnits();
	}

	@Override
	public String getHardwareUnitString() {
		return scannableMotionUnitsDelegate.getHardwareUnitString();
	}

	@Override
	public String getUserUnits() {
		return scannableMotionUnitsDelegate.getUserUnits();
	}

	@Override
	public void setHardwareUnitString(String hardwareUnitString) throws DeviceException {
		scannableMotionUnitsDelegate.setHardwareUnitString(hardwareUnitString);
	}

	@Override
	public void setUserUnits(String userUnitsString) throws DeviceException {
		scannableMotionUnitsDelegate.setUserUnits(userUnitsString);
	}

	@Override
	public void setOffset(Object offsetPositionInExternalUnits) {
		scannableMotionUnitsDelegate.setOffset(offsetPositionInExternalUnits);

	}
}

