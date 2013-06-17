/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.tilt;

import java.awt.geom.Point2D.Double;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TiltParameters implements Serializable {

	private String errorMessage;
	
	private List<Double> preTiltPoints;

	private List<Double> postTiltPoints;

	public TiltParameters() {
		preTiltPoints = new ArrayList<Double>();
		postTiltPoints = new ArrayList<Double>();
	}

	public void addPreTiltPoint(float x, float y) {
		preTiltPoints.add(new Double(x, y));
	}

	public void addPostTiltPoint(float x, float y) {
		postTiltPoints.add(new Double(x, y));
	}

	public List<Double> getPostTiltPoints() {
		return postTiltPoints;
	}

	public List<Double> getPreTiltPoints() {
		return preTiltPoints;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
