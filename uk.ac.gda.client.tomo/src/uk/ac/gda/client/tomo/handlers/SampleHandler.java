/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.handlers;

import gda.jython.JythonServerFacade;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.gda.common.rcp.util.EclipseUtils;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SampleHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IEditorPart editor = HandlerUtil.getActiveEditor(event); 
		String filename = EclipseUtils.getFilePath(editor.getEditorInput());
		
		editor.doSave(new IProgressMonitor() {
			
			@Override
			public void worked(int work) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void subTask(String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setTaskName(String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCanceled(boolean value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isCanceled() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void internalWorked(double work) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void done() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beginTask(String name, int totalWork) {
				// TODO Auto-generated method stub
				
			}
		});
		
		JythonServerFacade jsf = JythonServerFacade.getInstance();
		String wd = jsf.evaluateCommand("pwd");
		jsf.runCommand(String.format("sm.addBasicTomographyScan('%s')",filename));
		
		return null;
	}
}
