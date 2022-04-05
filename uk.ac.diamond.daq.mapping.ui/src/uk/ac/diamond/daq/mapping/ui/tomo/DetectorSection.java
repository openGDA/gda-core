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

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

public class DetectorSection extends AbstractTomoViewSection {

	private static final Logger logger = LoggerFactory.getLogger(DetectorSection.class);

	private final IRunnableDeviceService runnableDeviceService;

	private ComboViewer malcolmDeviceCombo;

	protected DetectorSection(TensorTomoScanSetupView tomoView) {
		super(tomoView);

		final IEventService eventService = getService(IEventService.class);
		try {
			final URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			runnableDeviceService = eventService.createRemoteService(jmsURI, IRunnableDeviceService.class);
		} catch (Exception e) {
			logger.error("Could not create DetectorSection", e);
			throw new RuntimeException("Could not create DetectorSection", e);
		}
	}

	@Override
	public void createControls(Composite parent) {
		// TODO merge with acquisition time section
		createSeparator(parent);

		final Composite composite = createComposite(parent, 2, true);
		final Label label = new Label(composite, SWT.NONE);
		label.setText("Malcolm Device:");

		malcolmDeviceCombo = new ComboViewer(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(malcolmDeviceCombo.getControl());
		malcolmDeviceCombo.setContentProvider(ArrayContentProvider.getInstance());
		malcolmDeviceCombo.setLabelProvider(LabelProvider.createTextProvider(
				element -> ((DeviceInformation<?>) element).getLabel()));

		populateDetectorCombo(malcolmDeviceCombo);
	}

	private void populateDetectorCombo(ComboViewer comboViewer) {
		try {
			final List<DeviceInformation<?>> malcolmDeviceInfos =
					runnableDeviceService.getDeviceInformation(DeviceRole.MALCOLM).stream()
					.sorted(Comparator.comparing(DeviceInformation::getLabel)).collect(toList());
			comboViewer.setInput(malcolmDeviceInfos);
			if (!malcolmDeviceInfos.isEmpty()) {
				comboViewer.setSelection(new StructuredSelection(malcolmDeviceInfos.get(0)));
			}
		} catch (Exception e) {
			logger.error("Could not get malcolm devices", e);
		}
	}

	@Override
	public void configureScanBean(ScanBean scanBean) {
		final DeviceInformation<IDetectorModel> malcolmDeviceInfo = getSelectedMalcolmDeviceInfo();
		final Map<String, IDetectorModel> detectorMap = new HashMap<>();
		if (malcolmDeviceInfo != null) {
			detectorMap.put(malcolmDeviceInfo.getName(), malcolmDeviceInfo.getModel());
		}
		scanBean.getScanRequest().setDetectors(detectorMap);
	}

	@SuppressWarnings("unchecked")
	private DeviceInformation<IDetectorModel> getSelectedMalcolmDeviceInfo() {
		return (DeviceInformation<IDetectorModel>) malcolmDeviceCombo.getStructuredSelection().getFirstElement();
	}

}
