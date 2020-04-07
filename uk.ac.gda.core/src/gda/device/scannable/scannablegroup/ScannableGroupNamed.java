/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A logical group of Scannables that allows multiple Scannables to be moved at the same time, or allows further
 * validation for movement, e.g. {@link MotomanRobotScannableGroup}'s validation on simultaneous KTheta, KPhi movement,
 * that can additionally be managed through the Finder.
 *
 * inputNames, extraNames and outputFormat are taken from the constituent Scannables, not maintained as a field of the
 * Group.
 *
 * A ScannableGroupNamed is a logical group of Scannables that can be created through Spring instantiation or by adding
 * Scannables from the Jython console, or through the use of Scannable names with the Finder.
 *
 * Configuring a ScannableGroup configures all of its component Scannables and adds itself as an IObserver, and the
 * default behaviour of adding a Scannable to an already configured ScannableGroup is to configure the Scannable
 * (although it can instead un-configure the group, allowing it to be configured again).
 *
 * ScannableGroups can add, remove or set Scannables by using the Scannables or their names, and additionally can remove
 * Scannables by their index.
 *
 * The names within a ScannableGroupNamed are simply taken from the Scannables, not maintained seperately. For a more
 * lightweight Name/Scannable group management object, see AssemblyBase, which maintains Scannables and Names
 * seperately until configured.
 */
@ServiceInterface(IScannableGroupNamed.class)
public class ScannableGroupNamed extends ScannableGroup implements IScannableGroupNamed {

	private static final Logger logger = LoggerFactory.getLogger(ScannableGroupNamed.class);

	public ScannableGroupNamed() {
	}

	public ScannableGroupNamed(String name, Scannable[] groupMembers) throws FactoryException {
		this(name);
		setGroupMembersWithArray(groupMembers);
	}

	public ScannableGroupNamed(String name, List<Scannable> groupMembers) throws FactoryException {
		this(name);
		setGroupMembers(groupMembers);
	}

	public ScannableGroupNamed(String name) {
		setName(name);
	}

	/**
	 * Cannot have equivalent List<String> constructor as would mask List<Scannable> constructor
	 *
	 * @param name
	 * @param groupMembersNames
	 * @throws FactoryException
	 */

	public ScannableGroupNamed(String name, String[] groupMembersNames) throws FactoryException {
		this(name);
		setGroupMembersNamesWithArray(groupMembersNames);
	}

	@Override
	public final String[] getGroupMembersNamesAsArray() {
		return getGroupMembersNames().toArray(new String[0]);
	}

	@Override
	public List<String> getGroupMembersNames() {
		List<String> names = new ArrayList<>(groupMembers.size());
		for (Scannable groupMember : groupMembers) {
			names.add(groupMember.getName());
		}
		return names;
	}

	/**
	 * The default (scannableNames, false) implementation of setGroupMembersNames(names, toConfigure): if this group is
	 * already configured, it will be unconfigured to allow all group members to be configured by calling
	 * this.configure()
	 *
	 * @param scannableNames
	 *            a List of the names of the scannables to add to the group
	 * @throws FactoryException
	 *             if configuring a Scannable throws this Exception
	 */

	@Override
	public void setGroupMembersNames(List<String> scannableNames) throws FactoryException {
		setGroupMembersNamesWithList(scannableNames, false);
	}

	/**
	 * @param scannableNames - the names of the scannables to add to the group
	 * @param toConfigure
	 * 			behaviour if this group is already configured: whether to configure the new members (true)
	 * 			or unconfigure this group (false)
	 * @throws FactoryException if configuring a Scannable throws this Exception
	 */

	@Override
	public void setGroupMembersNamesWithList(List<String> scannableNames, boolean toConfigure) throws FactoryException {
		List<Scannable> scannables = new ArrayList<>(scannableNames.size());
		for (String name : scannableNames) {
			scannables.add((Scannable) Finder.getInstance().findOptional(name).orElseThrow(() ->
				new FactoryException("Finder does not contain a Scannable of the name: " + name)));
		}
		setGroupMembersWithList(scannables, toConfigure);
	}

	/**
	 * @param scannableNames - the names of the scannables to add to the group
	 * @param toConfigure
	 * 			behaviour if this group is already configured: whether to configure the new members (true)
	 * 			or unconfigure this group (false)
	 * @throws FactoryException if configuring a Scannable throws this Exception
	 */

	@Override
	public void setGroupMembersNamesWithArray(String[] scannableNames, boolean toConfigure)
			throws FactoryException {
		setGroupMembersNamesWithList(Arrays.asList(scannableNames), toConfigure);

	}

	/**
	 * The default (scannableNames, false) implementation of setGroupMembersNamesWithArray(names, toConfigure)
	 * @param scannableNames - the names of the scannables to add to the group
	 * @throws FactoryException if configuring a Scannable throws this Exception
	 */

	@Override
	public void setGroupMembersNamesWithArray(String[] scannableNames) throws FactoryException {
		setGroupMembersNamesWithArray(scannableNames, false);

	}

	@Override
	public void removeGroupMemberName(String name) throws DeviceException {
		removeGroupMemberByScannable(getGroupMemberByName(name));

	}

	@Override
	public Scannable getGroupMemberByName(String name) throws DeviceException {
		for (Scannable groupMember : groupMembers) {
			if (groupMember.getName().equals(name)) {
				return groupMember;
			}
		}
		throw new DeviceException(name, getName() + " does not contain " + name);
	}

	@Override
	public void addGroupMemberByName(String name) throws FactoryException {
		Scannable toAdd = (Scannable) Finder.getInstance().findOptional(name).orElseThrow(() ->
			new FactoryException("Finder does not contain a Scannable of the name: " + name));
		addGroupMember(toAdd);
	}

	@Override
	public void addGroupMember(Scannable groupMember, boolean toConfigure) throws FactoryException {
		for (Scannable alreadyMember : groupMembers) {
			if (alreadyMember.getName().equals(groupMember.getName())) {
				logger.info(getName() + " will not add Scannable named " + groupMember.getName()
						+ " as it already contains a Scannable with this name.");
				return;
			}
		}
		super.addGroupMember(groupMember, toConfigure);
	}

}
