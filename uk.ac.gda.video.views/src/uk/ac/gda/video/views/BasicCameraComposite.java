/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.video.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FactoryException;
import gda.factory.FindableBase;
import gda.images.camera.ImageListener;
import gda.images.camera.MotionJpegOverHttpReceiverSwt;

public class BasicCameraComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(BasicCameraComposite.class);

	private uk.ac.gda.client.viewer.ImageViewer viewer;
	private MotionJpegOverHttpReceiverSwt videoReceiver;
	private ImageListener<ImageData> listener = null;

	public BasicCameraComposite(Composite parent, int style) {
		this(parent, style, null);
	}

	public BasicCameraComposite(Composite parent, int style, ImageListener<ImageData> listener) {
		super(parent, style);

		setLayout(new GridLayout(1, false));

		viewer = new uk.ac.gda.client.viewer.ImageViewer(parent, style|SWT.DOUBLE_BUFFERED);
		viewer.showDefaultImage();
		videoReceiver = new MotionJpegOverHttpReceiverSwt();
		if (listener == null)
			listener = new VideoListener();
		this.listener = listener;
		videoReceiver.addImageListener(listener);
		pack();
	}

	void closeDownVideo(boolean full){
		if (listener != null)
			videoReceiver.removeImageListener(listener);
		videoReceiver.closeConnection();
		if( full){
			videoReceiver = null;
		}
	}

	@Override
	public boolean setFocus() {
		return viewer != null ? viewer.setFocus() : false;
	}

	@Override
	public void dispose() {
		super.dispose();
		closeDownVideo(true);
	}

	public void resetView() {
		if (viewer != null)
			viewer.resetView();
	}

	public void zoomFit() {
		if (viewer != null)
			viewer.zoomFit();
	}

	public void playURL(String URL) throws FactoryException {
		videoReceiver.setUrl(URL);
		if (listener != null)
			videoReceiver.addImageListener(listener);
		videoReceiver.configure();
		videoReceiver.start();
	}

	public void loadImage(ImageData lastImage) {
		viewer.loadImage(lastImage);
	}

	public boolean isShowingDefault() {
		return viewer.isShowingDefault();
	}

	public void showDefaultImage() {
		viewer.showDefaultImage();
	}

	private final class VideoListener extends FindableBase implements ImageListener<ImageData> {

		boolean processingImage=false;
		private ImageData imageToProcess;

		@Override
		public void processImage(final ImageData image) {
			if (image == null)
				return;
			imageToProcess = image;
			if (viewer != null) {
				if(isDisposed())
					return;
				if(processingImage)
					return;
				processingImage = true;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Error sleeping for 100ms", e);
				}
				getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						boolean showingDefault = viewer.isShowingDefault();
						viewer.loadImage(imageToProcess);
						viewer.refreshView();
						if (showingDefault) {
							zoomFit();
						}
						processingImage = false;
					}
				});
			}
		}
	}
}