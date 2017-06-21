/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.image;

/**
 * Thrown by ImageCutdown.java when provided with wrongly formatted co-ords
 */
public class InvalidCoordinatesException extends Exception {

	private final int[][] coords;
	private final int index;

	public InvalidCoordinatesException(int [][] coords, int index) {
		this.index = index;
		this.coords = coords;
	}

	public InvalidCoordinatesException(int [][] coords) {
		this(coords, -1);
	}

	public InvalidCoordinatesException(String message, int [][] coords, int index) {
		super(message);
		this.index = index;
		this.coords = coords;
	}

	public InvalidCoordinatesException(String message, int [][] coords) {
		this(message, coords, -1);
	}

	/*
	 * if ImageCutdown is given a list of co-ordinates, this returns the index of the
	 * coordinates in the list which cause the exception
	 * If desired, this can be used to remove the offending coords from the list and try
	 * again
	 *
	 * If image cutdown was called with a single set of coordinates, this is -1.
	 *
	 * @return
	 */
	public int index() {
		return index;
	}

	/*
	 * returns a copy of the coordinates which caused the exception
	 *
	 * @return
	 */
	public int[][] coords() {
		return coords;
	}
}
