/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.views.synoptic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.concurrent.Async;

class HighlightImageLabel implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(HighlightImageLabel.class);

	private final Composite parent;
	private Label nameLabel;
	private List<Scannable> scannablesToMonitor = new ArrayList<>();

	/** Position of widget relative to origin of background image (percent). */
	private Point relativePosition = new Point(0,0);

	/** Absolute position of label in parent composite */
	private Point position = new Point(0,0);

	/** Image when scannable is not busy */
	private Image normalImage;

	/** Image shown when scannable is busy */
	private Image busyImage;

	/** Highlight colour used to modify normalImage to create busyImage (default = red)*/
	private int swtHighLightColor = SWT.COLOR_RED;

	private volatile boolean updateInProgress = false;


	public HighlightImageLabel(final Composite parent) {
		this.parent = parent;
		setLayout();
	}

	public HighlightImageLabel(Composite parent, String scannableName) {
		this.parent = parent;
		setLayout();
		Scannable scannable = Finder.find(scannableName);
		addScannableToMonitor(scannable);
	}

	public HighlightImageLabel(Composite parent, Scannable scannable) {
		this.parent = parent;
		setLayout();
		addScannableToMonitor(scannable);
	}

	public void addScannableToMonitor(Scannable scn) {
		scannablesToMonitor.add(scn);
		scn.addIObserver(this);
	}

	private void setLayout() {
		nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		// Add dipose listener to tidy up after the widget is disposed :
		parent.addDisposeListener(l -> {

			// remove this class from each scannable's observers
			scannablesToMonitor.forEach(scn -> scn.deleteIObserver(this));

			// Dispose of the images
			if (busyImage != null) {
				busyImage.dispose();
			}
			if (normalImage != null) {
				normalImage.dispose();
			}
		});
	}

	/** Create busyImage from normalImage by making a copy of it and changing all black pixels to highlightColor */
	public void makeHighLightImage() {
		// Get RGB colour from SWT colour number
		RGB rgbColour = Display.getDefault().getSystemColor(swtHighLightColor).getRGB();

		ImageData imageData = (ImageData) normalImage.getImageData().clone();
		int highlightPixel = imageData.palette.getPixel(rgbColour);
		int blackPixel = imageData.palette.getPixel(new RGB(0,0,0));

		// Change colour of the black pixels to the highlight colour
		for(int i=0; i<imageData.width; i++) {
			for(int j=0; j<imageData.height; j++) {
				int val = imageData.getPixel(i, j);
				if (val==blackPixel) {
					imageData.setPixel(i, j, highlightPixel);
				}
			}
		}
		busyImage = new Image(parent.getDisplay(), imageData);
	}


	public void setImage(Image image) {
		this.normalImage = image;
		setLabelImage(normalImage);
	}

	/**
	 * Set label to use specified image
	 * @param image
	 */
	public void setLabelImage(final Image image) {
		if (parent.isDisposed() || nameLabel.isDisposed()) {
			return;
		}

		runInGuithread(() -> {
			logger.trace("Update label image");
			nameLabel.setImage(image);
		});
	}

	/** Wait for scannable to finishe being busy, update between busy and idle label images */
	private void updateLabelWaitForScannable(Scannable scn) {
		if (scn==null) {
			return;
		}
		updateInProgress = true;
		logger.trace("LineLabel update called ");
		try {
			updateLabelAndImage();
			do {
				logger.trace("Wait while {} is busy", scn.getName());
				Thread.sleep(250);
			} while (scn.isBusy() );
			logger.trace("Scannable movement finished");

		} catch (InterruptedException | DeviceException e) {
			logger.warn("Problem waiting for {} to finish moving", scn.getName(), e);
		} finally {
			updateInProgress = false;
			updateLabelAndImage();
		}
	}

	private void updateLabelAndImage() {
		if (parent.isDisposed() || nameLabel.isDisposed()) {
			return;
		}
		Image image = updateInProgress ? busyImage : normalImage;
		int swtColour = updateInProgress ? swtHighLightColor : SWT.COLOR_BLACK;
		runInGuithread(() -> {
			Color textColor = Display.getDefault().getSystemColor(swtColour);
			logger.trace("Update label image");
			nameLabel.setImage(image);
			nameLabel.setForeground(textColor);
		});

	}

	@Override
	public void update(Object source, Object arg) {
		if (updateInProgress)
			return;
		if (source instanceof Scannable src) {
			Async.execute(() -> updateLabelWaitForScannable(src));
		}
	}

	public void setLabelText(String text) {
		nameLabel.setText(text);
	}

	public void setHighlightImage(Image highlightImage) {
		this.busyImage = highlightImage;
	}

	public Image getHighlightImage() {
		return busyImage;
	}

	public Control getControl() {
		return nameLabel;
	}

	public void setHighLightColour(int swtColour) {
		swtHighLightColor = swtColour;
	}

	private void runInGuithread(Runnable runnable) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
	}
}
