/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;


public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {

		boolean useToolBar = LocalProperties.check(LocalProperties.GDA_GUI_USE_TOOL_BAR, true);
		boolean usePerspectiveBar = LocalProperties.check(LocalProperties.GDA_GUI_USE_PERSPECTIVE_BAR, true);
		int width = LocalProperties.getAsInt(LocalProperties.GDA_GUI_START_WIDTH, 1450);
		int height = LocalProperties.getAsInt(LocalProperties.GDA_GUI_START_HEIGHT, 900);

		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(width, height));
		configurer.setShowCoolBar(useToolBar);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowPerspectiveBar(usePerspectiveBar);
		final String prefix = LocalProperties.get(LocalProperties.GDA_GUI_TITLEBAR_PREFIX);
		final String suffix = LocalProperties.get(LocalProperties.GDA_GUI_TITLEBAR_SUFFIX);
		String title;
		if (prefix != null) {
			title = prefix + " - GDA - " + gda.util.Version.getRelease();
		} else if (suffix != null){
			title = "GDA " + gda.util.Version.getRelease() + " " + suffix;
		} else {
			String beamLineName = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
			title = String.format("Data Acquisition Client - Beamline %s - %s", beamLineName == null ? "Unknown"
					: beamLineName.toUpperCase(), gda.util.Version.getRelease());
		}
		configurer.setTitle(title);

	}

	@Override
	public void openIntro() {
		handleForceIntroProperty();
		super.openIntro();
	}

	private static void handleForceIntroProperty() {

		Boolean useIntroScreen = null;
		if (LocalProperties.contains(LocalProperties.GDA_GUI_FORCE_INTRO)) {
			useIntroScreen = LocalProperties.check(LocalProperties.GDA_GUI_FORCE_INTRO);
		}

		// Option to save and restore the GUI state between sessions. For GDA default is 'false'.
		// If LocalProperties.GDA_GUI_SAVE_RESTORE is set to true, this setting to force Intro may have no effect
		if (useIntroScreen != null) {
			PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_INTRO, useIntroScreen);
		}
	}

	@Override
	public void postWindowOpen() {
		boolean doMaximise = LocalProperties.check(LocalProperties.GDA_GUI_START_MAXIMISE,false);
		if(doMaximise) {
			IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
			Shell shell = configurer.getWindow().getShell();
			shell.setMaximized(true);
		}
		super.postWindowOpen();
	}

	@Override
	public boolean preWindowShellClose() {
		SWTResourceManager.disposeColors();
		return super.preWindowShellClose();
	}
}
