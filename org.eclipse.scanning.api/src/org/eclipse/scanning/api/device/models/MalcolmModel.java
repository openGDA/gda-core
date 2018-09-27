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
package org.eclipse.scanning.api.device.models;

import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * The model for a malcolm device that writes h5 files.
 */
public class MalcolmModel extends AbstractDetectorModel implements IMalcolmModel {

	public MalcolmModel() {
		setTimeout(60*60*24); // 1 Day
	}

	@FieldDescriptor(editable=false)
	private List<String> axesToMove;

	@Override
	public List<String> getAxesToMove() {
		return axesToMove;
	}

	public void setAxesToMove(List<String> axesToMove) {
		this.axesToMove = axesToMove;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((axesToMove == null) ? 0 : axesToMove.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MalcolmModel other = (MalcolmModel) obj;
		if (axesToMove == null) {
			if (other.axesToMove != null)
				return false;
		} else if (!axesToMove.equals(other.axesToMove))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MalcolmModel [axesToMove=" + axesToMove + " " + super.toString() + "]";
	}

}
