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
import gda.images.camera.MotionJpegOverHttpReceiverSwt;
import gda.images.camera.VideoReceiver;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.SWTImageUtils;
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
		SashForm sashForm = new SashForm(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sashForm);
		sashForm.setOrientation(SWT.VERTICAL);

		createImageComposite(sashForm);

		viewer.showDefaultImage();

		videoReceiver.addImageListener(listener);
		pack();
	}

	private void createImageComposite(Composite sashForm2) {
		Composite imageComp = new Composite(sashForm2, SWT.NONE);
		imageComp.setLayout(new GridLayout(1, true));

		Composite btnComp = new Composite(imageComp, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(btnComp);
		btnComp.setLayout(new GridLayout(3, false));

		Button zoomFit = new Button(btnComp, SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(zoomFit);
		zoomFit.setText("Auto-zoom");
		zoomFit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				zoomFit();
			}
		});

		lastImageId = new Label(btnComp, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lastImageId);
		lastImageId.setText("Time of last Image");
		

		viewer = new uk.ac.gda.client.viewer.ImageViewer(imageComp, SWT.DOUBLE_BUFFERED);
		pack();
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

	void zoomFit() {
		if (viewer != null)
			viewer.zoomFit();
	}
	IDataset getDataset(){
		return lastImage != null ? new SWTImageDataConverter(lastImage).toIDataset() : null;
	}

	ImageData lastImage;
	private Label lastImageId;
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
	private final class VideoListener implements ImageListener<ImageData> {
		private String name;

		@Override
		public void setName(String name) {
			this.name = name;
		}

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
/*				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e);
				}
*/				getDisplay().asyncExec(new Runnable() {
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
							lastImageId.setText(df.format(new Date()));
							if( newImageListener != null)
								newImageListener.handlerNewImageNotification();
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