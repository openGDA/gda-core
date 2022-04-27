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

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionPathModelEditor;
import uk.ac.diamond.daq.mapping.ui.path.AbstractGridPathEditor;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathEditor.PathOption;
import uk.ac.diamond.daq.mapping.ui.path.PathEditorProvider;
import uk.ac.diamond.daq.mapping.ui.region.RegionEditorProvider;

abstract class AbstractRegionAndPathSection extends AbstractTomoViewSection {

	protected enum RegionAndPathType {
		MAP, TOMO;
	}

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

	protected static class RegionAndPathConfig {
		protected RegionAndPathType type;
		protected String axis1Name;
		protected String axis2Name;
		protected IMappingScanRegionShape regionModel;
		protected AbstractTwoAxisGridModel pathModel;
		protected String units;
	}

	private static final Logger logger = LoggerFactory.getLogger(AbstractRegionAndPathSection.class);

	private final RegionAndPathConfig config;

	private final Map<GridPathType, AbstractTwoAxisGridModel> pathModels;

	private Composite pathEditorComposite;

	private AbstractGridPathEditor pathEditor;

	protected AbstractRegionAndPathSection(TensorTomoScanSetupView tomoView) {
		super(tomoView);
		this.config = createRegionAndPathConfig();
		pathModels = initializePathModels();
	}

	protected abstract RegionAndPathConfig createRegionAndPathConfig();

	private Map<GridPathType, AbstractTwoAxisGridModel> initializePathModels() {
		final Map<GridPathType, AbstractTwoAxisGridModel> pathModels = new EnumMap<>(GridPathType.class);
		final GridPathType initialType = GridPathType.forModelClass(config.pathModel.getClass());
		for (GridPathType gridPathType : GridPathType.values()) {
			try {
				final AbstractTwoAxisGridModel pathModel;
				if (gridPathType == initialType) {
					pathModel = config.pathModel;
				} else {
					pathModel = gridPathType.modelClass.getDeclaredConstructor().newInstance(); // TODO this isn't right for tomo
					pathModel.setxAxisName(config.axis1Name);
					pathModel.setyAxisName(config.axis2Name);
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
		createSeparator(parent);

		final Composite composite = createComposite(parent, 1, true);
		createUpperControls(composite);
		createRegionAndPathEditors(composite);
	}

	protected void createUpperControls(final Composite composite) {
		final Label label = new Label(composite, SWT.NONE);
		label.setText(getSectionLabel());
	}

	protected abstract String getSectionLabel();

	protected void createRegionAndPathEditors(final Composite parent) {
		final Composite editorsComposite = createComposite(parent, 2, false);
		((GridLayout) editorsComposite.getLayout()).horizontalSpacing = 10;

		createRegionEditor(editorsComposite);
		createPathEditorArea(editorsComposite);
	}

	private void createPathEditorArea(final Composite parent) {
		final Composite composite = createComposite(parent, 1, false);

		final Composite pathTypeChoiceComposite = createComposite(composite, 3, false);
		final Label pathTypeLabel = new Label(pathTypeChoiceComposite, SWT.NONE);
		pathTypeLabel.setText("Path Type:");

		final GridPathType initialPathType = GridPathType.forModelClass(config.pathModel.getClass());
		for (GridPathType gridPathType : GridPathType.values()) {
			final Button gridPathTypeButton = new Button(pathTypeChoiceComposite, SWT.RADIO);
			gridPathTypeButton.setText(gridPathType.label);
			gridPathTypeButton.setSelection(gridPathType == initialPathType);
			gridPathTypeButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
					e -> { if (gridPathTypeButton.getSelection()) gridPathTypeSelected(gridPathType); }));
		}

		// grid model is the step for each axis
		pathEditorComposite = createComposite(composite, 1, false);
		createPathEditor();
	}

	private void gridPathTypeSelected(GridPathType gridPathType) {
		config.pathModel = pathModels.get(gridPathType);
		updateBeanWithGridPath(config.pathModel);
		createPathEditor();
		tomoView.relayout();
	}

	private void createPathEditor() {
		if (pathEditor != null) {
			pathEditor.dispose();
		}

		pathEditor = (AbstractGridPathEditor) PathEditorProvider.createPathComposite(
				config.pathModel, getEclipseContext());
		pathEditor.setAxisNames(config.axis1Name, config.axis2Name);
		pathEditor.setOptionsToDisplay(getPathOptions());
		pathEditor.createEditorPart(pathEditorComposite);
	}

	protected abstract void updateBeanWithGridPath(AbstractTwoAxisGridModel pathModel);

	protected abstract Set<PathOption> getPathOptions();

	private void createRegionEditor(final Composite parent) {
		// Region is the start/stop for each axis
		final Map<String, String> regionUnits = Map.of(config.axis1Name, config.units, config.axis2Name, config.units);
		final AbstractRegionPathModelEditor<IMappingScanRegionShape> regionEditor = RegionEditorProvider.createRegionEditor(
				config.regionModel, regionUnits, getEclipseContext());
		regionEditor.setAxisNames(config.axis1Name, config.axis2Name);
		regionEditor.setUnitsEditable(false);
		regionEditor.createEditorPart(parent);
	}

	@Override
	public void configureScanBean(ScanBean scanBean) {
		// nothing to do - creating the CompoundModel is done by the view
	}

}
