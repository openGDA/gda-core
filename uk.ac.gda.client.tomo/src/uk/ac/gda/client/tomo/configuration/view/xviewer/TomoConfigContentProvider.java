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

package uk.ac.gda.client.tomo.configuration.view.xviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.TomoExperiment;

/**
 *
 */
public class TomoConfigContentProvider implements ITreeContentProvider {
	protected Collection<AlignmentConfiguration> rootSet = new HashSet<AlignmentConfiguration>();
	private static Object[] EMPTY_ARRAY = new Object[0];

	public TomoConfigContentProvider() {
		super();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object[] getChildren(Object parentElement) {

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
			configContent.setSampleDescription(alignmentConfiguration.getDescription());
			configContent.setFlatExposureTime(alignmentConfiguration.getFlatExposureTime());
			configContent.setSampleExposureTime(alignmentConfiguration.getSampleExposureTime());
			configContent.setUserId(alignmentConfiguration.getCreatedUserId());
			configContents.add(configContent);
		}
		return configContents.toArray();
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof String)
			return new Object[] { inputElement };
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * @return the rootSet
	 */
	public Collection<AlignmentConfiguration> getRootSet() {
		return rootSet;
	}

}
