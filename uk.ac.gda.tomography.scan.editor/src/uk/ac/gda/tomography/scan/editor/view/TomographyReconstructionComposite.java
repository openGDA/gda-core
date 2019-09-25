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

import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * @author Maurizio Nagni
 */
public class TomographyReconstructionComposite extends CompositeTemplate<TomographyParametersAcquisitionController> {

	/** Pixel size **/
	private Text pixelSizeX;
	private Text pixelSizeY;

	public TomographyReconstructionComposite(Composite parent, TomographyParametersAcquisitionController controller) {
		this(parent, SWT.NONE, controller);
	}

	public TomographyReconstructionComposite(Composite parent, int style, TomographyParametersAcquisitionController controller) {
		super(parent, style, controller);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		GridLayoutFactory.swtDefaults().margins(ClientSWTElements.defaultCompositeMargin()).applyTo(this);
		pixelSizeContent(ClientSWTElements.createGroup(this, 2, ClientMessages.PIXEL_SIZE), labelStyle, textStyle);
	}

	private void pixelSizeContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, "X");
		ClientSWTElements.createLabel(parent, labelStyle, "Y");
		pixelSizeX = ClientSWTElements.createText(parent, textStyle, null);
		pixelSizeY = ClientSWTElements.createText(parent, textStyle, null);
	}

	@Override
	protected void bindElements() {
		DataBindingContext dbc = new DataBindingContext();
		ClientBindingElements.bindText(dbc, pixelSizeX, Integer.class, "pixelSizeX", getMetadata());
		ClientBindingElements.bindText(dbc, pixelSizeY, Integer.class, "pixelSizeY", getMetadata());
	}

	@Override
	protected void initialiseElements() {

	}

	private Map<String, String> getMetadata() {
		return getController().getAcquisition().getAcquisitionConfiguration().getMetadata();
	}
}
