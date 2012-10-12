/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.util;

import java.util.LinkedList;

/**
 * Subclass of {@link LinkedList} that enforces a maximum size on the list.
 */
public class BoundedLinkedList<E> extends LinkedList<E> {
	
	private int maxSize;
	
	public BoundedLinkedList(int maxSize) {
		super();
		this.maxSize = maxSize;
	}
	
	@Override
	public synchronized boolean add(E e) {
		removeRange(0, size() - maxSize + 1);
		return super.add(e);
	}
	
}
