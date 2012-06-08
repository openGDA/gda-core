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

public interface CommandProgress extends Serializable {

	/**
	 * @return Returns the percentDone.
	 */
	public float getPercentDone();

	/**
	 * @return Returns the msg.
	 */
	public String getMsg();

	/**
	 * @return a unique ID, which will not necessarily be displayed but can be used to identify which run of the script
	 *         this is from.
	 */
	public String getUniqueID();

	/**
	 * @return a label to identify to the user what type of script is being run. Enables filtering.
	 */
	public String getName();
}