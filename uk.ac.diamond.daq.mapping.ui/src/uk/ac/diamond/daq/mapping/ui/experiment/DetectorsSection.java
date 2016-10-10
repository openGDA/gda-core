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

import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;

/**
 * A section for choosing which detectors should be included in the scan, and for
 * configuring their parameters.
 */
public class DetectorsSection extends AbstractMappingSection {

	DetectorsSection(MappingExperimentView mappingView, IEclipseContext context) {
		super(mappingView, context);
	}

	@Override
	public void createControls(Composite parent) {
		Composite detectorsComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(detectorsComposite);
		final int detectorsColumns = 3;
		GridLayoutFactory.swtDefaults().numColumns(detectorsColumns).applyTo(detectorsComposite);
		Label detectorsLabel = new Label(detectorsComposite, SWT.NONE);
		detectorsLabel.setText("Detectors");
		GridDataFactory.fillDefaults().span(detectorsColumns, 1).applyTo(detectorsLabel);

		DataBindingContext dataBindingContext = new DataBindingContext();
		for (IDetectorModelWrapper detectorParameters : mappingBean.getDetectorParameters()) {
			Button checkBox = new Button(detectorsComposite, SWT.CHECK);
			checkBox.setText(detectorParameters.getName());
			IObservableValue checkBoxValue = WidgetProperties.selection().observe(checkBox);
			IObservableValue activeValue = PojoProperties.value("includeInScan").observe(detectorParameters);
			dataBindingContext.bindValue(checkBoxValue, activeValue);
			checkBox.addListener(SWT.Selection, event -> {
				updateStatusLabel();
			});
			Text exposureTimeText = new Text(detectorsComposite, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);
			IObservableValue exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
			IObservableValue exposureTimeValue = PojoProperties.value("exposureTime").observe(detectorParameters.getModel());
			dataBindingContext.bindValue(exposureTextValue, exposureTimeValue);
			exposureTimeText.addListener(SWT.Modify, event -> {
				updateStatusLabel();
			});
			Button configButton = new Button(detectorsComposite, SWT.PUSH);
			configButton.setText("Edit parameters");

			IGuiGeneratorService guiGenerator = context.get(IGuiGeneratorService.class);
			configButton.addListener(SWT.Selection, event -> {
//				showDialogToEdit(detectorParameters.getModel(), detectorParameters.getName() + " Parameters");
				guiGenerator.openDialog(detectorParameters.getModel(), parent.getShell(),
						detectorParameters.getName() + " Parameters");
				dataBindingContext.updateTargets();
			});
		}
	}

}
