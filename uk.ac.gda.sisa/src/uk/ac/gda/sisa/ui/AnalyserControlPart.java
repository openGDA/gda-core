/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.sisa.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.rcp.views.NudgePositionerComposite;

public class AnalyserControlPart {

	private static final Logger logger = LoggerFactory.getLogger(AnalyserControlPart.class);
	final Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	final Color green = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);

	private Composite parent;

	private Composite child;
	private Combo lensModeCombo;
	private Combo passEnergyCombo;
	private Text centreEnergyText;

	private IVGScientaAnalyser analyser;

	@Inject
	public AnalyserControlPart() {
		logger.trace("Constructor called");

		// Get an analyser
		List<IVGScientaAnalyser> analysers = Finder.getInstance().listLocalFindablesOfType(IVGScientaAnalyser.class);
		if (analysers.size() != 1) {
			String msg = "No Analyser was found! (Or more than 1)";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		analyser = analysers.get(0);
		logger.info("Connected to analyser {}", analyser);
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		logger.trace("postConstruct called");
		this.parent = parent;
		ScrolledComposite scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrollComp.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		this.child = new Composite(scrollComp, SWT.NONE);
		child.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(child);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(child);

		// Analyser group
		Group analyserGroup = new Group(child, SWT.NONE);
		analyserGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridLayoutFactory.swtDefaults().numColumns(4).spacing(10, 20).applyTo(analyserGroup);

		// Lens mode
		Label lensModeLabel = new Label(analyserGroup, SWT.NONE);
		setTextToBold(lensModeLabel);
		lensModeLabel.setText("Lens Mode");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, true).applyTo(lensModeLabel);

		lensModeCombo = new Combo(analyserGroup, SWT.READ_ONLY);
		lensModeCombo.setToolTipText("Analyser Lens Mode");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(lensModeCombo);
		// Setup lens modes and select currently selected one
		try {
			lensModeCombo.setItems(analyser.getLensModes());
			String activeLensMode = analyser.getLensMode();
			lensModeCombo.select(Arrays.asList(lensModeCombo.getItems()).indexOf(activeLensMode));
		} catch (Exception ex) {
			logger.error("Failed to get list of lens modes", ex);
		}

		lensModeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Changing analyser lens mode to " + lensModeCombo.getText());
				try {
					analyser.setLensMode(lensModeCombo.getText());
				} catch (Exception ex) {
					logger.error("Failed to change lens mode", ex);
				}
				updatePassEnergyCombo();
			}
		});

		// Analyser pass energy
		Label passEnergyLabel = new Label(analyserGroup, SWT.NONE);
		setTextToBold(passEnergyLabel);
		passEnergyLabel.setText("Pass Energy");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, true).applyTo(passEnergyLabel);

		passEnergyCombo = new Combo(analyserGroup, SWT.READ_ONLY);
		// Call update to setup passEnergyCombo
		updatePassEnergyCombo();
		passEnergyCombo.setToolTipText("Select a pass energy");

		// Add listener to update analyser pass energy when changed
		passEnergyCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Changing analyser pass energy to {}", passEnergyCombo.getText());
				try {
					analyser.setPassEnergy(Integer.parseInt(passEnergyCombo.getText()));
				} catch (Exception ex) {
					logger.error("Failed to set pass energy", ex);
				}
			}
		});

		NudgePositionerComposite centreEnergyNPC = new NudgePositionerComposite(analyserGroup, SWT.NONE);
		centreEnergyNPC.setScannable((Scannable) Finder.getInstance().find("raw_centre_energy"));
		centreEnergyNPC.setDisplayName("Centre Energy");
		centreEnergyNPC.hideStopButton();
		centreEnergyNPC.setUserUnits("eV");
		centreEnergyNPC.setLabelToBold();
		GridDataFactory.swtDefaults().span(1, 2).applyTo(centreEnergyNPC);

		// Analyser Start Button
		Button startButton = new Button(analyserGroup, SWT.DEFAULT);
		startButton.setText("Start");
		setTextToBold(startButton);
		startButton.setBackground(green);
		startButton.setToolTipText("Apply voltages and start acquiring");
		GridDataFactory.swtDefaults().span(1, 2).align(SWT.END, SWT.CENTER).hint(100, SWT.DEFAULT).applyTo(startButton);
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Starting fixed mode acquistion");
				try {
					// Need to reset lens mode and pass energy as they may have changed from the values
					// Shown in this GUI so resend the settings
					analyser.setAcquisitionMode("Fixed");
					analyser.setLensMode(lensModeCombo.getText());
					analyser.setPassEnergy(Integer.parseInt(passEnergyCombo.getText()));
					analyser.start();
				} catch (Exception ex) {
					logger.error("Failed to start fixed mode acquisition", ex);
				}
			}
		});

		// Analyser Stop Button
		Button stopButton = new Button(analyserGroup, SWT.DEFAULT);
		stopButton.setText("Stop");
		setTextToBold(stopButton);
		stopButton.setBackground(red);
		stopButton.setToolTipText("Stop acquiring and zero supplies");
		GridDataFactory.swtDefaults().span(1, 2).align(SWT.END, SWT.CENTER).hint(100, SWT.DEFAULT).applyTo(stopButton);
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Stopping continuous acquistion");
				try {
					analyser.zeroSupplies();
				} catch (Exception ex) {
					logger.error("Failed to stop analyser", ex);
				}
			}
		});

		// SMPM group
		Group smpmGroup = new Group(child, SWT.NONE);
		analyserGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridLayoutFactory.swtDefaults().numColumns(3).spacing(10, 20).applyTo(smpmGroup);

		NudgePositionerComposite smpmXNPC = new NudgePositionerComposite(smpmGroup, SWT.NONE);
		smpmXNPC.setScannable((Scannable) Finder.getInstance().find("smpmx"));
		smpmXNPC.hideStopButton();
		smpmXNPC.setUserUnits("mm");
		smpmXNPC.setDisplayName("SMPM X Trans");
		smpmXNPC.setLabelToBold();
		smpmXNPC.setIncrement(0.2);
		GridDataFactory.swtDefaults().span(1, 2).applyTo(smpmXNPC);

		NudgePositionerComposite smpmYNPC = new NudgePositionerComposite(smpmGroup, SWT.NONE);
		smpmYNPC.setScannable((Scannable) Finder.getInstance().find("smpmy"));
		smpmYNPC.hideStopButton();
		smpmYNPC.setUserUnits("mm");
		smpmYNPC.setDisplayName("SMPM Y Trans");
		smpmYNPC.setLabelToBold();
		smpmYNPC.setIncrement(0.2);
		GridDataFactory.swtDefaults().span(1, 2).applyTo(smpmYNPC);

		NudgePositionerComposite smpmZNPC = new NudgePositionerComposite(smpmGroup, SWT.NONE);
		smpmZNPC.setScannable((Scannable) Finder.getInstance().find("smpmz"));
		smpmZNPC.hideStopButton();
		smpmZNPC.setUserUnits("mm");
		smpmZNPC.setDisplayName("SMPM Z Trans");
		smpmZNPC.setLabelToBold();
		smpmZNPC.setIncrement(0.2);
		GridDataFactory.swtDefaults().span(1, 2).applyTo(smpmZNPC);

		NudgePositionerComposite smpmPolarNPC = new NudgePositionerComposite(smpmGroup, SWT.NONE);
		smpmPolarNPC.setScannable((Scannable) Finder.getInstance().find("smpmpolar"));
		smpmPolarNPC.hideStopButton();
		smpmPolarNPC.setUserUnits("mm");
		smpmPolarNPC.setDisplayName("SMPM Polar Trans");
		smpmPolarNPC.setLabelToBold();
		smpmPolarNPC.setIncrement(0.2);
		GridDataFactory.swtDefaults().span(1, 2).applyTo(smpmPolarNPC);

		NudgePositionerComposite smpmAzimuthNPC = new NudgePositionerComposite(smpmGroup, SWT.NONE);
		smpmAzimuthNPC.setScannable((Scannable) Finder.getInstance().find("smpmazimuth"));
		smpmAzimuthNPC.hideStopButton();
		smpmAzimuthNPC.setUserUnits("mm");
		smpmAzimuthNPC.setDisplayName("SMPM Azimuth Trans");
		smpmAzimuthNPC.setLabelToBold();
		smpmAzimuthNPC.setIncrement(0.2);
		GridDataFactory.swtDefaults().span(1, 2).applyTo(smpmAzimuthNPC);

		// Set the child as the scrolled content of the ScrolledComposite
		scrollComp.setContent(child);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
	}

	private void setTextToBold(Control control) {
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(control.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(control.getDisplay());
		control.setFont(boldFont);
	}

	private void updatePassEnergyCombo() {
		try {
			final String[] passEnergyStrings = analyser.getPassENergies();

			logger.debug("Setting items in pass energy combo box");
			passEnergyCombo.setItems(passEnergyStrings);
			// Automatically select the current pass energy if available in current PSU mode
			logger.debug("Selecting currently active pass energy in combo box");
			int activePassEnergy = analyser.getPassEnergy();
			passEnergyCombo.select(Arrays.asList(passEnergyStrings).indexOf(Integer.toString(activePassEnergy)));
		} catch (Exception e) {
			logger.error("Failed to get PSU mode", e);
		}
	}

	@Focus
	public void onFocus() {
		parent.setFocus();
	}
}
