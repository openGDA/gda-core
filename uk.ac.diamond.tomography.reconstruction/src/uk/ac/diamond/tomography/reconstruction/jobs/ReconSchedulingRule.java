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

import org.eclipse.core.runtime.jobs.ISchedulingRule;

import uk.ac.diamond.tomography.reconstruction.Activator;

public class ReconSchedulingRule implements ISchedulingRule {

	private String nexusFile;

	public ReconSchedulingRule(String nexusFileLocation) {
		this.nexusFile = nexusFileLocation;
	}

	public String getNexusFileFullLocation() {
		return nexusFile;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof ReconSchedulingRule) {
			return this.getNexusFileFullLocation().equals(((ReconSchedulingRule) rule).getNexusFileFullLocation());
		}
		return false;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule.equals(this)
				|| Activator.getDefault().getTomoFilesProject().equals(rule)
				|| (rule instanceof ReconSchedulingRule && (getNexusFileFullLocation()
						.equals(((ReconSchedulingRule) rule).getNexusFileFullLocation())));
	}

}
