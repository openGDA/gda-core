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

import static java.util.Comparator.comparing;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_CAMERA;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.device.ui.device.EditDetectorModelDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.Activator;

public class MalcolmDeviceSection extends AbstractTomoViewSection {

	private static final Logger logger = LoggerFactory.getLogger(MalcolmDeviceSection.class);

	private IRunnableDeviceService runnableDeviceService;

	private ComboViewer malcolmDeviceCombo;

	private Text exposureTimeText;

	private Binding exposureTimeBinding;

	private DeviceInformation<IMalcolmModel> selectedMalcolmDevice;

	@Override
	public void initialize(TensorTomoScanSetupView view) {
		super.initialize(view);

		runnableDeviceService = getRemoteService(IRunnableDeviceService.class);
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		final Composite composite = createComposite(parent, 2, true);

		createMalcolmDeviceChoiceRow(composite);
		createExposureTimeRow(composite);

		populateMalcolmDeviceCombo();
	}

	private void createMalcolmDeviceChoiceRow(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("Malcolm Device:");

		final Composite malcolmDeviceComposite = createComposite(parent, 2, false);

		malcolmDeviceCombo = new ComboViewer(malcolmDeviceComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(malcolmDeviceCombo.getControl());
		malcolmDeviceCombo.setContentProvider(ArrayContentProvider.getInstance());
		malcolmDeviceCombo.setLabelProvider(LabelProvider.createTextProvider(
				element -> ((DeviceInformation<?>) element).getLabel()));
		malcolmDeviceCombo.addSelectionChangedListener(this::malcolmDeviceSelected);

		final Button configButton = new Button(malcolmDeviceComposite, SWT.PUSH);
		configButton.setImage(Activator.getImage(IMG_CAMERA));
		GridDataFactory.swtDefaults().applyTo(configButton);
		configButton.addSelectionListener(widgetSelectedAdapter(e -> configureMalcolmDevice()));
	}

	private void populateMalcolmDeviceCombo() {
		try {
			final List<DeviceInformation<?>> malcolmDeviceInfos =
					runnableDeviceService.getDeviceInformation(DeviceRole.MALCOLM).stream()
					.sorted(comparing(DeviceInformation::getLabel)).toList();
			malcolmDeviceCombo.setInput(malcolmDeviceInfos);
			if (malcolmDeviceInfos.isEmpty()) {
				MessageDialog.openError(getShell(), "Tensor Tomograpy Setup", "No malcolm devices available.");
			}
			setSelectedMalcolmDevice();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Tensor Tomograpy Setup", "Could not get malcolm devices: " + e + "\n. See error log for details");
			logger.error("Could not get malcolm devices", e);
		}
	}

	private void setSelectedMalcolmDevice() {
		final String malcolmDeviceName = getBean().getMalcolmDeviceName();
		final IMalcolmModel malcolmModel = getBean().getMalcolmModel();

		@SuppressWarnings("unchecked")
		final List<DeviceInformation<IMalcolmModel>> malcolmDeviceInfos =
				(List<DeviceInformation<IMalcolmModel>>) malcolmDeviceCombo.getInput();
		disableExcludedDetectors(malcolmDeviceInfos);

		// get the device info for the malcolm device in the tomo scan bean
		final Optional<DeviceInformation<IMalcolmModel>> optInfo = malcolmDeviceInfos.stream()
				.filter(devInfo -> devInfo.getName().equals(malcolmDeviceName))
				.findFirst();

		final DeviceInformation<IMalcolmModel> malcolmDevInfo;
		if (optInfo.isPresent()) {
			malcolmDevInfo = optInfo.get();
			if (malcolmModel != null) {
				// overwrite the model of the selected malcolm device with the model from the bean
				malcolmDevInfo.setModel(malcolmModel);
			}
		} else {
			// no malcolm device previously selected, just use the first one
			malcolmDevInfo = malcolmDeviceInfos.get(0);
		}

		malcolmDeviceCombo.setSelection(new StructuredSelection(malcolmDevInfo));
	}

	private void disableExcludedDetectors(List<DeviceInformation<IMalcolmModel>> malcolmDevInfos) {
		// disable any detectors whose name is in the list of excluded detector names
		final Set<String> excludedDetectorNames = getBean().getExcludedDetectorNames();

		malcolmDevInfos.stream()
			.map(DeviceInformation::getModel)
			.map(IMalcolmModel::getDetectorModels)
			.flatMap(List::stream)
			.filter(detModel -> excludedDetectorNames.contains(detModel.getName()))
			.forEach(detModel -> detModel.setEnabled(false));
	}

	private void createExposureTimeRow(final Composite parent) {
		final Label exposureTimeLabel = new Label(parent, SWT.NONE);
		exposureTimeLabel.setText("Exposure Time:");
		GridDataFactory.swtDefaults().applyTo(exposureTimeLabel);

		exposureTimeText = new Text(parent, SWT.BORDER);
		exposureTimeText.setToolTipText("Set the exposure time for the detector");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);

		exposureTimeText.addModifyListener(event -> getView().updateStatusLabel());
	}

	private void bindExposureTimeTextToMalcolmModel() {
		if (exposureTimeText == null) return;
		if (exposureTimeBinding != null) exposureTimeBinding.dispose();

		final IObservableValue<String> exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
		final IObservableValue<Double> exposureTimeValue = PojoProperties.value("exposureTime", Double.class).observe(getBean().getMalcolmModel());
		exposureTimeBinding = getDataBindingContext().bindValue(exposureTextValue, exposureTimeValue);
	}

	private void configureMalcolmDevice() {
		final DeviceInformation<IMalcolmModel> malcolmInfo = getSelectedMalcolmDevice(malcolmDeviceCombo.getStructuredSelection());
		final IMalcolmModel malcolmModel = malcolmInfo.getModel();

		final Dialog editModelDialog = new EditDetectorModelDialog(getShell(),
				runnableDeviceService, malcolmModel, malcolmInfo.getLabel());
		editModelDialog.create();
		if (editModelDialog.open() == Window.OK) {
			getDataBindingContext().updateTargets();
		}
	}

	private void malcolmDeviceSelected(SelectionChangedEvent event) {
		selectedMalcolmDevice = getSelectedMalcolmDevice(event.getStructuredSelection());
		getBean().setMalcolmDeviceName(selectedMalcolmDevice.getName());
		getBean().setMalcolmModel(selectedMalcolmDevice.getModel());
		bindExposureTimeTextToMalcolmModel();
		getView().redrawMapSection();
	}

	public DeviceInformation<IMalcolmModel> getSelectedMalcolmDevice() {
		return selectedMalcolmDevice;
	}

	@SuppressWarnings("unchecked")
	private DeviceInformation<IMalcolmModel> getSelectedMalcolmDevice(IStructuredSelection selection) {
		return (DeviceInformation<IMalcolmModel>) selection.getFirstElement();
	}

	@Override
	public void updateControls() {
		setSelectedMalcolmDevice();
	}

}
