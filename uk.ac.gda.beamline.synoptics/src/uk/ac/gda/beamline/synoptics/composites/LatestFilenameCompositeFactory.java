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

package uk.ac.gda.beamline.synoptics.composites;

import gda.device.detectorfilemonitor.FileProcessor;
import gda.rcp.GDAClientActivator;
import gda.rcp.views.CompositeFactory;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.beamline.synoptics.utils.DataFileListener;
/**
 * CompositeFactory to create a Composite for displaying latest collected detector data file and 
 * used for processing these detector data files in a file set reported by an instance of
 * {@link DataFileListener}. The processing is done by a FileProcessor object, see {@link DetectorFileDisplayer}.
 */
public class LatestFilenameCompositeFactory implements CompositeFactory, InitializingBean {
	// private static final Logger logger = LoggerFactory.getLogger(LatestFileNameCompositeFactory.class);
	FileProcessor fileProcessor;
	String label;
	private int startNumber=0;
	private DataFileListener dirWatcher;
	private String[] detectors;

	public FileProcessor getFileProcessor() {
		return fileProcessor;
	}

	public void setFileProcessor(FileProcessor fileProcessor) {
		this.fileProcessor = fileProcessor;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	private boolean showButtonSeparator;
	
	public void setShowButtonSeparator(boolean showButtonSeparator) {
		this.showButtonSeparator = showButtonSeparator;
	}
	
	private boolean separatePlayPauseButtons;
	private LatestFilenameComposite comp;
	
	public void setSeparatePlayPauseButtons(boolean separatePlayPauseButtons) {
		this.separatePlayPauseButtons = separatePlayPauseButtons;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		final Image toLatestImage = GDAClientActivator.getImageDescriptor("icons/control_end_blue.png").createImage();
		final Image toStartImage = GDAClientActivator.getImageDescriptor("icons/control_start_blue.png").createImage();
		final Image backOneImage = GDAClientActivator.getImageDescriptor("icons/control_rewind_blue.png").createImage();
		final Image forwardOneImage = GDAClientActivator.getImageDescriptor("icons/control_fastforward_blue.png").createImage();
		final Image pauseImage = GDAClientActivator.getImageDescriptor("icons/control_pause_blue.png").createImage();
		final Image runImage = GDAClientActivator.getImageDescriptor("icons/control_play_blue.png").createImage();

		comp = new LatestFilenameComposite(parent, style);
		comp.setLabel(label);
		comp.setFileProcessor(fileProcessor);
		comp.setImages(toStartImage, toLatestImage, pauseImage, runImage, backOneImage, forwardOneImage);
		comp.setShowButtonSeparator(showButtonSeparator);
		comp.setSeparatePlayPauseButtons(separatePlayPauseButtons);
		comp.setStartNumber(startNumber);
		comp.setDirWatcher(dirWatcher);
		comp.setDetectors(detectors);
		
		comp.createControls();
		comp.initialize();
		
		return comp;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (fileProcessor == null) {
			throw new IllegalArgumentException("fileProcessor == null");
		}
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}
		if (dirWatcher == null) {
			throw new IllegalArgumentException("dirWatcher == null");
		}
	}
	
	public int getStartNumber() {
		return startNumber;
	}
	
	public void setStartNumber(int startNumber) {
		this.startNumber = startNumber;
	}
	
	public DataFileListener getDirWatcher() {
		return dirWatcher;
	}
	
	public void setDirWatcher(DataFileListener dirWatcher) {
		this.dirWatcher = dirWatcher;
	}


	public String[] getDetectors() {
		return detectors;
	}

	public void setDetectors(String[] detectors) {
		this.detectors = detectors;
	}
}
