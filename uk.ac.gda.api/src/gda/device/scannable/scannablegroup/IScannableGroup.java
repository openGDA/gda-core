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

import java.util.List;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FactoryException;

/**
 * Distributed interface for the ScannableGroup which provides a logical group of scannables
 */

public interface IScannableGroup extends Scannable {

	Scannable[] getGroupMembersAsArray() throws DeviceException;

	List<Scannable> getGroupMembersAsList() throws DeviceException;

	void setGroupMembersWithList(List<Scannable> scannables) throws FactoryException;

	void setGroupMembersWithArray(Scannable[] scannables) throws FactoryException;

	void removeGroupMemberByScannable(Scannable scannable) throws FactoryException;

	void removeGroupMemberByIndex(int index) throws FactoryException;

	void addGroupMember(Scannable scannable) throws FactoryException;

	void addGroupMember(Scannable groupMember, boolean toConfigure) throws FactoryException;

	void setGroupMembersWithList(List<Scannable> groupMembers, boolean toConfigure) throws FactoryException;
}
