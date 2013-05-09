/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.rcp.views;

import gda.device.Scannable;
import gda.device.monitor.DummyMonitor;
import gda.device.motor.DummyMotor;
import gda.device.scannable.ScannableMotor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.springframework.beans.factory.InitializingBean;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.ui.utils.SWTUtils;
import uk.ac.gda.ui.viewer.MotorPositionViewer;

public class MotorPositionViewerCompositeFactory implements CompositeFactory, InitializingBean {
	private Scannable scannable;
	private Boolean layoutHoriz=true;
	private String label=null;
	private Integer decimalPlaces = null;
	private static Boolean restoreValueWhenFocusLost;
	
	public Scannable getScannable() {
		return scannable;
	}


	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}


	public Boolean getLayoutHoriz() {
		return layoutHoriz;
	}


	public void setLayoutHoriz(Boolean layoutHoriz) {
		this.layoutHoriz = layoutHoriz;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setDecimalPlaces(Integer dp) {
		decimalPlaces = dp;
	}
	
	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	public static Composite createComposite(Composite parent, int style, final Display display, Scannable scannable, Boolean layoutHoriz,
			String label, Integer decimalPlaces){
		return new MotorPositionViewerComposite(parent, style, display, scannable, layoutHoriz, label, decimalPlaces, null, getRestoreValueWhenFocusLost());
	}
	@Override
	public Composite createComposite(Composite parent, int style, IWorkbenchPartSite iWorkbenchPartSite) {
		return new MotorPositionViewerComposite(parent, style, iWorkbenchPartSite.getShell().getDisplay(), scannable, layoutHoriz, label, 
				decimalPlaces, commandFormat, getRestoreValueWhenFocusLost());
	}

	private String commandFormat;
	
	public void setCommandFormat(String commandFormat) {
		this.commandFormat = commandFormat;
	}
	
	public static Boolean getRestoreValueWhenFocusLost() {
		return restoreValueWhenFocusLost;
	}

	public void setRestoreValueWhenFocusLost(Boolean restoreValueWhenFocusLost) {
		MotorPositionViewerCompositeFactory.restoreValueWhenFocusLost = restoreValueWhenFocusLost;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (scannable == null)
			throw new IllegalArgumentException("scannable is null");

	}

	public static void main(String... args) throws Exception {
		DummyMotor dummyMotor = new DummyMotor();
		dummyMotor.setName("dummyMotor");
		dummyMotor.configure();
		ScannableMotor scannableMotor = new ScannableMotor();
		scannableMotor.setMotor(dummyMotor);
		scannableMotor.setName("scannableMotor");
		scannableMotor.setLowerGdaLimits(0.);
		scannableMotor.setInitialUserUnits("mm");
		scannableMotor.configure();
		
		DummyMonitor dummy = new DummyMonitor();
		dummy.setName("dummy");
		dummy.configure();
		MotorPositionViewerCompositeFactory motorPositionViewFactory = new MotorPositionViewerCompositeFactory();
		motorPositionViewFactory.setScannable(scannableMotor);
		motorPositionViewFactory.afterPropertiesSet();
		
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new BorderLayout());

		final MotorPositionViewerComposite comp = new MotorPositionViewerComposite(shell, SWT.NONE, display, scannableMotor, true, "North", null, null, getRestoreValueWhenFocusLost());
		comp.setLayoutData(BorderLayout.NORTH);
		comp.setVisible(true);
		final MotorPositionViewerComposite comp1 = new MotorPositionViewerComposite(shell, SWT.NONE, display, scannableMotor, false, null, null, null, getRestoreValueWhenFocusLost());
		comp1.setLayoutData(BorderLayout.SOUTH);
		comp1.setVisible(true);
		shell.pack();
		shell.setSize(400, 400);
		SWTUtils.showCenteredShell(shell);
	}	
}
class MotorPositionViewerComposite extends Composite {

	@SuppressWarnings("unused")
	MotorPositionViewerComposite(Composite parent, int style, final Display display, Scannable scannable, Boolean layoutHoriz,
			String label, Integer decimalPlaces, String commandFormat, Boolean restoreValueWhenFocusLost) {
		super(parent, style);
		
		GridLayoutFactory.fillDefaults().numColumns(layoutHoriz ? 2: 1).applyTo(this);
		GridDataFactory.fillDefaults().applyTo(this);
		MotorPositionViewer mpv = new MotorPositionViewer(this, scannable, label);		
		mpv.setCommandFormat(commandFormat);
		mpv.setDecimalPlaces(2);
		if (restoreValueWhenFocusLost != null) {
			mpv.setRestoreValueWhenFocusLost(restoreValueWhenFocusLost);
		}
		else {
			mpv.setRestoreValueWhenFocusLost(false);
		}

		if (decimalPlaces != null) 
			mpv.setDecimalPlaces(decimalPlaces.intValue());
	}
}
