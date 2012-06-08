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

package gda.rcp.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextViewer;

/**
 * Action for enabling/disabling word wrap in a text viewer. The setting is
 * not persisted. The default setting is for word wrapping to be disabled.
 */
public class TextViewerWordWrapToggleAction extends Action {
	
	private ITextViewer textViewer;
	
	public TextViewerWordWrapToggleAction(ITextViewer textViewer) {
		super("Wrap Text", IAction.AS_CHECK_BOX);
		this.textViewer = textViewer;
	}
	
	@Override
	public void run() {
		textViewer.getTextWidget().setWordWrap(isChecked());
	}
	
}
