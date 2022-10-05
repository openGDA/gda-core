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

package org.eclipse.scanning.event.queue;

import java.util.List;
import java.util.Queue;

import org.eclipse.scanning.api.event.IdBean;

/**
 * A {@link Queue} that provides methods to allow the queue to be modified.
 * Methods are provided that allow elements to be moved up (closer to the head)
 * and down (closer to the tail) the queue.
 *
 * It also identifies elements in the queue by their id, as given by
 * {@link IdBean#getUniqueId()}, i.e. if the queue contains an existing element
 * with the same id as the element passed into the method as an argument,
 * that is the element that is affected by the method called.
 *
 * @param <E>
 */
public interface IModifiableIdQueue<E extends IdBean> extends Queue<E> {

	/**
	 * Return a list of contents of the queue, in the same order as they are in the queue.
	 * Note that the list is a snapshot of the contents of the queue at the time that this
	 * method is called, changes to this list will not be reflected in the underlying queue.
	 * @return a list of the contents of the queue
	 */
	public List<E> getElements();

	/**
	 * Replaces the existing element in the queue with the same id as the given
	 * element with the given element. If the queue does not contain an element with the
	 * same id as the one given, the queue is unmodified.
	 * @param e the new element
	 * @return <code>true</code> if the queue was modified as a result of this
	 *   call (i.e. the queue contained an element with the same id as the that given),
	 *   <code>false</code> otherwise
	 */
	public boolean replace(E e);

	/**
	 * Moves the element in the queue with the same id as that given up the queue,
	 * i.e. towards the head. If the queue does not contain an element with the same
	 * id as the one given, the queue is unmodified.
	 * @param e the element to move up the queue
	 * @return <code>true</code> if the queue was modified as a result of this
	 *   call (i.e. the queue contained an element with the same id as the that given),
	 *   <code>false</code> otherwise
	 */
	public boolean moveUp(E e);

	/**
	 * Moves the element in the queue with the same id as that given down the queue,
	 * i.e. towards the tail. If the queue does not contain an element with the same
	 * id as the one given, the queue is unmodified.
	 * @param e the element to move down the queue
	 * @return <code>true</code> if the queue was modified as a result of this
	 *   call (i.e. the queue contained an element with the same id as the that given),
	 *   <code>false</code> otherwise
	 */
	public boolean moveDown(E e);

	/**
	 * Removes the element in the queue with the same id as the given element.
	 * If the queue does not contain an element with the same id as the one given,
	 * the queue is unmodified.
	 * @return <code>true</code> if the queue was modified as a result of this
	 *   call (i.e. the queue contained an element with the same id as the that given),
	 *   <code>false</code> otherwise
	 */
	@Override
	public boolean remove(Object o);

}
