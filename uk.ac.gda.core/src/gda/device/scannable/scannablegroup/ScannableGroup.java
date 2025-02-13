/*-
 * Copyright © 2009-2013 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.python.core.Py;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A logical group of Scannables that allows multiple Scannables to be moved at the same time, or allows further
 * validation for movement, e.g. {@link MotomanRobotScannableGroup}'s validation on simultaneous KTheta, KPhi movement.
 *
 * inputNames, extraNames and outputFormat are taken from the constituent Scannables, not maintained as a field of the
 * Group.
 *
 * A ScannableGroup is a logical group of Scannables that can be created through Spring instantiation or by adding
 * Scannables from the Jython console.
 *
 * Configuring a ScannableGroup configures all of its component Scannables and adds itself as an IObserver, and the
 * default behaviour of adding a Scannable to an already configured ScannableGroup is to configure the Scannable
 * (although it can instead un-configure the group, allowing it to be configured again).
 *
 * ScannableGroups can add, remove or set Scannables by using the Scannables, and additionally can remove Scannables by
 * their index.
 *
 * See also {@link ScannableGroupNamed}, which additionally uses the Finder to manage Scannables by name, a
 * former function of this class
 */
@ServiceInterface(IScannableGroup.class)
public class ScannableGroup extends ScannableBase implements IScannableGroup, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(ScannableGroup.class);

	/**
	 * If true, when this group is moved it will demand the current (readback) position of any scannable which does not
	 * have a position demanded.  This will not cause the scannable to move but avoids some bugs related to deferred
	 * moves (see I07-559).
	 */
	private boolean demandAllScannablePositions = false;

	// the list of members
	ArrayList<Scannable> groupMembers = new ArrayList<>();

	public ScannableGroup() {
	}

	/**
	 * Constructor.
	 *
	 * @param name
	 * @param groupMembers
	 * @throws FactoryException
	 */
	public ScannableGroup(String name, Scannable[] groupMembers) throws FactoryException {
		this(name);
		setGroupMembersWithArray(groupMembers);
	}

	public ScannableGroup(String name, List<Scannable> groupMembers) throws FactoryException {
		this(name);
		setGroupMembers(groupMembers);
	}

	public ScannableGroup(String name) {
		setName(name);
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}

		logger.info("{} configuring...", getName());
		// configure all members
		for (Scannable scannable : groupMembers) {
			scannable.configure();
			scannable.addIObserver(this);
		}

		setConfigured(true);
	}

	@Override
	public String[] getGroupMemberNames() {
		return groupMembers.stream().map(Scannable::getName).toArray(String[]::new);
	}

	/**
	 * Adds a scannable to this group. This will not add a Scannable if it is already included
	 * Will configure the Scannable if the ScannableGroup is already configured
	 *
	 * @param groupMember
	 * @throws FactoryException
	 */
	@Override
	public void addGroupMember(Scannable groupMember) throws FactoryException {
		addGroupMember(groupMember, true);
	}

	/**
	 * Adds a scannable to this group. This will not add a Scannable if it is already included
	 *
	 * @param groupMember
	 * @param toConfigure - controls behaviour if the ScannableGroup is already configured:
	 * 			whether to configure the scannable (true)
	 * 			or set the ScannableGroue to be unconfigured (false)
	 * @throws FactoryException
	 */
	@Override
	public void addGroupMember(Scannable groupMember, boolean toConfigure) throws FactoryException {
		if (!groupMembers.contains(groupMember)) {
			groupMembers.add(groupMember);
			if (toConfigure && isConfigured()) {
				groupMember.configure();
				groupMember.addIObserver(this);
			}
			if (!toConfigure) {
				logger.info("{} contains unconfigured member, re-enabling configuring...", getName());
				setConfigured(false);
			}
		} else {
			logger.info(getName() + " will not add Scannable named " + groupMember.getName()
					+ " as it already contains this Scannable.");
		}
	}

	@Override
	public void removeGroupMemberByScannable(Scannable groupMember) {
		groupMember.deleteIObserver(this);
		groupMembers.remove(groupMember);
	}

	/**
	 * @return array of scannable objects in this group
	 */
	@Override
	public Scannable[] getGroupMembersAsArray() {
		return groupMembers.toArray(new Scannable[0]);
	}

	/**
	 * @param name
	 * @return the Scannable of the given name
	 */
	public Scannable getGroupMember(String name) {
		return groupMembers.stream().filter(member -> member.getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Sets the group members that make up this scannable group. If this ScannableGroup is configured, sets it to
	 * unconfigured, to allow all the scannables to be configured by call this.configure()
	 *
	 * @param groupMembers
	 *            the group members
	 * @throws FactoryException
	 */
	@Override
	public void setGroupMembers(List<Scannable> groupMembers) throws FactoryException {
		setGroupMembersWithList(groupMembers, false);
	}

	/**
	 * Sets the group members that make up this scannable group.
	 *
	 * @param groupMembers
	 *            the group members
	 * @throws FactoryException
	 */
	@Override
	public void setGroupMembersWithList(List<Scannable> groupMembers, boolean toConfigure) throws FactoryException {
		for (Scannable groupMember : this.groupMembers) {
			groupMember.deleteIObserver(this);
		}
		this.groupMembers = new ArrayList<>(groupMembers);
		if (toConfigure) {
			for (Scannable groupMember : this.groupMembers) {
				groupMember.addIObserver(this);
				groupMember.configure();
			}
		} else {
			setConfigured(false);
		}
	}

	/**
	 * Sets the members of this group.
	 * <p>
	 * This is final, as for historical reasons there are two setters on here, and it is natural to extend just one.
	 * <p>
	 * See setGroupMembersWithList for configuration behaviour
	 * @param groupMembers
	 * @throws FactoryException
	 */
	@Override
	public final void setGroupMembersWithArray(Scannable[] groupMembers) throws FactoryException {
		setGroupMembers(new ArrayList<Scannable>(Arrays.asList(groupMembers)));
	}

	/**
	 * See __getattr__(String name).
	 **/
	public Object __getattr__(PyString name) {
		return __getattr__(name.internedString());
	}

	/**
	 * Python method used by the interpreter to get attributes that are not defined on an object. Used here to provide
	 * dotted access to member scannables.
	 *
	 * @param name
	 * @return The named member scannable or null if it does not exist.
	 */
	Object __getattr__(String name) {
		// find the member's name and return it
		for (Scannable member : groupMembers) {
			if (member.getName().compareTo(name) == 0) {
				return member;
			}
		}

		// else try adding the scanablegroups name to the request
		String newName = getName() + "_" + name;
		for (Scannable member : groupMembers) {
			if (member.getName().compareTo(newName) == 0) {
				return member;
			}
		}
		throw Py.AttributeError(name);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		// TODO must check if the number of inputs match with number of members
		Vector<Object[]> targets = extractPositionsFromObject(position);

		// send out moves
		for (int i = 0; i < groupMembers.size(); i++) {
			// Don't try to move zero-input-extra name scannables
			if ((groupMembers.get(i).getInputNames().length + groupMembers.get(i).getExtraNames().length) == 0) {
				continue;
			}
			Object[] thisTarget = targets.get(i);
			if (thisTarget.length == 1) {
				if (targets.get(i)[0] != null) {
					groupMembers.get(i).asynchronousMoveTo(targets.get(i)[0]);
				}
			} else {
				groupMembers.get(i).asynchronousMoveTo(targets.get(i));
			}
		}

	}

	protected Vector<Object[]> extractPositionsFromObject(Object position) throws DeviceException {
		// map object to an array of doubles
		int inputLength = groupMembers.stream().mapToInt(member -> member.getInputNames().length).sum();
		Object[] targetPosition = PositionConvertorFunctions.toObjectArray(position);
		if (targetPosition.length != inputLength) {
			throw new DeviceException("Position does not have correct number of fields. Expected = " + inputLength
					+ " actual = " + targetPosition.length + " position= " + position.toString());
		}
		// break down to individual commands
		int targetIndex = 0;
		Vector<Object[]> targets = new Vector<Object[]>();
		for (Scannable member : groupMembers) {
			Object[] thisTarget = new Object[member.getInputNames().length];
			Object[] currentPos = PositionConvertorFunctions.toObjectArray(member.getPosition());
			for (int i = 0; i < member.getInputNames().length; i++) {
				thisTarget[i] = targetPosition[targetIndex];
				if (demandAllScannablePositions && thisTarget[i] == null) {
					thisTarget[i] = currentPos[i];
				}
				targetIndex++;
			}
			targets.add(thisTarget);
		}
		return targets;
	}

	@Override
	public Object getPosition() throws DeviceException {

		// create array of correct length
		int outputLength = 0;
		for (Scannable member : groupMembers) {
			outputLength += member.getInputNames().length;
			outputLength += member.getExtraNames().length;
		}
		Object[] position = new Object[outputLength];

		// loop through members and add position values to array
		try {
			List<Object[]> memberPositions = new Vector<Object[]>();
			for (Scannable member : groupMembers) {

				Object[] memberPosition;
				try {

					Object pos = member.getPosition();
					if (pos != null) {
						memberPosition = PositionConvertorFunctions.toObjectArray(pos);
					} else {
						memberPosition = new Object[0];
					}
				} catch (Exception e) {
					// if this fails, try getting strings instead. If that fails then let exception escalate
					memberPosition = ScannableUtils.getFormattedCurrentPositionArray(member);
				}
				memberPositions.add(memberPosition);
			}

			int n = 0;
			for (int i = 0; i < groupMembers.size(); i++) {
				for (int j = 0; j < groupMembers.get(i).getInputNames().length; j++) {
					position[n] = memberPositions.get(i)[j];
					n++;
				}
			}
			for (int i = 0; i < groupMembers.size(); i++) {
				for (int j = 0; j < groupMembers.get(i).getExtraNames().length; j++) {
					position[n] = memberPositions.get(i)[j + groupMembers.get(i).getInputNames().length];
					n++;
				}
			}
		} catch (Exception e) {
			throw new DeviceException("Exception occurred during {}.getPosition()", getName(), e);
		}

		return position;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		for (Scannable member : groupMembers) {
			if (member.isBusy()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setExtraNames(String[] names) {
		// do nothing: don't want to override members' extra names
	}

	@Override
	public void setInputNames(String[] names) {
		// do nothing: don't want to override members' input names
	}

	@Override
	public String toFormattedString() {
		return ScannableUtils.formatScannableWithChildren(this, getGroupMembers(), false);
	}

	@Override
	public void stop() throws DeviceException {
		for (Scannable member : groupMembers) {
			member.stop();
		}
	}

	/**
	 * Acts as a fan-out for messages from the Scannables inside this group {@inheritDoc}
	 *
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		/*
		 * only fan out if the notification did not already come from oneself. This is required for situations where a
		 * scannable is observing its surrounding scannableGroup and the scannablegroup is observing its children.
		 */
		if (theObserved != this) {
			notifyIObservers(this, changeCode);
		}
	}

	@Override
	public String[] getExtraNames() {
		// recalculate every time as these attributes may be dynamic
		// prepend scannable names to extra names to ensure uniqueness
		List<String> eNames = new ArrayList<>();
		for (Scannable member : groupMembers) {
			for (String eN : member.getExtraNames()) {
				eNames.add(eN);
			}
		}
		return eNames.toArray(new String[eNames.size()]);
	}

	@Override
	public String[] getInputNames() {
		// recalculate every time as these attributes may be dynamic
		// prepend scannable names to input names to ensure uniqueness
		List<String> iNames = new ArrayList<>();
		for (Scannable member : groupMembers) {
			for (String iN : member.getInputNames()) {
				iNames.add(iN);
			}
		}
		return iNames.toArray(new String[iNames.size()]);
	}

	@Override
	public String[] getOutputFormat() {
		List<String> outputFormat = new Vector<String>();
		for (int i = 0; i < groupMembers.size(); i++) {
			for (int j = 0; j < groupMembers.get(i).getInputNames().length; j++) {
				outputFormat.add(groupMembers.get(i).getOutputFormat()[j]);
			}
		}
		for (int i = 0; i < groupMembers.size(); i++) {
			for (int j = 0; j < groupMembers.get(i).getExtraNames().length; j++) {
				outputFormat.add(groupMembers.get(i).getOutputFormat()[j + groupMembers.get(i).getInputNames().length]);
			}
		}
		return outputFormat.toArray(new String[] {});
	}

	@Override
	public void atPointEnd() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atPointEnd();
		}
	}

	@Override
	public void atPointStart() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atPointStart();
		}
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atScanLineEnd();
		}

	}

	@Override
	public void atScanEnd() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atScanEnd();
		}
	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atLevelMoveStart();
		}
	}

	@Override
	public void atLevelStart() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atLevelStart();
		}
	}

	@Override
	public void atLevelEnd() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atLevelEnd();
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atCommandFailure();
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atScanStart();
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		for (Scannable scannable : groupMembers) {
			scannable.atScanLineStart();
		}
	}

	@Override
	public String checkPositionValid(Object illDefinedPosObject) throws DeviceException {

		Vector<Object[]> targets;
		try {
			targets = extractPositionsFromObject(illDefinedPosObject);
		} catch (Exception e) {
			return e.getMessage();
		}

		for (int i = 0; i < groupMembers.size(); i++) {
			Object[] thisTarget = targets.get(i);
			if ((thisTarget != null) && (thisTarget.length > 0) && (thisTarget[0] != null)) {
				String reason = groupMembers.get(i).checkPositionValid(thisTarget);
				if (reason != null) {
					return reason;
				}
			}
		}
		return null;
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		for (Scannable scannable : groupMembers) {
			scannable.waitWhileBusy();
		}
	}

	@Override
	public void removeGroupMemberByIndex(int index) {
		removeGroupMemberByScannable(groupMembers.get(index));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() + " ")
		.append(getName()+ ": ")
		.append(groupMembers);
		return sb.toString();
	}

	@Override
	public List<Scannable> getGroupMembers() {
		return groupMembers;
	}

	public boolean isDemandAllScannablePositions() {
		return demandAllScannablePositions;
	}

	public void setDemandAllScannablePositions(boolean demandAllScannablePositions) {
		this.demandAllScannablePositions = demandAllScannablePositions;
	}

	/**
	 * @return units corresponding to each field name.
	 */
	@Override
	public String[] getUnits() {
		final List<String> groupFieldNames = ScannableUtils.getScannableFieldNames(this);
		final String[] units = new String[groupFieldNames.size()];
		for (Scannable member: getGroupMembers()) {
			final String memberUnits = ScannableUtils.getScannableUnits(member);
			if (memberUnits == null) continue;
			for (String memberFieldName : ScannableUtils.getScannableFieldNames(member)) {
				final int memberFieldNameIndex = groupFieldNames.indexOf(memberFieldName);
				units[memberFieldNameIndex] = memberUnits;
			}
		}
		return units;
	}
}
