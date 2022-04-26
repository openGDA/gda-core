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

import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

class AcquistionTimeSection extends AbstractTomoViewSection {

	public AcquistionTimeSection(TensorTomoScanSetupView tomoView) {
		super(tomoView);
	}

	@Override
	public void createControls(Composite parent) {
		createSeparator(parent);

		final Composite composite = createComposite(parent, 2, true);
		final Label label = new Label(composite, SWT.NONE);
		label.setText("Exposure time:");

		final Text exposureTimeText = new Text(composite, SWT.BORDER);
		exposureTimeText.setToolTipText("Set the exposure time for the detector");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);
		final IObservableValue<String> exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
		final IObservableValue<Double> exposureTimeValue = PojoProperties.value("exposureTime", Double.class).observe(getTomoBean());
		dataBindingContext.bindValue(exposureTextValue, exposureTimeValue);
		exposureTimeText.addModifyListener(event -> tomoView.updateStatusLabel());
	}

	@Override
	public void configureScanBean(ScanBean scanBean) {
		// set the exposure time
		final ScanRequest scanRequest = scanBean.getScanRequest();
		if (!scanRequest.getDetectors().isEmpty()) {
			final IDetectorModel detModel = scanRequest.getDetectors().values().iterator().next();
			detModel.setExposureTime(getTomoBean().getExposureTime());
		}
	}

}
