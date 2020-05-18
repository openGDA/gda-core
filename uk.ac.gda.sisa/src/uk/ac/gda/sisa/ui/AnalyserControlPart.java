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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.factory.Finder;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.livecontrol.ControlSet;
import uk.ac.gda.sisa.Activator;

public class AnalyserControlPart {

	private static final Logger logger = LoggerFactory.getLogger(AnalyserControlPart.class);
	final Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	final Color green = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	final Color transparent = SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT);
	final Image lightOn = Activator.getImage("/icons/red-dome-light-on.png");
	final Image lightOff = Activator.getImage("/icons/red-dome-light-off.png");
	

	private Composite parent;
	
	private final AlignmentConfiguration alignmentConfig;
	private final CameraControl eavCameraControl;
	
	Button cameraButton;
	Button accumulationButton;

	@Inject
	public AnalyserControlPart() {
		logger.trace("Constructor called");

		try {
			alignmentConfig = Finder.getInstance().findSingleton(AlignmentConfiguration.class);
		} catch (IllegalArgumentException exception) {
			String msg = "No AlignmentConfiguration was found! (Or more than 1)";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		
		eavCameraControl = alignmentConfig.getAnalyserEavControl();
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
				
		addCameraButton(group);
		addAccumulationButton(group);
	}
			
	private void addCameraButton(Composite composite) {
		cameraButton = new Button(composite, SWT.TOGGLE);
		cameraButton.setText("Camera");
		setTextToBold(cameraButton);
		
		RowData rowData = new RowData();
		rowData.width = 200;
		cameraButton.setLayoutData(rowData);
		
		cameraButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				if (cameraButton.getSelection()) {
					startCameraAcquisition();
				} else {
					stopCameraAcquisition();
				}
			}	
		});
	
		Monitor acquireStatus = alignmentConfig.getEavAcquiringStatusMonitor();
		acquireStatus.addIObserver(this::updateCameraButtonState);
		
		try {
			updateCameraButtonState((String)(acquireStatus).getPosition());
		} catch (DeviceException exception) {
			logger.error("Unable to set initial status of camera button - error getting current status.", exception);
		}
	}
		
	private void startCameraAcquisition() {
		try {
			logger.info("Starting continuous acquisition.");
			eavCameraControl.setImageMode(ImageMode.CONTINUOUS);
			eavCameraControl.startAcquiring();
		} catch (Exception ex) {
			logger.error("Failed to start fixed mode acquisition or live viewer", ex);
		}
	}
	
	private void stopCameraAcquisition() {
		try {
			logger.info("Stopping continuous acquisition.");
			eavCameraControl.stopAcquiring();
		} catch (Exception ex) {
			logger.error("Failed to stop analyser or live viewer", ex);
		}
	}
	
	private void updateCameraButtonState(Object source, Object arg) {
		if (arg instanceof String) {
			updateCameraButtonState((String)arg);
		}
	}
	
	private void updateCameraButtonState(String acquireStatus) {
		if (acquireStatus.equals("Acquire")) {
			Display.getDefault().asyncExec(() -> {
				cameraButton.setSelection(true);
				cameraButton.setImage(lightOn);
				cameraButton.setToolTipText("Stop analyser live stream");
			});
		} else {
			Display.getDefault().asyncExec(() -> {
				cameraButton.setSelection(false);
				cameraButton.setImage(lightOff);				
				cameraButton.setToolTipText("Start analyser live stream");
			});			
		}	
	}
		
	private void addAccumulationButton(Composite composite) {
		accumulationButton = new Button(composite, SWT.TOGGLE);
		accumulationButton.setText("Accumulation");
		setTextToBold(accumulationButton);
		
		RowData rowData = new RowData();
		rowData.width = 200;
		accumulationButton.setLayoutData(rowData);
		
		accumulationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (accumulationButton.getSelection()) {
					startAccumulation();
				} else {
					stopAccumulation();
				}
			}
		});	
		
		Monitor accumulationStatus = alignmentConfig.getEavAccumulationStatusMonitor();
		accumulationStatus.addIObserver(this::updateAccumulationButtonState);
		
		try {
			updateAccumulationButtonState((String)(accumulationStatus).getPosition());
		} catch (DeviceException exception) {
			logger.error("Unable to set initial status of accumulation button - error getting current status.", exception);
		}
	}
	
	private void updateAccumulationButtonState(Object source, Object arg) {
		if (arg instanceof String) {
			updateAccumulationButtonState((String)arg);
		}
	}
	
	private void updateAccumulationButtonState(String accumulationStatus) {
		if (accumulationStatus.equals("Enable")) {
			Display.getDefault().asyncExec(() -> {
				accumulationButton.setSelection(true);
				accumulationButton.setImage(lightOn);
				accumulationButton.setToolTipText("Disable the accumulation filter");
			});
		} else {
			Display.getDefault().asyncExec(() -> {
				accumulationButton.setSelection(false);
				accumulationButton.setImage(lightOff);
				accumulationButton.setToolTipText("Enable the accumulation filter");
			});
		}
	}
	
	private void startAccumulation() {
		logger.info("Starting accumulation");
		try {
			eavCameraControl.setProcessingFilterType(NDProcess.FilterTypeV1_8_Sum);
			eavCameraControl.resetFilter();
			eavCameraControl.enableProcessingFilter();				
		} catch (Exception ex) {
			logger.error("Failed to stop analyser or live viewer", ex);
		}
	}
	
	private void stopAccumulation() {
		logger.info("Stopping accumulation");
		try {
			eavCameraControl.disableProcessingFilter();
			eavCameraControl.resetFilter();
		} catch (Exception ex) {
			logger.error("Failed to stop analyser or live viewer", ex);
		}
	}
		
	private void addLiveControls(Composite composite) {
		if (alignmentConfig.hasAnalyserControls()) {
			addControlGroup(composite, "Analyser Controls", alignmentConfig.getAnalyserControls());
		}
		
		if (alignmentConfig.hasSampleControls()) {
			addControlGroup(composite, "Sample Controls", alignmentConfig.getSampleControls());
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
