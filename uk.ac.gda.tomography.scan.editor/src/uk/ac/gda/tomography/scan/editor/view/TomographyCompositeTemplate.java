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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.model.ITomographyScanParameters;

/**
 * Template for build Tomography composite objects
 * @param <T>
 * 
 * @author Maurizio Nagni 
 */
public abstract class TomographyCompositeTemplate<T extends ITomographyScanParameters> extends Composite {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(TomographyCompositeTemplate.class);
	/**
	 * The tomography scan data
	 */
	protected final T tomographyData;

	public TomographyCompositeTemplate(Composite parent, int style, T tomographyData) {
		super(parent, style);
		this.tomographyData = tomographyData;
		createElements(SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
	}

	protected T getTomographyData() {
		return tomographyData;
	}

	protected abstract void createElements(int labelStyle, int textStyle);
	protected abstract void initialiseElements();
	protected abstract void bindElements();
}