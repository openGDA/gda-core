/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.util;

import org.eclipse.jface.dialogs.MessageDialog;

/**
 *
 */
public class IntensityException extends Exception {

    private int type = MessageDialog.ERROR;
	private String suggestedGain;
	/**
	 * 
	 */
	public IntensityException() {
	
	}

	/**
	 * @param message
	 */
	public IntensityException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IntensityException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IntensityException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @return Returns the suggestedGain.
	 */
	public String getSuggestedGain() {
		return suggestedGain;
	}

	/**
	 * @param suggestedGain The suggestedGain to set.
	 */
	public void setSuggestedGain(String suggestedGain) {
		this.suggestedGain = suggestedGain;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

}
