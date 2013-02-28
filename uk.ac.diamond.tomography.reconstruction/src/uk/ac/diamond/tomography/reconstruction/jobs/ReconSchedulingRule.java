/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.jobs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class ReconSchedulingRule implements ISchedulingRule {

	private IFile nexusFile;

	public ReconSchedulingRule(IFile nexusFile) {
		this.nexusFile = nexusFile;
	}

	public IFile getNexusFile() {
		return nexusFile;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof ReconSchedulingRule) {
			return this.getNexusFile().equals(((ReconSchedulingRule) rule).getNexusFile());
		}
		return false;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule.equals(this);
	}

}
