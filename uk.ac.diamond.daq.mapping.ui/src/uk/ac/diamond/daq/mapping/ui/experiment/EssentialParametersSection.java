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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A section to configure essential parameters of a scan that do not belong elsewhere.
 */
public class EssentialParametersSection extends AbstractMappingSection {

	EssentialParametersSection(MappingExperimentView mappingView, IEclipseContext context) {
		super(mappingView, context);
	}

	@Override
	public void createControls(Composite parent) {
		Composite essentialParametersComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(essentialParametersComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(essentialParametersComposite);
		// FIXME not good to be hard-coding things here to look like the GUI generator - can we auto-generate these fields?
		Label sampleNameLabel = new Label(essentialParametersComposite, SWT.NONE);
		sampleNameLabel.setText("Sample Name:");
		Text sampleNameText = new Text(essentialParametersComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleNameText);

		DataBindingContext dataBindingContext = new DataBindingContext();
		IObservableValue sampleNameTextValue = WidgetProperties.text(SWT.Modify).observe(sampleNameText);
		IObservableValue sampleNameModelValue = PojoProperties.value("sampleName").observe(mappingBean.getSampleMetadata());
		dataBindingContext.bindValue(sampleNameTextValue, sampleNameModelValue);
		Button editMetadataButton = new Button(essentialParametersComposite, SWT.PUSH);
		editMetadataButton.setText("Edit metadata");

		IGuiGeneratorService guiGenerator = context.get(IGuiGeneratorService.class);
		editMetadataButton.addListener(SWT.Selection, event -> {
			guiGenerator.openDialog(mappingBean.getSampleMetadata(), parent.getShell(), "Sample Metadata");
			// Ensure that any changes to metadata in the dialog are reflected in the main GUI
			dataBindingContext.updateTargets();
		});
	}

}
