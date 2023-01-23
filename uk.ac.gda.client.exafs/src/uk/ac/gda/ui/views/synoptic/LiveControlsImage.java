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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.LiveControlBase;

public class LiveControlsImage extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(LiveControlsImage.class);

	private String imageName = "";
	private String busyImageName = "";
	private List<Scannable> scannablesToObserve = Collections.emptyList();
	private String labelText = "";
	private int highlightColour = SWT.COLOR_RED;

	@Override
	public void createControl(Composite composite) {
		setupImage(composite);
	}

	private void setupImage(Composite comp) {
		HighlightImageLabel imageLabel = new HighlightImageLabel(comp);
		imageLabel.getControl().setLayoutData(null);
		if (!StringUtils.isEmpty(imageName)) {
			Image image = getImageFromPlugin(imageName);
			imageLabel.setImage(image);
		}
		if (!StringUtils.isEmpty(busyImageName)) {
			Image image = getImageFromPlugin(busyImageName);
			imageLabel.setHighlightImage(image);
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

	protected Image getImageFromPlugin(String pathToImage) {
		URL imageURL = this.getClass().getResource("/"+pathToImage);
		return ImageDescriptor.createFromURL(imageURL).createImage();
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

}
