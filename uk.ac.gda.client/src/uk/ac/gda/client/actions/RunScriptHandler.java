/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.actions;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;
import uk.ac.gda.common.rcp.util.EclipseUtils;

public class RunScriptHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			File script = EclipseUtils.getFile(editor.getEditorInput());
			if (script != null) {
				ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();
				commandRunner.runScript(script);
			}
		} catch (Exception e) {
			throw new ExecutionException("Could not run script", e);
		}
		return null;
	}

}
