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

import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SampleNameSection extends AbstractTomoViewSection {

	private static final String DEFAULT_SAMPLE_NAME = "Unnamed sample";

	private Text sampleNameText;

	protected SampleNameSection(TensorTomoScanSetupView tomoView) {
		super(tomoView);
	}

	@Override
	public void createControls(Composite parent) {
		createSeparator(parent);

		final Composite composite = createComposite(parent, 2, true);
		final Label sampleNameLabel = new Label(composite, SWT.NONE);
		sampleNameLabel.setText("Sample Name:");
		GridDataFactory.swtDefaults().applyTo(sampleNameLabel);

		sampleNameText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleNameText);
	}

	@Override
	public void configureScanBean(ScanBean scanBean) {
		final String sampleName = getSampleName();

		final ScanMetadata sampleMetadata = new ScanMetadata(MetadataType.SAMPLE);
		sampleMetadata.addField("name", sampleName);
		scanBean.getScanRequest().setScanMetadata(Arrays.asList(sampleMetadata));
	}

	private String getSampleName() {
		final String sampleName = sampleNameText.getText().trim();
		return sampleName.isEmpty() ? DEFAULT_SAMPLE_NAME : sampleName;
	}

}
