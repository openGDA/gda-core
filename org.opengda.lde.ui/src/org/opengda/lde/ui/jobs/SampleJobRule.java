package org.opengda.lde.ui.jobs;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
/**
 * a scheduling rule that makes all sample job sequential.
 *
 */
public class SampleJobRule implements ISchedulingRule {

	private int jobOrder;

	public SampleJobRule(int order) {
		this.jobOrder = order;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean contains(ISchedulingRule rule) {
		if (rule instanceof IResource || rule instanceof SampleJobRule) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (!(rule instanceof SampleJobRule)) {
			return false;
		}
		return ((SampleJobRule) rule).getJobOrder() >= jobOrder;
	}

	public int getJobOrder() {
		return jobOrder;
	}


}
