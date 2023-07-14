/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.path;

import static java.util.stream.Collectors.toList;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_PENCIL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.diamond.daq.mapping.ui.experiment.MultiStepEditorDialog;

public class AxialMultiStepPathEditor extends AbstractAxialPathEditor<AxialMultiStepModel> {

	private static final String PROPERTY_NAME_MODELS = "models";

	private Text axisText;
	private Binding axisTextBinding;

	@Override
	public Composite createEditorPart(Composite parent) {
		composite = makeComposite(parent, 2);

		axisText = new Text(composite, SWT.BORDER);
		axisText.setToolTipText("Specify a list of ranges, <start1 stop1 step1; start2 stop2 step2; ...>\n"
				+ "e.g. 0 40 10; 40 45 1; 45 75 5");
		grabHorizontalSpace.applyTo(axisText);
		axisTextBinding = createBinding(axisText, PROPERTY_NAME_MODELS, List.class,
				this::stringToModelList, this::modelListToString, this::validateModel);

		createMultiStepButton(composite);

		return composite;
	}

	private void createMultiStepButton(Composite parent) {
		final Button multiStepButton = new Button(parent, SWT.NONE);
		multiStepButton.setImage(Activator.getImage(IMG_PENCIL));
		multiStepButton.setToolTipText("Edit a multi-step path");
		multiStepButton.addSelectionListener(widgetSelectedAdapter(event -> {
			final Shell shell = Display.getCurrent().getActiveShell();
			final MultiStepEditorDialog editorDialog = new MultiStepEditorDialog(shell, getModel().getAxisName());
			editorDialog.setModel(getModel());
			if (editorDialog.open() == Window.OK) {
				// the editor updates the same model rather than creating a new one, so just update the text from the model
				axisTextBinding.updateModelToTarget();
			}
		}));
	}

	public IStatus validateModel(List<AxialStepModel> models) {
		return (models != null || axisText.getText().isEmpty()) ?
				ValidationStatus.ok() :
				ValidationStatus.error("Text is incorrectly formatted");
	}

	public String modelListToString(List<AxialStepModel> axialStepModels) {
		if (axialStepModels.isEmpty()) return "";
		if (axialStepModels.size() == 1)
			return modelToString(axialStepModels.get(0));

		final String[] modelStrings = axialStepModels.stream()
				.map(this::modelToString)
				.toArray(String[]::new);
		return String.join(";", modelStrings);
	}

	public String modelToString(AxialStepModel axialStepModel) {
		final String startString = doubleToString(axialStepModel.getStart());
		final String stopString = doubleToString(axialStepModel.getStop());
		final String stepString = doubleToString(axialStepModel.getStep());
		return String.join(" ", startString, stopString, stepString);
	}

	public List<AxialStepModel> stringToModelList(String modelsString) {
		if (modelsString.isBlank()) return Collections.emptyList();
		if (!modelsString.contains(";")) return List.of(stringToModel(modelsString));

		try {
			final String[] stepModelStrs = modelsString.trim().split(";");
			return Arrays.stream(stepModelStrs)
					.map(this::stringToModel)
					.collect(toList());
		} catch (Exception e) {
			return null; // NOSONAR cannot covert string to model
		}
	}

	public AxialStepModel stringToModel(String modelString) {
		final String[] startStopStep = modelString.trim().split(" ");
		if (startStopStep.length != 3) {
			throw new IllegalArgumentException();
		}

		final AxialStepModel stepModel = new AxialStepModel();
		stepModel.setName(getModel().getAxisName());
		stepModel.setStart(Double.parseDouble(startStopStep[0]));
		stepModel.setStop(Double.parseDouble(startStopStep[1]));
		stepModel.setStep(Double.parseDouble(startStopStep[2]));
		return stepModel;
	}

	@Override
	public void dispose() {
		super.dispose();
		axisTextBinding.dispose();
	}

}
