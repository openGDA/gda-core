/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.util.VisitPath;

public class ScanOverrideView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(ScanOverrideView.class);

	private IPartListener partListener = new IPartListener() {
		@Override
		public void partActivated(IWorkbenchPart part) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}
	};

	public ScanOverrideView() {
	}

	ScanOverrideComposite scanOverrideComposite;

	@Override
	public void createPartControl(Composite parent) {
		scanOverrideComposite = new ScanOverrideComposite(parent);
		scanOverrideComposite.setXmlDirectory(VisitPath.getVisitPath());
		scanOverrideComposite.createTableAndControls();

		getSite().getPage().addPartListener(partListener);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(partListener);
	}

}

