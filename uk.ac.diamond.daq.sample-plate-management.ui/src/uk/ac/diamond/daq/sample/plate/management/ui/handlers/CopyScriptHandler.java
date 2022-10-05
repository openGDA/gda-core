/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui.handlers;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.daq.sample.plate.management.ui.ScriptEditorView;
public class CopyScriptHandler {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part) {
		String script = ((ScriptEditorView) part.getObject()).getScript();
		Clipboard clipboard = new Clipboard(Display.getCurrent());
		clipboard.setContents(new Object[] {script}, new Transfer[] {TextTransfer.getInstance()});
		clipboard.dispose();
	}

	@CanExecute
	public boolean canExecute() {
		return true;
	}
}
