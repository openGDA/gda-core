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

package gda.device.detector.analyser;

import java.io.Serializable;

/**
 * EpicsMCAAdc Class
 */
public class EpicsMCAAdc implements Serializable {
	private long gain;

	private long offset;

	private long lld;

	/**
	 * Constructor.
	 * 
	 * @param gain
	 * @param offset
	 * @param lld
	 */
	public EpicsMCAAdc(long gain, long offset, long lld) {
		this.gain = gain;
		this.offset = offset;
		this.lld = lld;
	}

	/**
	 * @return gain
	 */
	public long getGain() {
		return gain;
	}

	/**
	 * @param gain
	 */
	public void setGain(long gain) {
		this.gain = gain;
	}

	/**
	 * @return lld
	 */
	public long getLld() {
		return lld;
	}

	/**
	 * @param lld
	 */
	public void setLld(long lld) {
		this.lld = lld;
	}

	/**
	 * @return offset
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}

}
