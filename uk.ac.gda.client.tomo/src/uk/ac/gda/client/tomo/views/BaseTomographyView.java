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

package uk.ac.gda.client.tomo.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTomographyView extends ViewPart {

	public static final Logger logger = LoggerFactory.getLogger(BaseTomographyView.class);

	protected void doPartDeactivated() {
		// do nothing in the base class
	}

	private TomoPartAdapter tomoPartAdapter;

	class TomoPartAdapter implements IPartListener2 {
		@Override
		public void partHidden(org.eclipse.ui.IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomographyView.this)) {
				doPartDeactivated();
			}
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomographyView.this)) {
				BaseTomographyView.this.createCheckIOCStatusJob().schedule(1000);
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			// Do nothing
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomographyView.this)) {
				doPartDeactivated();
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomographyView.this)) {
				if (BaseTomographyView.this.checkIocStatusJob != null) {
					BaseTomographyView.this.checkIocStatusJob.cancel();
				}
				BaseTomographyView.this.checkIocStatusJob = null;
				doPartDeactivated();
			}
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomographyView.this)) {
				BaseTomographyView.this.createCheckIOCStatusJob().schedule(1000);
			}
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			// Do nothing
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
			// Do nothing
		}

	};

	private CheckIOCStatusJob createCheckIOCStatusJob() {
		if (checkIocStatusJob == null) {
			checkIocStatusJob = new CheckIOCStatusJob("IOC may be down");
		}
		return checkIocStatusJob;
	}

	protected void addPartListener() {
		tomoPartAdapter = new TomoPartAdapter();
		getSite().getPage().addPartListener(tomoPartAdapter);
	}

	protected abstract String getDetectorPortName() throws Exception;

	private class CheckIOCStatusJob extends Job {

		boolean isCancelled = false;

		@Override
		protected void canceling() {
			isCancelled = true;
		}

		public CheckIOCStatusJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IContextService cs = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
			if (cs != null) {

				try {
					getDetectorPortName();
					if (contextActivation == null) {
						activateContext(cs);
					}
				} catch (Exception ex) {
					logger.debug("Deactivating context :{}", ex);
					deactivateContext(cs);
				} finally {
					if (!isCancelled) {
						this.schedule(2000);
					}
				}
			}
			return Status.OK_STATUS;
		}
	}

	private IContextActivation contextActivation;

	private void deactivateContext(final IContextService contextService) {
		if (!getViewSite().getShell().getDisplay().isDisposed()) {
			getViewSite().getShell().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					contextService.deactivateContext(contextActivation);
					contextActivation = null;
				}
			});
		}
	}

	private void activateContext(final IContextService contextService) {
		if (!getViewSite().getShell().getDisplay().isDisposed()) {
			getViewSite().getShell().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					contextActivation = contextService.activateContext(getIocRunningContext());
				}
			});
		}
	}

	protected abstract String getIocRunningContext();

	private CheckIOCStatusJob checkIocStatusJob;

	@Override
	public void dispose() {
		if (checkIocStatusJob != null) {
			checkIocStatusJob.cancel();
		}
		IContextService cs = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		if (cs != null && contextActivation != null) {
			cs.deactivateContext(contextActivation);
			contextActivation = null;
		}
		getSite().getPage().removePartListener(tomoPartAdapter);
		super.dispose();
	}
}
