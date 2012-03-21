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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.ui.viewer.RotationViewer;

public class CameraFocusZoomControlView extends ViewPart {

	public static final String ID = "uk.ac.gda.client.microfocus.CameraFocusZoomControlView";
	
	private ScrolledComposite scrolledComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		
			scrolledComposite = new ScrolledComposite(parent,SWT.H_SCROLL | SWT.V_SCROLL);
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.setExpandVertical(true);

			
			Group sampleGroup = new Group(scrolledComposite, SWT.BORDER);	
			sampleGroup.setLayout(new GridLayout(2, false));
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			sampleGroup.setLayoutData(data);
			
			
			RotationViewer vmaZoomViewer = new RotationViewer((ScannableMotionUnits)Finder.getInstance().find("vma_zoom1"));
			vmaZoomViewer.createControls(sampleGroup, SWT.SINGLE);
			/*RotationViewer vmaFocusViewer = new RotationViewer((ScannableMotionUnits)Finder.getInstance().find("vma_focus1"));
			vmaFocusViewer.createControls(sampleGroup, SWT.SINGLE);*/
			
			scrolledComposite.setContent(sampleGroup);
	}

	@Override
	public void setFocus() {
		scrolledComposite.setFocus();
	}

}
