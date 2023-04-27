/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.LiveControlBase;
import uk.ac.gda.ui.utils.ImageTools;

public class LiveControlsImage extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(LiveControlsImage.class);

	private String imageName = "";
	private String busyImageName = "";
	private boolean generateBusyImage;
	private int busyImageColourThreshold = 100;

	private List<Scannable> scannablesToObserve = Collections.emptyList();
	private String labelText = "";
	private int highlightColour = SWT.COLOR_RED;
	private int colourToReplace;
	private int imageRotation = 0;

	@Override
	public void createControl(Composite composite) {
		setupImage(composite);
	}

	private void setupImage(Composite comp) {
		HighlightImageLabel imageLabel = new HighlightImageLabel(comp);
		imageLabel.getControl().setLayoutData(null);

		getImage(imageName).ifPresent(image -> {
			imageLabel.setImage(image);

			// Make sure the parent composite is the correct size for the image
			var imageBounds = image.getBounds();
			comp.setSize(imageBounds.width, imageBounds.height);
		});

		if (generateBusyImage && imageLabel.getImage() != null) {
			RGB colourToReplaceRgb;
			logger.info("Generating 'busy' image from {}", imageName);
			if (colourToReplace > 0) {
				logger.debug("Replacing user specified colour : {}", colourToReplace);
				colourToReplaceRgb = Display.getDefault().getSystemColor(colourToReplace).getRGB();
			} else {
				colourToReplaceRgb = ImageTools.getDominantColour(imageLabel.getImage());
				logger.debug("Replacing dominant image colour : {}", colourToReplaceRgb);
			}

			var highlightRgb = Display.getDefault().getSystemColor(highlightColour).getRGB();
			Image busyImage = ImageTools.replaceColour(imageLabel.getImage(), colourToReplaceRgb, highlightRgb, busyImageColourThreshold);
			imageLabel.setHighlightImage(busyImage);
		} else {
			logger.info("Using busy image from {}", busyImageName);
			getImage(busyImageName).ifPresent(imageLabel::setHighlightImage);
		}

		if (!StringUtils.isEmpty(labelText)) {
			imageLabel.setLabelText(labelText);
		}

		imageLabel.setHighLightColour(highlightColour);

		// Make the label listen to scannable events
		scannablesToObserve.forEach(imageLabel::addScannableToMonitor);

		// Remove the listener when the widget is disposed
		imageLabel.getControl().addDisposeListener(l ->
			scannablesToObserve.forEach(scn -> scn.deleteIObserver(imageLabel))
		);
	}

	protected Optional<Image> getImage(String pathToImage) {
		if (StringUtils.isEmpty(pathToImage)) {
			return Optional.empty();
		}
		try {
			Image originalImage = SynopticView.getImage(pathToImage);
			return Optional.of(getRotatedImage(originalImage));
		} catch(IOException e) {
			logger.warn("Problem getting image for {}", getName(),e);
			return Optional.empty();
		}
	}

	private Image getRotatedImage(Image image) {
		if (imageRotation == 0) {
			return image;
		} else {
			Image rotatedImage = ImageTools.getRotatedImage(image, imageRotation);
			Image transparentImage = ImageTools.setTransparentPixels(rotatedImage, 220);
			image.dispose();
			rotatedImage.dispose();
			return transparentImage;
		}
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public void setBusyImageName(String busyImageName) {
		this.busyImageName = busyImageName;
	}

	public void setLabelText(String labelText) {
		this.labelText = labelText;
	}

	/**
	 * Set the colour to be used to highlight the text/image
	 * when one of the scannables being observed is busy.
	 *
	 * This should be one of the SWT color values ({@link SWT#COLOR_RED}, {@link SWT#COLOR_BLUE} etc)
	 *
	 * @param highlightColour
	 */
	public void setHighlightColour(int highlightColour) {
		this.highlightColour = highlightColour;
	}

	public void setScannablesToObserve(List<String> scannableNames) {
		scannablesToObserve = new ArrayList<>();
		for(String name : scannableNames) {
			Finder.findOptionalOfType(name, Scannable.class)
					.ifPresentOrElse(scn -> scannablesToObserve.add(scn),
					() -> logger.warn("Could not add {} to list of scannables to observe - scannable not found", name));
		}
	}

	public void setScannableToObserve(String scannableNames) {
		setScannablesToObserve(Arrays.asList(scannableNames));
	}

	public int getImageRotation() {
		return imageRotation;
	}

	public void setImageRotation(int imageRotation) {
		this.imageRotation = imageRotation;
	}

	public boolean isGenerateBusyImage() {
		return generateBusyImage;
	}

	public void setGenerateBusyImage(boolean generateBusyImage) {
		this.generateBusyImage = generateBusyImage;
	}
}
