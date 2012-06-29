/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.hrpd.anticollision;


import gda.factory.Findable;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

public class OperationModes implements Findable {
	private String name;
	private Mode mode = Mode.MAC;
	
	public Mode getMode() {
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode=mode;
	}
	
	public void availableModes() {
		for (Mode mode: Mode.values()) {
			ITerminalPrinter printer;
			if ((printer=InterfaceProvider.getTerminalPrinter()) != null) {
				printer.print(mode.toString());
			}
		}
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name=name;		
	}

}
