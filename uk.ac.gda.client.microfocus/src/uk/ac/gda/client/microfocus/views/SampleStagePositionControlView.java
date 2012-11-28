/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.views;

import gda.device.ScannableMotionUnits;
import gda.factory.Finder;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.ui.viewer.RotationViewer;

public class SampleStagePositionControlView extends ViewPart  {
	public SampleStagePositionControlView() {
	}

	protected static final Logger logger = LoggerFactory.getLogger(SampleStagePositionControlView.class);
	public static final String ID = "uk.ac.gda.client.microfocus.views.SamplePositionView";
	private ScrolledComposite scrolledComposite;
	private RotationViewer samplexViewer;
	private RotationViewer sampleyViewer;
	private RotationViewer samplezViewer;
	private RotationViewer energyViewer;
	private RotationViewer sampleThetaViewer;
	private RotationViewer samplexzViewer;
	@Override
	public void createPartControl(Composite parent) {
		scrolledComposite = new ScrolledComposite(parent,SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		
		Group sampleGroup = new Group(scrolledComposite, SWT.BORDER);	
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(25, 25).applyTo(sampleGroup);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		sampleGroup.setLayoutData(data);
		
		samplexViewer = new RotationViewer((ScannableMotionUnits)Finder.getInstance().find("sc_MicroFocusSampleX"), 1.0);
		samplexViewer.createControls(sampleGroup, SWT.SINGLE);
		samplexViewer.setMotorPositionViewerDecimalPlaces(4);
		sampleyViewer = new RotationViewer((ScannableMotionUnits)Finder.getInstance().find("sc_MicroFocusSampleY"), 1.0);
		sampleyViewer.createControls(sampleGroup, SWT.SINGLE);
		sampleyViewer.setMotorPositionViewerDecimalPlaces(4);
		samplezViewer = new RotationViewer((ScannableMotionUnits)Finder.getInstance().find("sc_sample_z"), 1.0);
		samplezViewer.createControls(sampleGroup, SWT.SINGLE);
		samplezViewer.setMotorPositionViewerDecimalPlaces(4);
		sampleThetaViewer = new RotationViewer((ScannableMotionUnits)Finder.getInstance().find("sc_sample_thetacoarse"));
		sampleThetaViewer.createControls(sampleGroup, SWT.SINGLE);
		sampleThetaViewer.setMotorPositionViewerDecimalPlaces(4);
		samplexzViewer = new RotationViewer((ScannableMotionUnits)Finder.getInstance().find("sc_microfocusXZ"), 1.0);
		samplexzViewer.createControls(sampleGroup, SWT.SINGLE);
		samplexzViewer.setMotorPositionViewerDecimalPlaces(4);
		energyViewer = new RotationViewer((ScannableMotionUnits)Finder.getInstance().find("energy"));
		energyViewer.createControls(sampleGroup, SWT.SINGLE);
		energyViewer.setMotorPositionViewerDecimalPlaces(4);
		
		for (Control c : sampleGroup.getChildren()) {
			GridDataFactory.fillDefaults().applyTo(c);
		}
		
		scrolledComposite.setContent(sampleGroup);
	}

	@Override
	public void setFocus() {
		scrolledComposite.setFocus();
	}
	
}
