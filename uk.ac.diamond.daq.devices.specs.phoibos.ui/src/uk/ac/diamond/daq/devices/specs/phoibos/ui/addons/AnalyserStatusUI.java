/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.addons;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyserStatus;

public class AnalyserStatusUI implements IObserver {

	public static final String CLASS_URI = "bundleclass://uk.ac.diamond.daq.devices.specs.phoibos.ui/" + AnalyserStatusUI.class.getName();
	public static final String ID = "uk.ac.diamond.daq.devices.specs.phoibos.ui.addon.analyserstatus";

	private static final Logger logger = LoggerFactory.getLogger(AnalyserStatusUI.class);

	private Text status;
	private ISpecsPhoibosAnalyserStatus specsStatus;


	public AnalyserStatusUI() {
		List<ISpecsPhoibosAnalyserStatus> analyserStatusList = Finder.listFindablesOfType(ISpecsPhoibosAnalyserStatus.class);
		if (analyserStatusList.size() != 1) {
			String msg = "No analyser status was found! (Or more than 1)";
			logger.error(msg);
			throw new IllegalStateException(msg);
		}
		specsStatus = analyserStatusList.get(0);
		specsStatus.addIObserver(this);
	}

	@PostConstruct
	public void createControl(Composite parent) {
		if (specsStatus != null) {
			Composite statusComposite = new Composite(parent, SWT.NONE);
			RowLayout layout = new RowLayout();
			layout.center = true;
			statusComposite.setLayout(layout);
			Label statusLabel = new Label(statusComposite, SWT.NONE);
			statusLabel.setText("SPECS status:");
			status = new Text(statusComposite, SWT.READ_ONLY);
			status.setLayoutData(new RowData(80, 20));
			status.setText(specsStatus.getCurrentPosition());
		}
	}

	@Override
	public void update(Object source, Object arg) {
		if (source == specsStatus && arg instanceof String) {
			Display.getDefault().asyncExec(() -> {
				status.setText((String)arg);
			});
		}
	}



}
