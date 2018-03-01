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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Base class for model (e.g. region, path) editors. Once the model is set, call createEditorPart.
 * Though it has no abstract methods, this class is abstract because createEditorPart returns a blank composite.
 * @param <T> Type of model handled by the editor
 */
public abstract class AbstractModelEditor<T> {

	private T model;
	private Composite composite;

	@Inject
	private IStageScanConfiguration mappingStageInfo;

	/**
	 * Apply to a control to make it fill horizontal space
	 */
	protected final GridDataFactory grabHorizontalSpace = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

	/**
	 * For common binding calls
	 */
	protected final DataBinder binder = new DataBinder();

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
	public Composite createEditorPart(Composite parent) {
		return makeComposite(parent);
	}

	/**
	 * This method should be called before createEditorPart for data binding.
	 * @param model
	 */
	public void setModel(T model) {
		this.model = model;
	}

	/**
	 * @return the model being edited by this editor
	 */
	public T getModel() {
		return model;
	}

	/**
	 * Child classes can override and add their tear down calls,
	 * but they should call super.dispose() as well
	 */
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

	/**
	 * @return scannable name if IStageScanConfiguration is configured, otherwise the literal "Fast axis"
	 */
	protected String getFastAxisName() {
		return Objects.nonNull(mappingStageInfo) ? mappingStageInfo.getActiveFastScanAxis() : "Fast axis";
	}

	/**
	 * @return scannable name if IStageScanConfiguration is configured, otherwise the literal "Slow axis"
	 */
	protected String getSlowAxisName() {
		return Objects.nonNull(mappingStageInfo) ? mappingStageInfo.getActiveSlowScanAxis() : "Slow axis";
	}

}
