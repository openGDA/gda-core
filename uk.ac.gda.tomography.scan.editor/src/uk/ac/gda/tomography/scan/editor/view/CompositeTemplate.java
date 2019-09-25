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

package uk.ac.gda.tomography.scan.editor.view;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Template for build composite objects.
 * Using a <T extends Object> may look useless but the idea is to keep this "useless generic" as place holder for a future restriction.
 * At the moment the closest restriction could be <T extends AcquisitionController<?>> however this template is supposed to be driven by a controller but not necessarily a Controller
 * related to an acquisition.
 * Consequently the decision to restrict <T> to a specific interface is delayed until more uses case will appear.
 *
 * @param <T>
 * @author Maurizio Nagni
 */
public abstract class CompositeTemplate<T extends Object> extends Composite {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CompositeTemplate.class);
	/**
	 * The configuration data
	 */
	// protected T templateData;

	protected T controller;

	public CompositeTemplate(Composite parent, int style, T controller) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().margins(ClientSWTElements.defaultCompositeMargin()).applyTo(this);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.LEFT, SWT.TOP).applyTo(this);
		this.controller = controller;
		createElements(SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
	}

	public CompositeTemplate(Composite parent, int style) {
		this(parent, style, null);
	}

	/**
	 * Returns the data model exposed by this Composite
	 *
	 * @return the model
	 */
	protected T getController() {
		return controller;
	}

	// /**
	// * Sets the data model exposed by this Composite
	// *
	// * @return the model
	// */
	// protected void setTemplateData(T templateData) {
	// this.templateData = templateData;
	// }

	/**
	 * Forces controls or model to a specific state depending from the logic implemented by the extending class. This methods is a default implementation and
	 * does nothing.
	 */
	protected void initialiseElements() {

	}

	/**
	 * Creates the Composite layout
	 *
	 * @param labelStyle
	 * @param textStyle
	 */
	protected abstract void createElements(int labelStyle, int textStyle);

	/**
	 * Binds the Composite's Controls to the underlying data model
	 */
	protected abstract void bindElements();
}