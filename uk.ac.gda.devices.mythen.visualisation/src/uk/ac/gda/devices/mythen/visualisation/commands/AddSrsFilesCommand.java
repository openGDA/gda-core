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

package uk.ac.gda.devices.mythen.visualisation.commands;

import gda.device.detector.mythen.data.MythenSrsFileLoader;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.springframework.util.StringUtils;

import uk.ac.gda.devices.mythen.visualisation.views.MythenDataControlView;

public class AddSrsFilesCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MythenDataControlView controlView = MythenDataControlView.getInstance();
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		FileDialog fileDialog = new FileDialog(shell, SWT.SINGLE);
		String srsFilename = fileDialog.open();
		
		if (StringUtils.hasLength(srsFilename)) {
			try {
				MythenSrsFileLoader loader = new MythenSrsFileLoader();
				String[] filenames = loader.load(srsFilename);
				for (String filenameWithoutSuffix : filenames) {
					String filenameWithSuffix = filenameWithoutSuffix + ".dat";
					controlView.addFileToTable(filenameWithSuffix);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		return null;
	}

}
