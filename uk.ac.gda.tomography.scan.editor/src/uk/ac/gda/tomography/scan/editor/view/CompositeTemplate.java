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

import uk.ac.gda.tomography.scan.editor.TomographySWTElements;

/**
 * Template for build composite objects
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
	protected final T templateData;

	public CompositeTemplate(Composite parent, int style, T templateData) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().margins(TomographySWTElements.defaultCompositeMargin()).applyTo(this);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.LEFT, SWT.TOP).applyTo(this);
		this.templateData = templateData;
		createElements(SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
	}

	/**
	 * Returns the data model exposed by this Composite
	 *
	 * @return the model
	 */
	protected T getTemplateData() {
		return templateData;
	}

	/**
	 * Forces controls or model to a specific state depending from the logic implemented by the extending class.
	 * This methods is a default implementation and does nothing.
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