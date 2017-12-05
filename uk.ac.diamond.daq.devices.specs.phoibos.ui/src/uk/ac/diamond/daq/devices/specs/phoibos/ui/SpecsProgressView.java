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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;

public class SpecsProgressView implements IObserver {

	private ProgressBar progressBar;
	private ISpecsPhoibosAnalyser analyser;

	private Text positionText;
	private Text regionNameText;

	@PostConstruct
	void createView(Composite parent) {

		List<ISpecsPhoibosAnalyser> analysers = Finder.getInstance()
				.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			throw new RuntimeException("No Analyser was found! (Or more than 1)");
		}
		analyser = analysers.get(0);

		analyser.addIObserver(this);

		Composite controlArea = new Composite(parent, SWT.None);
		controlArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		controlArea.setLayout(new GridLayout(4, false));
		controlArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Label positionLabel = new Label(controlArea, SWT.NONE);
		positionLabel.setText("Position in Sequence:");
		positionText = new Text(controlArea, SWT.BORDER);
		positionText.setEditable(false);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(positionText);

		Label regionNameLabel = new Label(controlArea, SWT.NONE);
		regionNameLabel.setText("Region Name:");
		regionNameText = new Text(controlArea, SWT.BORDER);
		regionNameText.setEditable(false);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(regionNameText);

		Label lblProgress = new Label(controlArea, SWT.NONE);
		lblProgress.setText("Progress in Region:");
		progressBar = new ProgressBar(controlArea, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(progressBar);
	}

	@Override
	public void update(Object source, Object arg) {

		if (arg instanceof SpecsPhoibosLiveDataUpdate) {
			SpecsPhoibosLiveDataUpdate evt = (SpecsPhoibosLiveDataUpdate) arg;
			Display.getDefault().asyncExec(() -> {
				progressBar.setMaximum(evt.getTotalPoints());
				progressBar.setSelection(evt.getCurrentPoint());
				regionNameText.setText(evt.getRegionName());
				positionText.setText(evt.getPositionString());
			});
		}
	}

	@PreDestroy
	void dispose() {
		analyser.deleteIObserver(this);
	}

}
