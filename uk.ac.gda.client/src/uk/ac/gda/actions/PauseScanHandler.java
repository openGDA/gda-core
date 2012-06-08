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

package uk.ac.gda.actions;

import gda.jython.Jython;
import gda.jython.JythonServerFacade;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class PauseScanHandler extends AbstractHandler {
	
	/**
	 * Returns if the button should be checked (ie something was pause), true or
	 * if there was nothing to pause or a resume happened then false.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			final JythonServerFacade facade = JythonServerFacade.getCurrentInstance();
			if (facade.getScanStatus()==Jython.IDLE) {
				return Boolean.FALSE;
			}
			if (facade.getScanStatus()!=Jython.PAUSED) {
				facade.pauseCurrentScan();
				return Boolean.TRUE;
			} 
			facade.resumeCurrentScan();
			return Boolean.FALSE;
		} catch (Exception ne) {
			throw new ExecutionException(ne.getMessage(), ne);
		}
	}
}
