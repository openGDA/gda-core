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
 * Implementation of IScannableGroupNamed which manages names exlusively with the Finder i.e. the groupMemberNames are
 * the names of the group members, and each name is irrevocably tied to a group member, vice versa.
 */
@ServiceInterface(IScannableGroup.class)
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
	 * The default (scannableNames, false) implementation of setGroupMembersNamesWithArray(names, toConfigure)
	 * @param scannableNames - the names of the scannables to add to the group
	 * @throws FactoryException if configuring a Scannable throws this Exception
	 */

	@Override
	public void setGroupMembersNames(List<String> scannableNames) throws FactoryException {
		setGroupMembersNamesWithList(scannableNames, false);
	}

	/**
	 * @param scannableNames - the names of the scannables to add to the group
	 * @param toConfigure - Should the ScannableGroup configure itself after setting the names?
	 * 						This would also configure all the members
	 * @throws FactoryException if configuring a Scannable throws this Exception
	 */

	@Override
	public void setGroupMembersNamesWithList(List<String> scannableNames, boolean toConfigure) throws FactoryException {
		List<Scannable> scannables = new ArrayList<>(scannableNames.size());
		for (String name : scannableNames) {
			if (Finder.getInstance().find(name) instanceof Scannable) {
				scannables.add(Finder.getInstance().find(name));
			} else {
				logger.error("{} was not a Scannable, it cannot be added to {}!", name, getName());
			}
		}
		setGroupMembersWithList(scannables, toConfigure);
	}

	/**
	 * @param scannableNames - the names of the scannables to add to the group
	 * @param toConfigure - Should the ScannableGroup configure itself after setting the names?
	 * 						This would also configure all the members
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
		Scannable toAdd = Finder.getInstance().find(name);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ScannableGroupNamed " + getName() + ": ").append(groupMembers);
		return sb.toString();
	}
}
