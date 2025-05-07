/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.standards;

import javax.annotation.PostConstruct;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardsExafsScanView extends StandardsScanView{
	private static final Logger logger = LoggerFactory.getLogger(StandardsExafsScanView.class);

	private static final String SCRIPT_FILE = "scanning/submit_standards_exafs_scan.py";
	private static final String BUTTON_NAME = "Submit EXAFS standards scan";
	private static final Color BUTTON_COLOUR = new Color(Display.getDefault(), new RGB(170, 204, 0));

	@Override
	@PostConstruct
	public void createView(Composite parent) {
		super.createView(parent);
	}

	@Override
	protected String getScriptFile() {
		return SCRIPT_FILE;
	}

	@Override
	protected String getButtonName() {
		return BUTTON_NAME;
	}

	@Override
	protected Color getButtonColour() {
		return BUTTON_COLOUR;
	}


}
