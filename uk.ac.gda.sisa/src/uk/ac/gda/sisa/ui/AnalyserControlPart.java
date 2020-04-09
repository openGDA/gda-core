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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.livecontrol.ControlSet;
import uk.ac.gda.client.livecontrol.LiveControl;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public class AnalyserControlPart {

	private static final Logger logger = LoggerFactory.getLogger(AnalyserControlPart.class);
	final Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	final Color green = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);

	private Composite parent;
	private Composite child;
	
	Group analyserGroup;
	
	private AlignmentControls alignmentControls;
	private IVGScientaAnalyserRMI analyser;
	private CameraControl eavCameraControl;

	@Inject
	public AnalyserControlPart() {
		logger.trace("Constructor called");

		try {
			alignmentControls = Finder.getInstance().findSingleton(AlignmentControls.class);
		} catch (IllegalArgumentException exception) {
			String msg = "No AlignmentControls was found! (Or more than 1)";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		
		analyser = alignmentControls.getAnalyser();
		eavCameraControl = alignmentControls.getAnalyserEavControl();
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
		
		createAnalyserControlsGroup();
		addLiveControls();
						
		// Set the child as the scrolled content of the ScrolledComposite
		scrollComp.setContent(child);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		scrollComp.setMinSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void createAnalyserControlsGroup() {
		analyserGroup = new Group(child, SWT.NONE);
		analyserGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridLayoutFactory.swtDefaults().numColumns(3).spacing(10, 20).applyTo(analyserGroup);
		
		addAnalyserButtons(analyserGroup);
	}
	
	private void addAnalyserButtons(Group analyserGroup) {
		Group analyserButtonsGroup = new Group(analyserGroup, SWT.NONE);
		analyserButtonsGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.fillDefaults().grab(true,  true).span(3,  1).applyTo(analyserButtonsGroup);
		analyserButtonsGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
			
		addAnalyserStartButton(analyserButtonsGroup);
		addAnalyserStopButton(analyserButtonsGroup);
	}
	
	private void addAnalyserStartButton(Group analyserButtonsGroup) {
		Button startButton = new Button(analyserButtonsGroup, SWT.DEFAULT);
		startButton.setText("Start");
		setTextToBold(startButton);
		startButton.setBackground(green);
		startButton.setToolTipText("Apply voltages and start acquiring");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Starting fixed mode acquistion");
				try {
					analyser.setAcquisitionMode("Fixed");
					analyser.start();
					eavCameraControl.setImageMode(ImageMode.CONTINUOUS);
					eavCameraControl.startAcquiring();
				} catch (Exception ex) {
					logger.error("Failed to start fixed mode acquisition or live viewer", ex);
				}
			}
		});
	}
	
	private void addAnalyserStopButton(Group analyserButtonsGroup) {
		Button stopButton = new Button(analyserButtonsGroup, SWT.DEFAULT);
		stopButton.setText("Stop");
		setTextToBold(stopButton);
		stopButton.setBackground(red);
		stopButton.setToolTipText("Stop acquiring and zero supplies");
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Stopping continuous acquistion");
				try {
					eavCameraControl.stopAcquiring();
					analyser.zeroSupplies();
				} catch (Exception ex) {
					logger.error("Failed to stop analyser or live viewer", ex);
				}
			}
		});	
	}
		
	private void addLiveControls() {
		if (alignmentControls.hasAnalyserControls()) {
			alignmentControls.getAnalyserControls()
				.getControls()
				.stream()
				.forEachOrdered(c -> c.createControl(analyserGroup));

		}
		
		if (alignmentControls.hasSampleControls()) {
			Group sampleControlGroup = new Group(child, SWT.NONE);
			sampleControlGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
			GridLayoutFactory.swtDefaults().numColumns(3).spacing(10, 20).applyTo(sampleControlGroup);
			
			alignmentControls.getSampleControls()
				.getControls()
				.stream()
				.forEachOrdered(c -> c.createControl(sampleControlGroup));
		}
	}
	
	private void setTextToBold(Control control) {
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(control.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(control.getDisplay());
		control.setFont(boldFont);
	}

	@Focus
	public void onFocus() {
		parent.setFocus();
	}
}
