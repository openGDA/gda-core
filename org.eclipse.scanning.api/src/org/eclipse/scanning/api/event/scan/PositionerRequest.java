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
package org.eclipse.scanning.api.event.scan;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.points.IPosition;

public class PositionerRequest extends IdBean {

	private PositionRequestType requestType = PositionRequestType.GET;
	private IPosition position;
	private String errorMessage;


	public IPosition getPosition() {
		return position;
	}

	public void setPosition(IPosition position) {
		this.position = position;
	}

	public PositionRequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(PositionRequestType positionType) {
		this.requestType = positionType;
	}

	/**
	 * @return
	 * 		An error message if the request is unsuccessful,
	 * 		or {@code null} if request handled successfully
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private static final long serialVersionUID = -3212765978085542676L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((requestType == null) ? 0 : requestType.hashCode());
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
		PositionerRequest other = (PositionerRequest) obj;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (requestType != other.requestType)
			return false;
		return true;
	}

}
