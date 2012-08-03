/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.view;

import gda.factory.FactoryException;
import gda.images.camera.DummySwtVideoReceiver;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.composites.CameraComposite;
import uk.ac.gda.client.tomo.composites.NewImageListener;
import uk.ac.gda.client.tomo.figures.BeamScaleFigure;
import uk.ac.gda.client.tomo.figures.ImageKeyFigure;

/**
 * View to display the live images from the camera Used in alignment and monitoring
 */
public class CameraViewPart extends ViewPart implements NewImageListener {

	static final Logger logger = LoggerFactory.getLogger(CameraViewPart.class);

	static public String ID = "uk.ac.gda.client.tomo.CameraView";
	private CameraComposite cameraComposite;

	private CameraViewPartConfig cameraConfig;

	public CameraViewPart() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {

		cameraConfig = TomoClientActivator.getCameraConfig();

		if (cameraConfig == null) {
			Label lblCamera = new Label(parent, SWT.NONE);
			lblCamera.setText("No CameraViewPartConfig service found");
			return;
		}
		
		cameraComposite = new CameraComposite(parent, SWT.NONE, parent.getDisplay(), cameraConfig.getReceiver(), this);

		Action zoomFit = new Action("Zoom to Fit") {
			@Override
			public void run() {
				zoomToFit();
			}
		};
		imageKeyAction = new Action("Image Key", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				//this runs after the state has been changed
				showImageKey(isChecked());
			}


		};
		imageKeyAction.setChecked(false);//do not 
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager dropDownMenu = actionBars.getMenuManager();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		dropDownMenu.add(zoomFit);
		dropDownMenu.add(imageKeyAction);
		// toolBar.add(action);

	}
	private ImageKeyFigure imageKeyFigure;

	private void showImageKey(boolean showImage) {
		if(showImage){
			Rectangle imageKeyBounds = new Rectangle(5, 5, -1, -1);
			cameraComposite.getTopFigure().add(getImageKeyFigure(), imageKeyBounds);
		} else {
			cameraComposite.getTopFigure().remove(getImageKeyFigure());
			
		}
	}

	private ImageKeyFigure getImageKeyFigure() {
		return imageKeyFigure != null ? imageKeyFigure : (imageKeyFigure= new ImageKeyFigure());
	}
	
	
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private BeamScaleFigure beamScaleFigure;


	private boolean layoutReset = false;

	private Action imageKeyAction;

	@Override
	public void handlerNewImageNotification(ImageData newImage) {
		// On the first image, ensure we reset the display to match incoming image dimensions
		if (!layoutReset) {
			layoutReset = true;

			// add figures
			int offset = 200;
			Rectangle imageBounds = cameraComposite.getViewer().getImageBounds();
			Rectangle scaleBounds = new Rectangle(imageBounds.width - offset, imageBounds.height - offset, -1, -1);
			beamScaleFigure = new BeamScaleFigure();
			beamScaleFigure.setBeamSize(100, 100);
			beamScaleFigure.setXScale(1.0);
			beamScaleFigure.setYScale(1.0);
			beamScaleFigure.setBackgroundColor(ColorConstants.darkGray);
//do not add yet			cameraComposite.getTopFigure().add(beamScaleFigure, scaleBounds);
		}
		getImageKeyFigure().newImage(newImage);

	}

	@Override
	public void dispose() {
		super.dispose();
		if (cameraComposite != null) {
			cameraComposite.dispose();
		}
	}

	public void zoomToFit() {
		if (cameraComposite != null)
			cameraComposite.zoomFit();

	}

}
