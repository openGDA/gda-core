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

import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.ANGLE_1_LABEL;
import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.ANGLE_2_LABEL;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.ui.experiment.DataBinder;
import uk.ac.diamond.daq.mapping.ui.path.AbstractAxialPathEditor;
import uk.ac.diamond.daq.mapping.ui.path.PathEditorProvider;

class TomoPathSection extends AbstractTomoViewSection {

	private enum AxialPathModelType {
		STEP("Step", AxialStepModel.class),
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

	private PropertyChangeListener anglePathBeanPropertyChangeListener = evt -> getView().updatePoints();

	private Composite sectionComposite;
	private Composite pathControlsComposite;

	private final DataBinder binder = new DataBinder();
	private AbstractAxialPathEditor<? extends IAxialModel> angle1PathEditor;
	private AbstractAxialPathEditor<? extends IAxialModel> angle2PathEditor;

	private Map<AxialPathModelType, IAxialModel> angle1PathModels;
	private Map<AxialPathModelType, IAxialModel> angle2PathModels;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		initializeAnglePathModelMaps();

		sectionComposite = createComposite(parent, 1, true);
		createSectionLabel(sectionComposite);

		createAnglePathEditors();
	}

	private void initializeAnglePathModelMaps() {
		angle1PathModels = new EnumMap<>(AxialPathModelType.class);
		final IAxialModel angle1Model = getBean().getAngle1Model().getModel();
		angle1PathModels.put(AxialPathModelType.forModel(angle1Model), angle1Model);
		angle1Model.addPropertyChangeListener(anglePathBeanPropertyChangeListener);

		angle2PathModels = new EnumMap<>(AxialPathModelType.class);
		final IAxialModel angle2Model = getBean().getAngle2Model().getModel();
		angle2PathModels.put(AxialPathModelType.forModel(angle2Model), angle2Model);
		angle2Model.addPropertyChangeListener(anglePathBeanPropertyChangeListener);
	}

	private void createAnglePathEditors() {
		if (pathControlsComposite != null) pathControlsComposite.dispose();
		if (angle1PathEditor != null) angle1PathEditor.dispose();
		if (angle2PathEditor != null) angle2PathEditor.dispose();

		pathControlsComposite = createComposite(sectionComposite, 3, false);
		angle1PathEditor = createAngleEditorRow(pathControlsComposite, ANGLE_1_LABEL, getBean().getAngle1Model());
		angle2PathEditor = createAngleEditorRow(pathControlsComposite, ANGLE_2_LABEL, getBean().getAngle2Model());

		final Label label = new Label(pathControlsComposite, SWT.NONE);
		label.setText("Specify the " + ANGLE_2_LABEL + " path for " + ANGLE_1_LABEL + " = 0. The path for other values of " + ANGLE_1_LABEL + " is calculated (except 'List of points').");
		GridDataFactory.fillDefaults().span(3, 1).applyTo(label);
	}

	private AbstractAxialPathEditor<? extends IAxialModel> createAngleEditorRow(Composite parent,
				String angleLabel, IScanModelWrapper<IAxialModel> angleModelWrapper) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(angleLabel);

		final IAxialModel angleModel = angleModelWrapper.getModel();
		final AxialPathModelType modelType = AxialPathModelType.forModel(angleModel);
		final ComboViewer pathTypeCombo = new ComboViewer(parent, SWT.READ_ONLY);
		pathTypeCombo.setContentProvider(ArrayContentProvider.getInstance());
		pathTypeCombo.setLabelProvider(LabelProvider.createTextProvider(
				obj -> ((AxialPathModelType) obj).getLabel()));
		pathTypeCombo.setInput(AxialPathModelType.values());
		pathTypeCombo.setSelection(new StructuredSelection(modelType));
		pathTypeCombo.addSelectionChangedListener(evt -> pathTypeSelected(angleLabel,
				(AxialPathModelType) evt.getStructuredSelection().getFirstElement(), angleModelWrapper));

		return createAngleEditor(parent, angleModel);
	}

	private AbstractAxialPathEditor<? extends IAxialModel> createAngleEditor(Composite parent,
			final IAxialModel angleModel) {
		final AbstractAxialPathEditor<? extends IAxialModel> pathEditor =
				(AbstractAxialPathEditor<? extends IAxialModel>) PathEditorProvider.createPathComposite(
						angleModel, getEclipseContext());

		pathEditor.createEditorPart(parent);
		return pathEditor;
	}

	private void pathTypeSelected(String angleLabel, AxialPathModelType pathType,
			IScanModelWrapper<IAxialModel> angleModelWrapper) {
		final IAxialModel oldModel = angleModelWrapper.getModel();
		oldModel.removePropertyChangeListener(anglePathBeanPropertyChangeListener);

		final Map<AxialPathModelType, IAxialModel> pathModelsForAngle = getPathModelsForAngle(angleLabel);
		final IAxialModel newModel = pathModelsForAngle.computeIfAbsent(pathType,
				type -> createNewModel(type, oldModel));
		angleModelWrapper.setModel(newModel);
		newModel.addPropertyChangeListener(anglePathBeanPropertyChangeListener);

		createAnglePathEditors();
		getView().updatePoints(); // TODO we only need to update the outer point calculation, not the map points
		getView().relayout();
	}

	private Map<AxialPathModelType, IAxialModel> getPathModelsForAngle(String angleLabel) {
		switch (angleLabel) {
			case ANGLE_1_LABEL: return angle1PathModels;
			case ANGLE_2_LABEL: return angle2PathModels;
			default:
				throw new IllegalArgumentException("Invalid angle label: " + angleLabel); // not expected
		}
	}

	private IAxialModel createNewModel(AxialPathModelType pathType, IAxialModel oldModel) {
		// TODO better defaults, taking existing model into consideration?
		final String angleName = oldModel.getAxisName();
		switch (pathType) {
			case STEP: return new AxialStepModel(angleName, 0, 60, 10);
			case POINTS: return new AxialPointsModel(angleName, 0, 60, 7);
			case ARRAY: return new AxialArrayModel(angleName, 0, 10, 20, 30, 40, 50, 60);
			case MULTI_STEP: return new AxialMultiStepModel(angleName, 0, 60, 10);
			default: throw new IllegalArgumentException("Unknown path type: " + pathType);
		}
	}

	private void createSectionLabel(final Composite parent) {
		final Label sectionLabel = new Label(parent, SWT.NONE);
		sectionLabel.setText("Tomography Setup");
	}

	@Override
	public void updateControls() {
		createAnglePathEditors();
		getView().relayout();
	}

	@Override
	public void dispose() {
		super.dispose();
		binder.dispose();
	}

}
