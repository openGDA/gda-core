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

package uk.ac.diamond.daq.mapping.ui.tomo;

import static java.util.stream.Collectors.joining;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.ANGLE_1_LABEL;
import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.ANGLE_2_LABEL;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.ui.experiment.DataBinder;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoPathInfo.StepSizes;

class TomoPathSection extends AbstractTomoViewSection {

	public enum AxialPathModelType {
		STEP("Step size", AxialStepModel.class),
		POINTS("Num. points", AxialPointsModel.class),
		ARRAY("List of points", AxialArrayModel.class),
		MULTI_STEP("Multiple ranges", AxialMultiStepModel.class);

		private final String label;
		private final Class<? extends IAxialModel> modelClass;

		private AxialPathModelType(String label, Class<? extends IAxialModel> modelClass) {
			this.label = label;
			this.modelClass = modelClass;
		}

		public String getLabel() {
			return label;
		}

		@SuppressWarnings("unchecked")
		public <T extends IAxialModel> Class<T> getModelClass() {
			return (Class<T>) modelClass;
		}

		public static <T extends IAxialModel> AxialPathModelType forModel(T model) {
			return Arrays.stream(AxialPathModelType.values())
				.filter(type -> type.getModelClass().isInstance(model))
				.findFirst().orElseThrow();
		}
	}

	private static final String ANGLE2_STEPS_ARRAY_LIST_LABEL = ANGLE_2_LABEL + " positions (same for each " + ANGLE_1_LABEL + " position): ";
	private static final String ANGLE2_STEP_SIZES_LABEL = "Calculated " + ANGLE_2_LABEL + " step sizes for " + ANGLE_1_LABEL + " positions: ";

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.#");
	private static final String DELIMITER = ", ";

	private final DataBinder binder = new DataBinder();

	private Label angle2StepsLabel;

	private TomoAngleEditorsBlock tomoAngleEditorsBlock;

	private Button moreDetailsButton;

	private Angle2StepSizesDialog tomoPathDetailsDialog;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		final Composite composite = createComposite(parent, 1, true);

		final Label sectionLabel = new Label(composite, SWT.NONE);
		sectionLabel.setText("Tomography Setup");

		// A label to explain that the specified angle2 path is for angle1 = 0, angle 2 step size is calculated for
		final Label label = new Label(composite, SWT.NONE);
		label.setText("Specify the " + ANGLE_2_LABEL + " path for " + ANGLE_1_LABEL + " = 0. The paths for " + ANGLE_1_LABEL + " positions are calculated (except 'List of points').");
		GridDataFactory.fillDefaults().applyTo(label);

		createStepsArea(composite);
	}

	private void createStepsArea(final Composite parent) {
		tomoAngleEditorsBlock = new TomoAngleEditorsBlock(getBean(), getEclipseContext());
		tomoAngleEditorsBlock.addAngleModelChangeListener(modelTypeChanged -> anglePathsChanged(modelTypeChanged, false));
		tomoAngleEditorsBlock.createControls(parent);

		// A row to show the calculated step sizes
		final Composite stepSizesComposite = createComposite(parent, 2, false);

		angle2StepsLabel = new Label(stepSizesComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(angle2StepsLabel);

		moreDetailsButton = new Button(stepSizesComposite, SWT.NONE);
		moreDetailsButton.setText("More...");
		moreDetailsButton.addSelectionListener(widgetSelectedAdapter(e -> showAngle2Details()));
		GridDataFactory.swtDefaults().applyTo(moreDetailsButton);
		moreDetailsButton.setEnabled(shouldCreateSeparator);
	}

	private void showAngle2Details() {
		tomoPathDetailsDialog = new Angle2StepSizesDialog(getShell(), getEclipseContext(),
				getBean(), getView().getPathInfo(true));
		tomoPathDetailsDialog.addAngleModelChangeListener(modelTypeChanged -> anglePathsChanged(modelTypeChanged, true));
		tomoPathDetailsDialog.open();
		tomoPathDetailsDialog = null;
	}

	private void anglePathsChanged(boolean modelTypeChanged, boolean external) {
		getView().updatePoints();

		if (modelTypeChanged) {
			if (external) {
				tomoAngleEditorsBlock.updateControls();
			}
			getView().relayout();
		}
	}

	public void updatePathInfo(TensorTomoPathInfo pathInfo) {
		final String angle2LabelText = getAngle2LabelText(pathInfo);
		angle2StepsLabel.setText(angle2LabelText);
		angle2StepsLabel.setToolTipText(angle2LabelText);
		moreDetailsButton.setEnabled(true);
		if (tomoPathDetailsDialog != null) {
			tomoPathDetailsDialog.updatePathInfo(pathInfo);
		}
	}

	private String getAngle2LabelText(TensorTomoPathInfo pathInfo) {
		final StepSizes angle2StepSizes = pathInfo.getAngle2StepSizes();
		final double[] angle1Positions = pathInfo.getAngle1Positions();

		return switch (angle2StepSizes.getRank()) {
			case 0:
				final double[] angle2Positions = pathInfo.getAngle2Positions()[0]; // same for each angle1 posAn
				yield ANGLE2_STEPS_ARRAY_LIST_LABEL + formatDoubles(angle2Positions);
			case 1:
				final double[] angle2StepSizes1DArr = angle2StepSizes.getOneDStepSizes();
				yield ANGLE2_STEP_SIZES_LABEL + IntStream.range(0, angle1Positions.length)
						.mapToObj(i -> formatDouble(angle1Positions[i]) + ":" + formatDouble(angle2StepSizes1DArr[i]))
						.collect(joining(DELIMITER));
			case 2:
				double[][] angle2StepSizes2DArr = angle2StepSizes.getTwoDStepSizes();
				yield ANGLE2_STEP_SIZES_LABEL + IntStream.range(0, angle1Positions.length)
						.mapToObj(i -> formatDouble(angle1Positions[i]) + ":" + formatDoubles(angle2StepSizes2DArr[i]))
						.collect(joining(DELIMITER));
			default: throw new IllegalArgumentException("step size rank not expected " + angle2StepSizes.getRank());
		};
	}

	public static String formatDouble(double doubleVal) {
		return DECIMAL_FORMAT.format(doubleVal);
	}

	public static String formatDoubles(double[] values) {
		return Arrays.stream(values).mapToObj(TomoPathSection::formatDouble).collect(Collectors.joining(DELIMITER));
	}

	@Override
	public void updateControls() {
		// TODO fix this method **************
//		createSectionContent();
		getView().relayout();
	}

	@Override
	public void dispose() {
		super.dispose();
		binder.dispose();
	}

}
