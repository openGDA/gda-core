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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.tomography.model.TomographyReconstruction;
import uk.ac.gda.tomography.scan.editor.TomographyBindingElements;
import uk.ac.gda.tomography.scan.editor.TomographySWTElements;

/**
 * @author Maurizio Nagni
 */
public class TomographyReconstructionComposite extends CompositeTemplate<TomographyReconstruction> {

	/** Pixel size **/
	private Text pixelSizeX;
	private Text pixelSizeY;

	public TomographyReconstructionComposite(Composite parent, TomographyReconstruction tomographyData) {
		this(parent, SWT.NONE, tomographyData);
	}

	public TomographyReconstructionComposite(Composite parent, int style, TomographyReconstruction tomographyData) {
		super(parent, style, tomographyData);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		GridLayoutFactory.swtDefaults().margins(TomographySWTElements.defaultCompositeMargin()).applyTo(this);
		pixelSizeContent(TomographySWTElements.createGroup(this, 2, TomographyMessages.PIXEL_SIZE), labelStyle, textStyle);
	}

	private void pixelSizeContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, "X");
		TomographySWTElements.createLabel(parent, labelStyle, "Y");
		pixelSizeX = TomographySWTElements.createText(parent, textStyle, null);
		pixelSizeY = TomographySWTElements.createText(parent, textStyle, null);
	}

	@Override
	protected void bindElements() {
		DataBindingContext dbc = new DataBindingContext();

		TomographyBindingElements.bindText(dbc, pixelSizeX, Integer.class, "pixelSizeX", getTemplateData());
		TomographyBindingElements.bindText(dbc, pixelSizeY, Integer.class, "pixelSizeY", getTemplateData());
	}

	@Override
	protected void initialiseElements() {

	}
}
