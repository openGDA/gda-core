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

package uk.ac.diamond.daq.mapping.ui.experiment.file;

import java.util.Comparator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * Provides content for controls based on the mapping scan Descriptive Filename Format that identifies the region
 * type of the scan definition stored in the file and provides a comparator to facilitate sorting.
 *
 * @since GDA 9.13
 */
public class SavedScanShapeLabelProvider extends LabelProvider implements IComparableStyledLabelProvider {

	private static final String[] ICONS = {"point", "rectangle", "centred_rectangle", "line", "circle", "poly"};
	private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

    /**
     * Return the standard image representing the region shape of the scan or null if there is no matching icon file
     */
	@Override
    public Image getImage(Object element) {
		if (!(element instanceof String  || element instanceof SavedScanMetaData)) {
			return null;
		}
		String[] tokens = splitOnDot(element.toString());

		if (tokens.length > 2 && tokens[tokens.length - 2].matches("^S[0-9].*")) {
			int i = Integer.valueOf(tokens[1].substring(1, 2));
			String iconName = String.format("icons/%s.png", ICONS[i]);
			ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, iconName);
			return descriptor != null ? resourceManager.createImage(descriptor) : null;
		}
		return null;
    }

	/**
	 * Dummy implementation returning and empty object
	 */
	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString();
		}

	/**
	 * Compares based on the first two characters of the scan definition description string, which are S followed by the
	 * shape  number and the direction indicated by the viewer column
	 */
	@Override
	public ViewerComparator getComparator() {
		return new ViewerComparator() {
		    @Override
			public int compare(Viewer viewer, Object element1, Object element2) {
		    	String[] first = splitOnDot(element1);
		    	String[] second = splitOnDot(element2);

		    	if (first.length < 3 || second.length < 3) {
			    	return (first.length < second.length) ? 1 : -1;
		    	}
		    	Comparator<String> c = Comparator.comparing(String::toString);
		    	int direction = ((TreeViewer)viewer).getTree().getSortDirection() == SWT.UP ? 1 : -1;
		    	return direction * c.compare(penultimateOf(first).substring(0, 2), penultimateOf(second).substring(0,2));
		    }
		};
	}
}
