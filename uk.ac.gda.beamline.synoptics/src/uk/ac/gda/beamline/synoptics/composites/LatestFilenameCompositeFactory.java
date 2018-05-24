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

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detectorfilemonitor.FileProcessor;
import gda.rcp.GDAClientActivator;
import gda.rcp.views.CompositeFactory;
import uk.ac.gda.beamline.synoptics.utils.DataDirectoryMonitor;

/**
 * CompositeFactory to create a Composite for displaying latest collected detector data file and
 * used for processing these detector data files in a file set reported by an instance of
 * {@link DataDirectoryMonitor}. The processing is done by a FileProcessor object.
 */
public class LatestFilenameCompositeFactory implements CompositeFactory, InitializingBean {
	FileProcessor fileProcessor;
	String label;
	private int startNumber=0;
	private DataDirectoryMonitor dirWatcher;
	private Map<String, Predicate<String>> detectors;

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

	public DataDirectoryMonitor getDirWatcher() {
		return dirWatcher;
	}

	public void setDirWatcher(DataDirectoryMonitor dirWatcher) {
		this.dirWatcher = dirWatcher;
	}


	public Map<String, Predicate<String>> getDetectors() {
		return detectors;
	}

	/**
	 * Set the list of detectors to filter filenames by. Filenames match iff they
	 * contain the name of the detector
	 *
	 * @see #setDetectors(Map)
	 * @param detectors
	 */
	public void setDetectors(String[] detectors) {
		// if just a list of detectors is given, use the detector name as the regex
		setDetectors(Arrays.stream(detectors).collect(toMap(s -> s, s -> s)));
	}

	/**
	 * Set the map of label name/match regex to use to filter scan files
	 *
	 * @see LatestFilenameComposite#setDetectors(Map)
	 * @param detectors
	 */
	public void setDetectors(Map<String, String> detectors) {
		this.detectors = detectors
				.entrySet()
				.stream()
				.collect(toMap(
						Entry::getKey,
						e -> Pattern.compile(e.getValue()).asPredicate()));
	}
}
