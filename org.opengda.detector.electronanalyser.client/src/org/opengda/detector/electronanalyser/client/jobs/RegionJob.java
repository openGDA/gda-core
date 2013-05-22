package org.opengda.detector.electronanalyser.client.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;
import org.opengda.detector.electronanalyser.RegionCommand;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionJob extends Job {
	private static final Logger logger=LoggerFactory.getLogger(RegionJob.class);
	/**
	 * A family identifier for all region jobs
	 */
	public static final Object FAMILY_REGION_JOB = new Object();

	private RegionCommand regionCommand;

	public RegionJob(String name,  RegionCommand regionCommand) {
		super(name);
		this.regionCommand = regionCommand;
		setProperty(IProgressConstants.ICON_PROPERTY, ElectronAnalyserClientPlugin
				.imageDescriptorFromPlugin(ElectronAnalyserClientPlugin.PLUGIN_ID,
						ImageConstants.ICON_SES));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			regionCommand.run();
		} catch (Exception e) {
			logger.error("Exception throw on run region command", e);
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
	
	public Region getRegion() {
		return regionCommand.getRegion();
	}
	
	@Override
	protected void canceling() {
		regionCommand.abort();
		super.canceling();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.jobs.InternalJob#belongsTo(java.lang.Object)
	 */
	@Override
	public boolean belongsTo(Object family) {
		if (family instanceof RegionJob) {
			return true;
		}
		return family==FAMILY_REGION_JOB;
	}

}
