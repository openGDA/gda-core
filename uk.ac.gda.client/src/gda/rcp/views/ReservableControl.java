/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.rcp.views;

/**
 * This interface allows an object to reserve a resource for itself.
 *
 * Having multiple objects are competing for the same resource, this interface allows a controller to restrict some
 * operations only to the object owning the resource. An example could be a controller exposing an instance of this
 * interface so other objects can reserve the controller for them self.
 *
 * As the reservation is done using {@link Object}, can act simply as validator irrespective of the class type, and this
 * is further stressed by the fact that the interface does not directly expose the object which reserved the resource.
 *
 * @author Maurizio Nagni
 */
public interface ReservableControl {
	/**
	 * Reserves the instance, setting the requesting object as owner. The operation fails if the instance is already
	 * reserved by another object or the requesting object is <code>null</code>
	 *
	 * @param owner
	 *            the class requesting to reserve the resource
	 * @return <code>true</code> if the operation succeeded, <code>false</code> otherwise
	 */
	public boolean reserve(Object owner);

	/**
	 * Release the instance. The operation fails if the requesting object is not the actual owner or the requesting
	 * object is <code>null</code>
	 *
	 * @param owner
	 *            the class requesting to release the resource
	 * @return <code>true</code> if the operation succeeded, <code>false</code> otherwise
	 */
	public boolean release(Object owner);

	/**
	 * Verify if an object is the actual owner.
	 *
	 * @param owner
	 *            the class requesting the validation
	 * @return <code>true</code> if the requesting object is the same as the actual owner, <code>false</code> otherwise
	 */
	public boolean isOwner(Object owner);

	/**
	 * Verify if this instance is actually reserved.
	 *
	 * @return <code>true</code> if reserved, <code>false</code> otherwise
	 */
	public boolean isReserved();
}
