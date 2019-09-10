/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractModelEditor<T> implements IModelEditor<T> {

	private T model;
	private Composite composite;

	/**
	 * @return the model being edited by this editor
	 */
	@Override
	public T getModel() {
		return model;
	}

	/**
	 * Sets the model to edit.
	 * This method should be called before createEditorPart for data binding.
	 * @param model the model to edit.
	 */
	@Override
	public void setModel(T model) {
		this.model = model;
	}

	/**
	 * Create a GUI to edit the model (which must already be set). Default implementation creates a blank 2-column composite.
	 * For proper disposal and style consistency, final implementations should use super's returned composite.
	 * i.e.
	 * <pre>
	 * {@code @Override
	 * public Composite createEditorPart(Composite parent)
	 *     final Composite composite = super.createEditorPart(parent)
	 *     ....
	 *     return composite;}
	 * </pre>
	 *
	 * Final implementations should handle the data binding as well - probably using the protected DataBinder instance.
	 * @param parent composite on which to put this one.
	 * @return Editor composite
	 */
	@Override
	public Composite createEditorPart(Composite parent) {
		composite = makeComposite(parent);
		return composite;
	}

	/**
	 * Child classes can override and add their tear down calls,
	 * but they should call super.dispose() as well
	 */
	@Override
	public void dispose() {
		composite.dispose();
	}

	/**
	 * @return a blank 2-column composite
	 */
	private Composite makeComposite(Composite parentComposite) {
		composite = new Composite(parentComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 5).applyTo(composite);
		return composite;
	}

}
