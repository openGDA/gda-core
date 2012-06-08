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

package gda.commandqueue;

import java.io.Serializable;

public class SimpleCommandProgress implements Serializable, CommandProgress {
	float percentDone;
	String msg;

	public SimpleCommandProgress(float percentDone, String msg) {
		super();
		this.percentDone = percentDone;
		this.msg = msg;
	}
	
	/**
	 * 
	 * @param percentDone
	 * @param msg
	 * @deprecated use floats for percentDone
	 */
	public SimpleCommandProgress(int percentDone, String msg) {
		super();
		this.percentDone = percentDone;
		this.msg = msg;
	}
	/**
	 * @return Returns the percentDone.
	 */
	@Override
	public float getPercentDone() {
		return percentDone;
	}
	/**
	 * @param percentDone The percentDone to set.
	 */
	public void setPercentDone(float percentDone) {
		this.percentDone = percentDone;
	}
	/**
	 * @param percentDone The percentDone to set.
	 * @deprecated use floats
	 */
	public void setPercentDone(int percentDone) {
		this.percentDone = percentDone;
	}
	/**
	 * @return Returns the msg.
	 */
	@Override
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg The msg to set.
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	@Override
	public String toString() {
		return String.format("SimpleCommandProgress [percentDone=%3.1f%%, msg=%s]", percentDone, msg);
	}
	@Override
	public String getUniqueID() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}	
}