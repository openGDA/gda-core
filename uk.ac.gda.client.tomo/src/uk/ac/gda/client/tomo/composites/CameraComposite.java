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

package uk.ac.gda.client.tomo.composites;

import gda.images.camera.ImageListener;
import gda.images.camera.VideoReceiver;

import java.text.DateFormat;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.SWTImageUtils;
import uk.ac.gda.client.tomo.figures.BeamScaleFigure;
import uk.ac.gda.client.viewer.ImageViewer;


public class CameraComposite extends Composite {

	static final Logger logger = LoggerFactory.getLogger(CameraComposite.class);
	private ImageViewer viewer;
	private VideoReceiver<ImageData> videoReceiver;
	private VideoListener listener = new VideoListener();


	Composite parent;

	NewImageListener newImageListener;
	public CameraComposite(Composite parent, int style, @SuppressWarnings("unused") Display display,
			VideoReceiver<ImageData> videoReceiver, NewImageListener newImageListener) {
		super(parent, style);
		this.newImageListener = newImageListener;
		this.videoReceiver = videoReceiver;

		setLayout(new GridLayout(1, false));
		viewer = new uk.ac.gda.client.viewer.ImageViewer(this, SWT.DOUBLE_BUFFERED);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getCanvas());

		viewer.showDefaultImage();

		videoReceiver.addImageListener(listener);
		viewer.getCanvas().addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				zoomFit();
			}
		});
		zoomFit();
		pack();
	}
	/**
	 * Adds the given figure as a child to the main image figure
	 * @param figure to add
	 */
	public void addFigure(IFigure figure) {
		getTopFigure().add(figure);
		
	}

	/**
	 * Removes the given figure from the image figure hierarchy
	 * @param figure
	 */
	public void removeFigure(IFigure figure) {
		getTopFigure().remove(figure);
		
	}
	
	public IFigure getTopFigure() {
		return viewer.getTopFigure();
	}

	private boolean layoutReset = false;
	private void initViewer() {
		// On the first image, ensure we reset the display to match incoming image dimensions
		if (!layoutReset){
			layoutReset = true;
					viewer.resetView();
					int offset = 200;
					Rectangle imageBounds = viewer.getImageBounds();
					Rectangle scaleBounds = new Rectangle(imageBounds.width - offset, imageBounds.height - offset, -1, -1);
					beamScaleFigure = new BeamScaleFigure();
					beamScaleFigure.setBeamSize(100, 100);
					beamScaleFigure.setXScale(1.0);
					beamScaleFigure.setYScale(1.0);
					beamScaleFigure.setBackgroundColor(ColorConstants.darkGray);
					getTopFigure().add(beamScaleFigure, scaleBounds);
		}
	}

	@Override
	public boolean setFocus() {
		return viewer != null ? viewer.setFocus() : false;
	}

	@Override
	public void dispose() {
		super.dispose();
		videoReceiver.removeImageListener(listener);
		viewer.dispose();
	}

	public void zoomFit() {
		if (viewer != null)
			viewer.zoomFit();
	}
	IDataset getDataset(){
		return lastImage != null ? new SWTImageDataConverter(lastImage).toIDataset() : null;
	}

	ImageData lastImage;
//	private Label lastImageId;
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
	private BeamScaleFigure beamScaleFigure;
	private final class VideoListener implements ImageListener<ImageData> {
		private String name;

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@SuppressWarnings("unused")
		public void clear() {
			viewer.showDefaultImage();
		}

		@Override
		public String getName() {
			return name;
		}

		boolean processingImage=false;
		@Override
		public void processImage(final ImageData image) {
			if (image == null)
				return;
			lastImage=image;
			if (viewer != null) {
				if(isDisposed())
					return;
				if(processingImage)
					return;
				processingImage=true;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e);
				}
				getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						boolean showingDefault = viewer.isShowingDefault();
						while(true){
							//ensure we don't miss an image that arrives while we process the first
							ImageData lastImage2 = lastImage;
							viewer.loadImage(lastImage2);
							if (showingDefault) {
								zoomFit();
							}
//							lastImageId.setText(df.format(new Date()));
							if( newImageListener != null)
								newImageListener.handlerNewImageNotification();

							initViewer();
							
							
							if( lastImage2 == lastImage){
								processingImage=false;
								break;
							}
							processingImage=false;
							break; //TODO we may remain in UI thread if frame rate is very high
						}
					}
				});
			}
		}
	}
}

class SWTImageDataConverter {
	ImageData imageData;
	IDataset idataset;

	public SWTImageDataConverter(ImageData imageData) {
		this.imageData = imageData;
	}

	public IDataset toIDataset() {
		if (imageData == null) {
			return null;
		}
		if( idataset == null){
			idataset = SWTImageUtils.createRGBDataset(imageData).createGreyDataset(AbstractDataset.FLOAT32);
		}
		return idataset;

	}
}


