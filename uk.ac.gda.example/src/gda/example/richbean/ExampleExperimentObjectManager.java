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

package gda.example.richbean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import uk.ac.gda.client.experimentdefinition.ExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;

public class ExampleExperimentObjectManager extends ExperimentObjectManager implements IExperimentObjectManager {

	@Override
	protected IExperimentObject createNewExperimentObject(String line) {
		final String[] items = line.split(" ");
		if (items.length > 2) {
			return createNewExperimentObject(items[0], items[1], Integer.parseInt(items[2]));
		}
		return createNewExperimentObject(items[0], items[1], 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IExperimentObject> getExperimentObjectType() {
		return (Class<IExperimentObject>) ExampleExperimentObject.class.asSubclass(IExperimentObject.class);
	}

	@Override
	public IExperimentObject createCopyOfExperiment(IExperimentObject original) throws CoreException {
		String exptName = getUniqueName(original.getRunName());
		IFile scanFile = createCopy(original.getFiles().get(0));
		return createNewExperimentObject(exptName, scanFile.getName(), original.getNumberRepetitions());
	}

	@Override
	public IExperimentObject cloneExperiment(IExperimentObject original) {
		ExampleExperimentObject originalAsExampleExperimentObject = (ExampleExperimentObject) original;
		return createNewExperimentObject(original.getRunName(), originalAsExampleExperimentObject.getScanFileName(),
				original.getNumberRepetitions());
	}

	private IExperimentObject createNewExperimentObject(String runName, String scanFileName, int numRepetitions) {
		ExampleExperimentObject newObject = new ExampleExperimentObject();
		newObject.setMultiScanName(this.getName());
		newObject.setFolder(getContainingFolder());
		newObject.setRunName(runName);
		newObject.setScanFileName(scanFileName);
		newObject.setNumberRepetitions(numRepetitions);
		return newObject;
	}

	@Override
	public String[] getOrderedColumnBeanTypes() {
		return new String[]{ExampleExperimentObject.SCANBEANTYPE};
	}

}
