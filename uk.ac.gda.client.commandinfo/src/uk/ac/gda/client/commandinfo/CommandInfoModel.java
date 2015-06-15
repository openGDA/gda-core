/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.commandinfo;

import gda.jython.commandinfo.ICommandThreadInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandInfoModel extends HashMap<Long,ICommandThreadInfo> {

	private static final long serialVersionUID = -7113504880257550153L;

	public List<ICommandThreadInfo> getCommandList() {
		return Arrays.asList(getCommandElements());
	}

	public ICommandThreadInfo[] getCommandElements() {
		return this.values().toArray(new ICommandThreadInfo[this.size()]);
	}

}
