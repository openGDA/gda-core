package org.opengda.lde.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.ui.Activator;
import org.opengda.lde.ui.ImageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleCollectionJob extends Job {
	private static final Logger logger=LoggerFactory.getLogger(SampleCollectionJob.class);
	/**
	 * A family identifier for all sample jobs
	 */
	public static final Object FAMILY_SAMPLE_JOB = new Object();

	private SampleCommand sampleCommand;

	public SampleCollectionJob(String name,  SampleCommand command) {
		super(name);
		this.sampleCommand = command;
		setProperty(IProgressConstants.ICON_PROPERTY, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,ImageConstants.ICON_RUNNING));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			sampleCommand.run();
		} catch (Exception e) {
			logger.error("Exception throw on run sample collection command", e);
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
	
	public Sample getSample() {
		return sampleCommand.getSample();
	}
	
	@Override
	protected void canceling() {
		sampleCommand.abort();
		super.canceling();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.jobs.InternalJob#belongsTo(java.lang.Object)
	 */
	@Override
	public boolean belongsTo(Object family) {
		if (family instanceof SampleCollectionJob) {
			return true;
		}
		return family==FAMILY_SAMPLE_JOB;
	}

}
