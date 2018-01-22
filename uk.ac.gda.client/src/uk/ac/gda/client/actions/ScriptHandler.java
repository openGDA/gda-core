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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.python.pydev.editor.PyEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.EclipseUtils;

public abstract class ScriptHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(ScriptHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		File script = null;
		try {
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			script = EclipseUtils.getFile(editor.getEditorInput());
			if (script != null) {
				run(script);
			}
		} catch (Exception e) {
			logger.error("Could not handle script '{}' from UI", script, e);
			throw new ExecutionException("Could not handle script", e);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		return (activePart instanceof PyEdit);
	}

	/**
	 * Run the event handler.
	 * <p>
	 * This will only be called if the file is present and has no unsaved changes.
	 * @param script file in currently active editor.
	 */
	abstract void run(File script);
}
