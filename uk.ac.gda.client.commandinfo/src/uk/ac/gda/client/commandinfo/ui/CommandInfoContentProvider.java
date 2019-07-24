/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.commandinfo.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.commandinfo.ICommandThreadInfo;
import uk.ac.gda.client.commandinfo.CommandInfoModel;

public class CommandInfoContentProvider implements IStructuredContentProvider {

	private static final Logger logger = LoggerFactory.getLogger(CommandInfoContentProvider.class);

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		@SuppressWarnings("unused")
		int debug = 0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		ICommandThreadInfo[] infos = new ICommandThreadInfo[0];
		try {
			if (inputElement instanceof CommandInfoModel) {
				CommandInfoModel model = (CommandInfoModel) inputElement;
				infos = model.getCommandElements();
			}
		} catch (Exception e) {
			logger.error("Cannot retrieve command information.", e);
		}
		return infos;
	}
}
