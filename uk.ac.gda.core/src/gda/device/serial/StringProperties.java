/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.serial;

import java.util.Vector;

/**
 * Properties of String buffer used in StringReader and StringWriter classes
 */
public class StringProperties {
	private Vector<String> terminatorList = new Vector<String>();

	private int bufferSize;

	private boolean termination;

	/**
	 * Sets default values String terminator = " ", bufferSize = 128, termination = on
	 */
	public StringProperties() {
		this(" ", 128, true);
	}

	/**
	 * Sets default values bufferSize = 128, termination = on
	 * 
	 * @param terminator
	 *            the termination String
	 */
	public StringProperties(String terminator) {
		this(terminator, 128, true);
	}

	/**
	 * Sets default value termination = on
	 * 
	 * @param terminator
	 *            the termination String
	 * @param bufferSize
	 *            buffer size
	 */
	public StringProperties(String terminator, int bufferSize) {
		this(terminator, bufferSize, true);
	}

	/**
	 * @param terminator
	 *            termination String
	 * @param bufferSize
	 *            buffer size
	 * @param termination
	 *            enable/disable termination
	 */
	public StringProperties(String terminator, int bufferSize, boolean termination) {
		setTerminator(terminator);
		this.bufferSize = bufferSize;
		this.termination = termination;
	}

	/**
	 * @param bufferSize
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param termination
	 */
	public void setTermination(boolean termination) {
		this.termination = termination;
	}

	/**
	 * @return boolean termination
	 */
	public boolean getTermination() {
		return termination;
	}

	/**
	 * @param terminator
	 */
	public void setTerminator(String terminator) {
		terminatorList.removeAllElements();
		terminatorList.addElement(terminator);
	}

	/**
	 * @param terminator
	 */
	public void addTerminator(String terminator) {
		if (!isTerminator(terminator)) {
			terminatorList.addElement(terminator);
		}
	}

	/**
	 * @return terminator
	 */
	public String getTerminator() {
		return (terminatorList.isEmpty() ? null : (String) (terminatorList.firstElement()));
	}

	/**
	 * @param terminator
	 * @return boolean
	 */
	public boolean isTerminator(String terminator) {
		return (terminatorList.contains(terminator));
	}

	/**
	 * @param terminator
	 */
	public void removeTerminator(String terminator) {
		int i;

		if ((i = terminatorList.indexOf(terminator)) > (-1)) {
			terminatorList.removeElementAt(i);
		}
	}

	/**
	 * Check if the string is terminated by one of the characters specified terminator list
	 * 
	 * @param terminator
	 *            the string to check for termination
	 * @return true if terminated
	 */
	public int isTerminated(String terminator) {
		int num_elements = terminatorList.size();
		for (int i = 0; i < num_elements; i++) {
			int index;
			if ((index = terminator.indexOf(terminatorList.elementAt(i))) > (-1)) {
				return index;
			}
		}

		return (-1);
	}
}
