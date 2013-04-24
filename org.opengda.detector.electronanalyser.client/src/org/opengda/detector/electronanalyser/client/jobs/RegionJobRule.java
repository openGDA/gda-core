package org.opengda.detector.electronanalyser.client.jobs;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
/**
 * RegionJobRule is a scheduling rule that makes all region job sequential.
 *
 */
public class RegionJobRule implements ISchedulingRule {

	private int jobOrder;

	public RegionJobRule(int order) {
		this.jobOrder = order;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean contains(ISchedulingRule rule) {
		if (rule instanceof IResource || rule instanceof RegionJobRule) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (!(rule instanceof RegionJobRule)) {
			return false;
		}
		return ((RegionJobRule) rule).getJobOrder() >= jobOrder;
	}

	public int getJobOrder() {
		return jobOrder;
	}


}
