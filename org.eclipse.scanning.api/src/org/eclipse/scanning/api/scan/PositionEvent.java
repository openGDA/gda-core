/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.scan;

import java.util.EventObject;
import java.util.List;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.points.IPosition;

/**
 * An event describing a change of position.
 *
 * @author Matthew Gerring
 */
public class PositionEvent extends EventObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 6101070929612847926L;

	private int level;
	private List<? extends ILevel> levelObjects;

	private final IPosition position;

	public PositionEvent(IPosition position, INameable device) {
		super(device);
		this.position = position;
	}

	/**
	 * The current position during a move or the final
	 * position at the end of a move.
	 *
	 * If during a move the position will be read from the
	 * levelObjects.
	 *
	 * @return
	 */
	public IPosition getPosition() {
		return position;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public List<? extends ILevel> getLevelObjects() {
		return levelObjects;
	}

	public void setLevelObjects(List<? extends ILevel> levelObjects) {
		this.levelObjects = levelObjects;
	}

	public INameable getDevice() {
		return (INameable) getSource();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + ((levelObjects == null) ? 0 : levelObjects.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionEvent other = (PositionEvent) obj;
		if (level != other.level)
			return false;
		if (levelObjects == null) {
			if (other.levelObjects != null)
				return false;
		} else if (!levelObjects.equals(other.levelObjects))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

}
