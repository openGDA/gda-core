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

package uk.ac.gda.common.rcp.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.ICommandService;

/**
 * A class for interacting with the ICommandService. This facade does not have to be
 * used but simply wraps up common code to stop it being written again.
 */
public class CommandServiceFacade {

	/**
	 * Run a preconfigured command.
	 * @param site
	 * @param runScanCommandId
	 * @return null is command cannot be found. The command will return null if
	 * Eclipse command could not be found. Otherwise the return value of the command handler.
	 * @throws NotHandledException 
	 * @throws NotEnabledException 
	 * @throws NotDefinedException 
	 * @throws ExecutionException 
	 */
	public static Object runCommand(final IWorkbenchPartSite site, 
			                        final String             runScanCommandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
		
		final ICommandService service = (ICommandService)site.getService(ICommandService.class);
        final Command         run     = service.getCommand(runScanCommandId);
        if (run!=null) {
        	final Map<String,String> params = new HashMap<String,String>(1);

        	return run.executeWithChecks(new ExecutionEvent(run, params, site, null));
        }
			
		return null;
	}

}
