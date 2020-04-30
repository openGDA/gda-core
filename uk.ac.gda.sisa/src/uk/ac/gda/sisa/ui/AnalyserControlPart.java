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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.detector.areadetector.v17.NDProcess;
import gda.factory.Finder;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.livecontrol.ControlSet;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public class AnalyserControlPart {

	private static final Logger logger = LoggerFactory.getLogger(AnalyserControlPart.class);
	final Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	final Color green = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	final Color transparent = SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT);

	private Composite parent;
	
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
		
		ScrolledComposite scroller = new ScrolledComposite(parent, SWT.V_SCROLL);
		scroller.setBackground(transparent);

		Composite scrollerContent = new Composite(scroller, SWT.NONE);
		scrollerContent.setBackground(transparent);
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.fill = true;
		scrollerContent.setLayout(rowLayout);
		
		addButtons(scrollerContent);
		addLiveControls(scrollerContent);

		// Set the child as the scrolled content of the ScrolledComposite
		scroller.setContent(scrollerContent);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		scroller.setMinSize(scrollerContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void addButtons(Composite composite) {
		RowLayout groupRowLayout = new RowLayout(SWT.HORIZONTAL);
		groupRowLayout.pack = false;
		groupRowLayout.justify = true;
		
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(groupRowLayout);
		
		addAnalyserStartButton(group);
		addAnalyserStopButton(group);
		addAccumulationStartButton(group);
		addAccumulationStopButton(group);
	}
				
	private void addAnalyserStartButton(Composite composite) {
		Button startButton = new Button(composite, SWT.PUSH);
		startButton.setText("Start Camera");
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
	
	private void addAnalyserStopButton(Composite composite) {
		Button stopButton = new Button(composite, SWT.PUSH);
		stopButton.setText("Stop Camera");
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
	
	private void addAccumulationStartButton(Composite composite) {
		Button accumulationStartButton = new Button(composite, SWT.PUSH);
		accumulationStartButton.setText("Start/Reset Accumulation");
		setTextToBold(accumulationStartButton);
		accumulationStartButton.setBackground(green);
		accumulationStartButton.setToolTipText("Enable accumulation or reset the current accumulation");
		accumulationStartButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Starting accumulation");
				try {
					eavCameraControl.setProcessingFilterType(NDProcess.FilterTypeV1_8_Sum);
					eavCameraControl.resetFilter();
					eavCameraControl.enableProcessingFilter();				
				} catch (Exception ex) {
					logger.error("Failed to stop analyser or live viewer", ex);
				}
			}
		});	
	}
	
	private void addAccumulationStopButton(Composite composite) {
		Button accumulationStopButton = new Button(composite, SWT.PUSH);
		accumulationStopButton.setText("Stop Accumulation");
		setTextToBold(accumulationStopButton);
		accumulationStopButton.setBackground(red);
		accumulationStopButton.setToolTipText("Disable accumulation");
		accumulationStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Stopping accumulation");
				try {
					eavCameraControl.disableProcessingFilter();
					eavCameraControl.resetFilter();
				} catch (Exception ex) {
					logger.error("Failed to stop analyser or live viewer", ex);
				}
			}
		});	
	}
		
	private void addLiveControls(Composite composite) {
		if (alignmentControls.hasAnalyserControls()) {
			addControlGroup(composite, "Analyser Controls", alignmentControls.getAnalyserControls());
		}
		
		if (alignmentControls.hasSampleControls()) {
			addControlGroup(composite, "Sample Controls", alignmentControls.getSampleControls());
		}
	}
	
	private void addControlGroup(Composite composite, String groupName, ControlSet controlSet) {
		Group group = new Group(composite, SWT.NONE);
		group.setText(groupName);
		group.setBackground(transparent);
		GridLayoutFactory.swtDefaults().numColumns(5).spacing(10, 20).applyTo(group);
		
		controlSet.getControls().stream().forEachOrdered(c -> c.createControl(group));
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
