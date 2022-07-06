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

package uk.ac.gda.richbeans.editors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;

public class RichBeanEditorOperation extends AbstractOperation {

	protected Object undo, redo;
	protected RichBeanEditorPart richBeanEditor;

	public RichBeanEditorOperation(final String label, final Object undo, final Object redo, final RichBeanEditorPart richBeanEditor) {
		super(label != null ? label : "");
		this.undo = undo;
		this.redo = redo;
		this.richBeanEditor = richBeanEditor;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return Status.OK_STATUS;
	}

	public IStatus beanToUI(@SuppressWarnings("unused") IProgressMonitor monitor, IAdaptable info, final Object bean) throws ExecutionException {
		final Shell shell = info.getAdapter(Shell.class);
		shell.setRedraw(false);
		richBeanEditor.setUndoStackActive(false);
		try {
			richBeanEditor.updateUiFromOtherBean(bean);
			return Status.OK_STATUS;
		} catch (Exception e) {
			throw new ExecutionException(e.getMessage(), e);
		} finally {
			richBeanEditor.setUndoStackActive(true);
			shell.setRedraw(true);
		}
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return beanToUI(monitor, info, redo);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return beanToUI(monitor, info, undo);
	}
}