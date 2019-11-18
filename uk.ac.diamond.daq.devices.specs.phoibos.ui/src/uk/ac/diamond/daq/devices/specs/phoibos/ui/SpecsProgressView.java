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
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceFileUpdate;

/**
 * Class to show the current progress of the SPECS analyser with bars for
 * overall region progress and for progress in the current iteration
 */

public class SpecsProgressView implements IObserver {

	private ProgressBar regionProgressBar;
	private ProgressBar iterationProgressBar;
	private ISpecsPhoibosAnalyser analyser;

	private Text positionText;
	private Text regionNameText;
	private Text iterationNumberText;
	private Text sequenceFileText;

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

		Label sequenceFileLabel = new Label(controlArea, SWT.NONE);
		sequenceFileLabel.setText("Sequence File:");
		sequenceFileText = new Text(controlArea, SWT.BORDER);
		sequenceFileText.setEditable(false);
		sequenceFileText.setEnabled(false);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(sequenceFileText);

		Label regionNameLabel = new Label(controlArea, SWT.NONE);
		regionNameLabel.setText("Region Name:");
		regionNameText = new Text(controlArea, SWT.BORDER);
		regionNameText.setEditable(false);
		regionNameText.setEnabled(false);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(regionNameText);

		Label lblRegionProgress = new Label(controlArea, SWT.NONE);
		lblRegionProgress.setText("Region Progress:");
		regionProgressBar = new ProgressBar(controlArea, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(regionProgressBar);

		Label positionLabel = new Label(controlArea, SWT.NONE);
		positionLabel.setText("Position in Sequence:");
		positionText = new Text(controlArea, SWT.BORDER);
		positionText.setEditable(false);
		positionText.setEnabled(false);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(positionText);

		Label iterationNumberLabel = new Label(controlArea, SWT.NONE);
		iterationNumberLabel.setText("Iteration Number:");
		iterationNumberText = new Text(controlArea, SWT.BORDER);
		iterationNumberText.setEditable(false);
		iterationNumberText.setEnabled(false);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(iterationNumberText);

		Label lblIterProgress = new Label(controlArea, SWT.NONE);
		lblIterProgress.setText("Iteration Progress:");
		iterationProgressBar = new ProgressBar(controlArea, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(1, 1).applyTo(iterationProgressBar);
	}

	@Override
	public void update(Object source, Object arg) {

		if (arg instanceof SpecsPhoibosLiveDataUpdate) {
			SpecsPhoibosLiveDataUpdate event = (SpecsPhoibosLiveDataUpdate) arg;
			final int pointsPerIter = event.getTotalPoints() / event.getTotalIterations();
			final int iterationNumber = (event.getCurrentPoint() -  1) / pointsPerIter + 1;
			final String iterationString = iterationNumber + " of " + event.getTotalIterations();
			Display.getDefault().asyncExec(() -> {
				regionProgressBar.setMaximum(event.getTotalPoints());
				regionProgressBar.setSelection(event.getCurrentPoint());
				regionNameText.setText(event.getRegionName());
				positionText.setText(event.getPositionString());
				iterationProgressBar.setMaximum(pointsPerIter);
				iterationProgressBar.setSelection(event.getcurrentPointInIteration());
				iterationNumberText.setText(iterationString);
			});

		} else if (arg instanceof SpecsPhoibosSequenceFileUpdate) {
			SpecsPhoibosSequenceFileUpdate event = (SpecsPhoibosSequenceFileUpdate) arg;
			Display.getDefault().asyncExec(() -> {
				if (event.getFilePath() == null || event.getFilePath().isEmpty()) {
					sequenceFileText.setText("Current sequence not saved");
				} else {
					sequenceFileText.setText(event.getFilePath());
				}
			});
		}
	}

	@PreDestroy
	void dispose() {
		analyser.deleteIObserver(this);
	}

}
