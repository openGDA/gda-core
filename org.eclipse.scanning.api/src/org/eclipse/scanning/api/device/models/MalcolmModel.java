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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * The model for a malcolm device that writes h5 files.
 */
public class MalcolmModel extends AbstractDetectorModel implements IMalcolmModel {

	private static final long DEFAULT_TIMEOUT_SECOND = Duration.ofDays(1).getSeconds();

	@FieldDescriptor(editable=false)
	private List<String> axesToMove;

	private List<IMalcolmDetectorModel> detectorModels;

	public MalcolmModel() {
		setTimeout(DEFAULT_TIMEOUT_SECOND);
	}

	public MalcolmModel(IMalcolmModel toCopy) {
		super((AbstractDetectorModel) toCopy);
		setAxesToMove(toCopy.getAxesToMove() == null ? null : new ArrayList<>(toCopy.getAxesToMove()));

		final List<IMalcolmDetectorModel> detectorModelsToCopy = toCopy.getDetectorModels();
		if (detectorModelsToCopy != null) {
			final List<IMalcolmDetectorModel> newDetectorModels = new ArrayList<>(detectorModelsToCopy.size());
			for (IMalcolmDetectorModel detectorModel : toCopy.getDetectorModels()) {
				newDetectorModels.add(new MalcolmDetectorModel(detectorModel));
			}
			setDetectorModels(newDetectorModels);
		}
	}

	@Override
	public List<String> getAxesToMove() {
		return axesToMove;
	}

	@Override
	public void setAxesToMove(List<String> axesToMove) {
		this.axesToMove = axesToMove;
	}

	@Override
	public List<IMalcolmDetectorModel> getDetectorModels() {
		return detectorModels;
	}

	@Override
	public void setDetectorModels(List<IMalcolmDetectorModel> detectorModels) {
		this.detectorModels = detectorModels;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((axesToMove == null) ? 0 : axesToMove.hashCode());
		result = prime * result + ((detectorModels == null) ? 0 : detectorModels.hashCode());
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
		if (detectorModels == null) {
			if (other.detectorModels != null)
				return false;
		} else if (!detectorModels.equals(other.detectorModels))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MalcolmModel [axesToMove=" + axesToMove + " detectorModels=" + detectorModels + " "
				+ super.toString() + "]";
	}

}
