/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A section containing the button to launch a scan.
 */
public class SubmitScanSection extends AbstractMappingSection {

	SubmitScanSection(MappingExperimentView mappingView, IEclipseContext context) {
		super(mappingView, context);
	}

	private static final Logger logger = LoggerFactory.getLogger(SubmitScanSection.class);

	@Override
	public void createControls(Composite parent) {
		Composite submitScanComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(submitScanComposite);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(submitScanComposite);

		Button scanButton = new Button(submitScanComposite, SWT.NONE);
		scanButton.setText("Queue Scan");

		final ScanRequestConverter converter = context.get(ScanRequestConverter.class);
		final ScanBeanSubmitter submitter = context.get(ScanBeanSubmitter.class);
		scanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					ScanBean scanBean = converter.convertToScanBean(mappingBean);
					submitter.submitScan(scanBean);
				} catch (Exception e) {
					logger.warn("Scan submission failed", e);
				}
			}
		});
	}

}
