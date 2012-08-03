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

package uk.ac.gda.client.tomo.configuration.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.TomoExperiment;

/**
 *
 */
public class TomoConfigContentProvider implements IStructuredContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];

	public TomoConfigContentProvider() {
		super();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object[] getElements(Object parentElement) {
		if (parentElement instanceof TomoExperiment) {
			TomoExperiment tomoExperiment = (TomoExperiment) parentElement;
			return getTomoConfigContent(tomoExperiment);
		}

		if (parentElement instanceof Object[]) {
			return (Object[]) parentElement;
		}

		if (parentElement instanceof Collection) {
			return ((Collection) parentElement).toArray();
		}
		return EMPTY_ARRAY;
	}

	private Object[] getTomoConfigContent(TomoExperiment tomoExperiment) {
		List<AlignmentConfiguration> configurationSet = tomoExperiment.getParameters().getConfigurationSet();

		ArrayList<ITomoConfigContent> configContents = new ArrayList<ITomoConfigContent>();
		for (AlignmentConfiguration alignmentConfiguration : configurationSet) {
			TomoConfigContent configContent = new TomoConfigContent();
			TomoConfigViewerUtil.setupConfigContent(alignmentConfiguration, configContent);
			configContents.add(configContent);
		}
		return configContents.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
