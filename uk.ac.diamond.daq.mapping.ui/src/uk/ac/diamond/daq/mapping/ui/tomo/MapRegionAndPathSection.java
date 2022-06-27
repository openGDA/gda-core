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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.ui.path.AbstractGridPathEditor;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathEditor.CommonPathOption;
import uk.ac.diamond.daq.mapping.ui.path.PathEditorProvider;
import uk.ac.diamond.daq.mapping.ui.region.RectangleRegionEditor;
import uk.ac.diamond.daq.mapping.ui.region.RegionEditorProvider;

class MapRegionAndPathSection extends AbstractTomoViewSection {

	protected enum GridPathType {
		NUM_STEPS("Num. points", TwoAxisGridPointsModel.class),
		STEP_SIZE("Step size", TwoAxisGridStepModel.class);

		protected String label;
		protected Class<? extends AbstractTwoAxisGridModel> modelClass;
		private GridPathType(String label, Class<? extends AbstractTwoAxisGridModel> modelClass) {
			this.label = label;
			this.modelClass = modelClass;
		}
		public static GridPathType forModelClass(Class<? extends AbstractTwoAxisGridModel> modelClass) {
			for (GridPathType type : GridPathType.values()) {
				if (type.modelClass.equals(modelClass))
					return type;
			}
			throw new IllegalArgumentException("Unknown model class: " + modelClass);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(MapRegionAndPathSection.class);

	private static final String UNITS_MILLIMETRES = "mm";

	private static final String X_AXIS_LABEL = "X";
	private static final String Y_AXIS_LABEL = "Y";

	private Map<GridPathType, AbstractTwoAxisGridModel> pathModels;

	private Composite regionEditorComposite;
	private Composite pathEditorComposite;

	private AbstractGridPathEditor<?> pathEditor;
	private RectangleRegionEditor regionEditor;

	private MappingStageInfo mappingStageInfo;

	private Map<GridPathType, Button> gridPathTypeRadioButtons;

	@Override
	public void initialize(TensorTomoScanSetupView view) {
		super.initialize(view);
		mappingStageInfo = getService(MappingStageInfo.class);
		pathModels = initializePathModels();
	}

	private Map<GridPathType, AbstractTwoAxisGridModel> initializePathModels() {
		final Map<GridPathType, AbstractTwoAxisGridModel> pathModels = new EnumMap<>(GridPathType.class);
		final GridPathType initialType = GridPathType.forModelClass(getBean().getGridPathModel().getClass());
		for (GridPathType gridPathType : GridPathType.values()) {
			try {
				final AbstractTwoAxisGridModel pathModel;
				if (gridPathType == initialType) {
					pathModel = getBean().getGridPathModel();
				} else {
					pathModel = gridPathType.modelClass.getDeclaredConstructor().newInstance();
					pathModel.setxAxisName(mappingStageInfo.getPlotXAxisName());
					pathModel.setyAxisName(mappingStageInfo.getPlotYAxisName());
				}
				pathModels.put(gridPathType, pathModel);
			} catch (Exception e) {
				logger.error("Could not create instance of model class {}", gridPathType.modelClass, e);
			}
		}

		return pathModels;
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		final Composite composite = createComposite(parent, 1, true);
		createUpperControls(composite);
		createRegionAndPathEditors(composite);
	}

	protected void createUpperControls(final Composite composite) {
		final Label label = new Label(composite, SWT.NONE);
		label.setText("Map Grid Setup");

		createDrawMapControl(composite);
	}

	private void createDrawMapControl(Composite parent) {
		final Composite composite = createComposite(parent, 2, false);

		final Label redrawLabel = new Label(composite, SWT.NONE);
		redrawLabel.setText("Click button to draw/redraw mapping region:");

		final Button redrawRegionButton = new Button(composite, SWT.NONE);
		redrawRegionButton.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/map--pencil.png")));
		redrawRegionButton.setToolTipText("Draw/Redraw region");
		redrawRegionButton.addSelectionListener(widgetSelectedAdapter(e -> getView().drawMappingRegion()));
	}

	protected void createRegionAndPathEditors(final Composite parent) {
		final Composite editorsComposite = createComposite(parent, 2, false);
		((GridLayout) editorsComposite.getLayout()).horizontalSpacing = 10;

		createRegionEditorArea(editorsComposite);
		createPathEditorArea(editorsComposite);
	}

	private void createPathEditorArea(final Composite parent) {
		final Composite composite = createComposite(parent, 1, false);

		final Composite pathTypeChoiceComposite = createComposite(composite, 3, false);
		final Label pathTypeLabel = new Label(pathTypeChoiceComposite, SWT.NONE);
		pathTypeLabel.setText("Path Type:");

		final GridPathType initialPathType = GridPathType.forModelClass(getBean().getGridPathModel().getClass());
		gridPathTypeRadioButtons = new EnumMap<>(GridPathType.class);
		for (GridPathType gridPathType : GridPathType.values()) {
			final Button gridPathTypeButton = new Button(pathTypeChoiceComposite, SWT.RADIO);
			gridPathTypeButton.setText(gridPathType.label);
			gridPathTypeButton.setSelection(gridPathType == initialPathType);
			gridPathTypeButton.addSelectionListener(widgetSelectedAdapter(
					e -> gridPathTypeSelected(gridPathType)));
			gridPathTypeRadioButtons.put(gridPathType, gridPathTypeButton);
		}

		// grid model is the step for each axis
		pathEditorComposite = createComposite(composite, 1, false);
		createPathEditor();
	}

	private void gridPathTypeSelected(GridPathType gridPathType) {
		if (gridPathTypeRadioButtons.get(gridPathType).getSelection()) {
			getBean().setGridPathModel(pathModels.get(gridPathType));
			createPathEditor();
			getView().relayout();
		}
	}

	private void createPathEditor() {
		if (pathEditor != null) {
			pathEditor.dispose();
		}

		pathEditor = (AbstractGridPathEditor<?>) PathEditorProvider.createPathComposite(
				getBean().getGridPathModel(), getEclipseContext());
		pathEditor.setAxisScannableNames(mappingStageInfo.getPlotXAxisName(), mappingStageInfo.getPlotYAxisName());
		pathEditor.setAxisLabels(X_AXIS_LABEL, Y_AXIS_LABEL);
		pathEditor.setOptionsToDisplay(Set.of(CommonPathOption.ALTERNATING));
		pathEditor.createEditorPart(pathEditorComposite);
	}

	private void createRegionEditorArea(final Composite parent) {
		regionEditorComposite = createComposite(parent, 1, false);

		createRegionEditor();
	}

	private void createRegionEditor() {
		if (regionEditor != null) {
			regionEditor.dispose();
		}

		// Region is the start/stop for each axis
		final String xAxisName = mappingStageInfo.getPlotXAxisName();
		final String yAxisName = mappingStageInfo.getPlotYAxisName();

		final Map<String, String> regionUnits = Map.of(xAxisName, UNITS_MILLIMETRES,
				yAxisName, UNITS_MILLIMETRES);
		regionEditor = (RectangleRegionEditor) RegionEditorProvider.createRegionEditor(
				getBean().getGridRegionModel(), regionUnits, getEclipseContext());
		regionEditor.setAxisScannableNames(xAxisName, yAxisName);
		regionEditor.setAxisLabels(X_AXIS_LABEL, Y_AXIS_LABEL);
		regionEditor.setUnitsEditable(false);
		regionEditor.createEditorPart(regionEditorComposite);
	}

	@Override
	public void updateControls() {
		createRegionEditor();
		createPathEditor();

		final GridPathType newGridPathType = GridPathType.forModelClass(getBean().getGridPathModel().getClass());
		Arrays.stream(GridPathType.values())
			.forEach(type -> gridPathTypeRadioButtons.get(type).setSelection(type == newGridPathType));
		getView().relayout();
	}

}
