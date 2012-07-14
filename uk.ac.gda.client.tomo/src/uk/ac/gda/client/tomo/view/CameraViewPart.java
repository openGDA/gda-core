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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.composites.CameraComposite;
import uk.ac.gda.client.tomo.composites.NewImageListener;

/**
 * View to display the live images from the camera
 * Used in alignment and monitoring
 */
public class CameraViewPart extends ViewPart implements NewImageListener {

	static final Logger logger = LoggerFactory.getLogger(CameraViewPart.class);

	static public String ID="uk.ac.gda.client.tomo.CameraView";
	private CameraComposite cameraComposite;
	public CameraViewPart() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		DummySwtVideoReceiver receiver = new DummySwtVideoReceiver();
		receiver.setDesiredFrameRate(10);
		try {
			receiver.configure();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
		cameraComposite = new CameraComposite(parent, SWT.NONE, parent.getDisplay(), receiver, this);
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handlerNewImageNotification() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		super.dispose();
		if( cameraComposite != null){
			cameraComposite.dispose();
		}
	}

}
