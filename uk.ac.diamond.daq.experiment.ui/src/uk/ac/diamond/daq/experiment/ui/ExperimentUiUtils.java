/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * It is hoped that using this utility class will result in a more or less consistent UI feel
 */
public class ExperimentUiUtils {

	/* ICONS */
	private static final String ICONS_DIR = "/icons/";
	public static final String RUN_ICON = ICONS_DIR + "navigation.png";
	public static final String CONFIGURE_ICON = ICONS_DIR + "gear--arrow.png";
	public static final String PLUS_ICON = ICONS_DIR + "plus-circle.png";
	public static final String MINUS_ICON = ICONS_DIR + "minus-circle.png";
	public static final String UP_ICON = ICONS_DIR + "arrow_up.png";
	public static final String DOWN_ICON = ICONS_DIR + "arrow_down.png";


	/**
	 * This GridDataFactory can be applied to controls which should
	 * fill horizontal space
	 */
	public static final GridDataFactory STRETCH = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

	public static void addSpace(Composite composite) {
		new Label(composite, SWT.NONE);
	}

	/**
	 * The path could be icon path fields in this class
	 */
	public static Image getImage(String path) {
		return INSTANCE.getIcon(path);
	}

	private ExperimentUiUtils() {
		// static access only!
	}

	private static final ExperimentUiUtils INSTANCE = new ExperimentUiUtils();

	private Image getIcon(String path) {
		return new Image(Display.getCurrent(), getClass().getResourceAsStream(path));
	}


}
