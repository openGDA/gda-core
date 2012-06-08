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

package uk.ac.gda.client.experimentdefinition.components;


import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;


import com.swtdesigner.SWTResourceManager;

/**
 * @author fcp94556
 */
public class ExperimentLabelProvider extends LabelProvider {

	private final Image rootIcon;
	private final Image folderIcon;
	private final Image lockFolderIcon;
	private final Image multiRunFileIcon;
	private final Image runIcon;
//	private final Image errorRunFileIcon;
//	private final Image errorRunIcon;
	private ExperimentContentProvider model;

	/**
	 * @param model
	 */
	public ExperimentLabelProvider(ExperimentContentProvider model) {
		this.rootIcon = SWTResourceManager.getImage(ExperimentLabelProvider.class, "/chart_curve_link.png");
		this.folderIcon = SWTResourceManager.getImage(ExperimentLabelProvider.class, "/folder.png");
		this.lockFolderIcon = SWTResourceManager.getImage(ExperimentLabelProvider.class, "/folder_delete.png");
		this.multiRunFileIcon = SWTResourceManager.getImage(ExperimentLabelProvider.class, "/chart_curve.png");
//		this.errorRunFileIcon = SWTResourceManager.getImage(ExperimentLabelProvider.class, "/chart_curve_error.png");
		this.runIcon = SWTResourceManager.getImage(ExperimentLabelProvider.class, "/chart_line.png");
//		this.errorRunIcon = SWTResourceManager.getImage(ExperimentLabelProvider.class, "/chart_line_error.png");
		this.model = model;
	}

	@Override
	public Image getImage(Object element) {
		if (element == null)
			return null;
		if (element.equals(model.getRoot())) {
			return rootIcon;
		}
		if (element instanceof IFolder) {
			final IFolder folder = (IFolder) element;
			return folder.getResourceAttributes().isReadOnly() ? lockFolderIcon : folderIcon;

		} else if (element instanceof IExperimentObjectManager) {
			return /*(((IExperimentObjectManager) element).checkError()) ? errorRunFileIcon :*/ multiRunFileIcon;
		} else if (element instanceof IExperimentObject) {
			return /*(((IExperimentObject) element).isError()) ? errorRunIcon : */runIcon;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element == null)
			return null;
		if (element.equals(model.getRoot())) {
			return "Workspace";
		}

		if (element instanceof IFolder) {
			final IFolder folder = (IFolder) element;
			return folder.getName();

		} else if (element instanceof IExperimentObjectManager) {
			final IExperimentObjectManager man = (IExperimentObjectManager) element;
			return man.getName();

		} else if (element instanceof IExperimentObject) {
			final IExperimentObject ob = (IExperimentObject) element;
			if (ob.getNumberRepetitions() > 1) {
				return ob.getRunName() + " (" + ob.getNumberRepetitions() + ")";
			}
			return ob.getRunName() != null ? ob.getRunName() : "";
		}
		return null;
	}
}
