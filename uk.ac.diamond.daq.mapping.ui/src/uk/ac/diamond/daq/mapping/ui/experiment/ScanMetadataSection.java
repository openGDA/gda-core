/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import static uk.ac.gda.ui.tool.ClientMessages.SAMPLE_METADATA_EDIT;
import static uk.ac.gda.ui.tool.ClientMessages.SAMPLE_METADATA_EDIT_TP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.SimpleSampleMetadata;

/**
 * A section to configure essential parameters of a scan that do not belong elsewhere.
 */
public class ScanMetadataSection extends AbstractMappingSection {

	private Text sampleNameText;
	private MetadataController controller;

	private MetadataController getController() {
		if (controller == null) {
			controller = getService(MetadataController.class);
			controller.initialise();
			controller.addListener(update -> {
				if (!update.getAcquisitionName().equals(sampleNameText.getText())) {
					sampleNameText.setText(update.getAcquisitionName());
				}
			});
		}
		return controller;
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		IMappingExperimentBean mappingBean = getMappingBean();
		Composite essentialParametersComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(essentialParametersComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(essentialParametersComposite);
		Label sampleNameLabel = new Label(essentialParametersComposite, SWT.NONE);
		sampleNameLabel.setText("Sample Name");
		sampleNameText = new Text(essentialParametersComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleNameText);

		sampleNameText.setText(getController().getAcquisitionName());
		sampleNameText.addListener(SWT.Modify, event -> getController().setAcquisitionName(sampleNameText.getText()));

		Button editMetadataButton = new Button(essentialParametersComposite, SWT.PUSH);
		editMetadataButton.setText(getMessage(SAMPLE_METADATA_EDIT));
		editMetadataButton.setToolTipText(getMessage(SAMPLE_METADATA_EDIT_TP));

		editMetadataButton.addListener(SWT.Selection, event -> {
			SimpleSampleMetadata metadata = (SimpleSampleMetadata) mappingBean.getSampleMetadata();
			EditSampleMetadataDialog dialog = new EditSampleMetadataDialog(parent.getShell(), metadata.getSampleName(), metadata.getDescription());
			if (dialog.open() == Window.OK) {
				metadata.setSampleName(dialog.getName());
				metadata.setDescription(dialog.getDescription());
				// Ensure that any changes to metadata in the dialog are reflected in the main GUI
				updateControls();
			}
		});
	}

	@Override
	public void updateControls() {
		sampleNameText.setText(getController().getAcquisitionName());
	}

}
