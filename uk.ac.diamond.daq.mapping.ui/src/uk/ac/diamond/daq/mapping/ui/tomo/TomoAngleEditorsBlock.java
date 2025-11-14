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

import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.TomoAngle.ANGLE_1;
import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.TomoAngle.ANGLE_2;

import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import uk.ac.diamond.daq.mapping.api.TensorTomoScanBean;
import uk.ac.diamond.daq.mapping.ui.path.AbstractAxialPathEditor;
import uk.ac.diamond.daq.mapping.ui.path.PathEditorProvider;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.TomoAngle;
import uk.ac.diamond.daq.mapping.ui.tomo.TomoPathSection.AxialPathModelType;

class TomoAngleEditorsBlock {

	public interface TomoAnglePathModelChangeListener {
		void angleModelChanged(boolean modelTypeChanged);
	}

	private TensorTomoScanBean scanBean;

	private final Map<AxialPathModelType, IAxialModel> angle1PathModels;
	private final Map<AxialPathModelType, IAxialModel> angle2PathModels;

	private AbstractAxialPathEditor<? extends IAxialModel> angle1PathEditor;
	private AbstractAxialPathEditor<? extends IAxialModel> angle2PathEditor;

	private IEclipseContext eclipseContext;

	private PropertyChangeListener angleModelPropertyChangeListener = evt -> angleModelChanged(false);

	private TomoAnglePathModelChangeListener angleModelChangeListener = null;

	private Composite blockAreaComposite;
	private Composite angleEditorsComposite;

	TomoAngleEditorsBlock(TensorTomoScanBean scanBean, IEclipseContext eclipseContext) {
		this.scanBean = scanBean;
		this.eclipseContext = eclipseContext;

		angle1PathModels = initializeAngleModelMap(ANGLE_1.getModel(scanBean));
		angle2PathModels = initializeAngleModelMap(ANGLE_2.getModel(scanBean));
	}

	private Map<AxialPathModelType, IAxialModel> initializeAngleModelMap(IAxialModel angleModel) {
		final Map<AxialPathModelType, IAxialModel> angleModelMap = new EnumMap<>(AxialPathModelType.class);
		angleModelMap.put(AxialPathModelType.forModel(angleModel), angleModel);
		return angleModelMap;
	}

	public Composite createControls(Composite parent) {
		blockAreaComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(blockAreaComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(blockAreaComposite);

		createAngleEditors();

		return blockAreaComposite;
	}

	private void createAngleEditors() {
		if (angleEditorsComposite != null) angleEditorsComposite.dispose();
		if (angle1PathEditor != null) angle1PathEditor.dispose();
		if (angle2PathEditor != null) angle2PathEditor.dispose();

		angleEditorsComposite = new Composite(blockAreaComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(angleEditorsComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(angleEditorsComposite);

		angle1PathEditor = createAngleEditorRow(angleEditorsComposite, ANGLE_1);
		angle2PathEditor = createAngleEditorRow(angleEditorsComposite, ANGLE_2);
	}

	private AbstractAxialPathEditor<? extends IAxialModel> createAngleEditorRow(Composite parent, TomoAngle tomoAngle) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(String.format("%s (%s):", tomoAngle.getLabel(), tomoAngle.getScannableName(scanBean)));

		final IAxialModel angleModel = tomoAngle.getModel(scanBean);
		final AxialPathModelType modelType = AxialPathModelType.forModel(angleModel);
		final ComboViewer pathTypeCombo = new ComboViewer(parent, SWT.READ_ONLY);
		pathTypeCombo.setContentProvider(ArrayContentProvider.getInstance());
		pathTypeCombo.setLabelProvider(LabelProvider.createTextProvider(
				obj -> ((AxialPathModelType) obj).getLabel()));
		pathTypeCombo.setInput(AxialPathModelType.values());
		pathTypeCombo.setSelection(new StructuredSelection(modelType));
		pathTypeCombo.addSelectionChangedListener(evt -> pathTypeSelected(tomoAngle,
				(AxialPathModelType) evt.getStructuredSelection().getFirstElement(), tomoAngle.getModelWrapper(scanBean)));

		final AbstractAxialPathEditor<? extends IAxialModel> pathEditor =
				(AbstractAxialPathEditor<? extends IAxialModel>) PathEditorProvider.createPathComposite(angleModel, eclipseContext);
		pathEditor.createEditorPart(parent);
		angleModel.addPropertyChangeListener(angleModelPropertyChangeListener);
		return pathEditor;
	}

	private void pathTypeSelected(TomoAngle tomoAngle, AxialPathModelType pathType,
			IScanModelWrapper<IAxialModel> angleModelWrapper) {
		final IAxialModel oldModel = angleModelWrapper.getModel();
		oldModel.removePropertyChangeListener(angleModelPropertyChangeListener);

		final Map<AxialPathModelType, IAxialModel> pathModelsForAngle = switch (tomoAngle) {
			case ANGLE_1 -> angle1PathModels;
			case ANGLE_2 -> angle2PathModels;
			default -> throw new IllegalArgumentException(); // not possible
		};

		final IAxialModel newModel = pathModelsForAngle.computeIfAbsent(pathType, type -> createNewModel(type, oldModel));
		newModel.addPropertyChangeListener(angleModelPropertyChangeListener);
		angleModelWrapper.setModel(newModel);

		createAngleEditors();

		angleModelChanged(true);
	}

	private IAxialModel createNewModel(AxialPathModelType pathType, IAxialModel oldModel) {
		final String angleName = oldModel.getAxisName();
		return switch (pathType) {
			case STEP -> new AxialStepModel(angleName, 0, 60, 10);
			case POINTS -> new AxialPointsModel(angleName, 0, 60, 7);
			case ARRAY -> new AxialArrayModel(angleName, 0, 10, 20, 30, 40, 50, 60);
			case MULTI_STEP -> new AxialMultiStepModel(angleName, 0, 60, 10);
			default -> throw new IllegalArgumentException("Unknown path type: " + pathType);
		};
	}

	public void addAngleModelChangeListener(TomoAnglePathModelChangeListener listener) {
		angleModelChangeListener = listener;
	}

	private void angleModelChanged(boolean modelTypeChanged) {
		if (angleModelChangeListener != null) {
			angleModelChangeListener.angleModelChanged(modelTypeChanged);
		}
	}

	public void setScanBean(TensorTomoScanBean scanBean) {
		this.scanBean = scanBean;
	}

	public void updateControls() {
		createAngleEditors();
	}

	public void dispose() {
		ANGLE_1.getModel(scanBean).removePropertyChangeListener(angleModelPropertyChangeListener);
		ANGLE_2.getModel(scanBean).removePropertyChangeListener(angleModelPropertyChangeListener);
	}
}
