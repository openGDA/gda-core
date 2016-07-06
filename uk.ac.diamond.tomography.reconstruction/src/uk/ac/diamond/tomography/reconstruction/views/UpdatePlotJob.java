package uk.ac.diamond.tomography.reconstruction.views;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.ReconUtil;

public class UpdatePlotJob extends Job {
	private static final Logger logger = LoggerFactory.getLogger(UpdatePlotJob.class);
	private static final String ERR_TITLE = "Problem loading data";
	private static final String ERR_MESSAGE = "Unable to locate image for the slice. Please run 'Preview Recon'. ";
	private String nexusFileLocation;
	private int pixelPosition;
	private final IViewPart viewPart;

	public UpdatePlotJob(IViewPart viewPart) {
		super("");
		this.viewPart = viewPart;
	}

	@Override
	protected void canceling() {
		super.canceling();
	}

	public void setNexusFileLocation(String nexusFileLocation) {
		this.nexusFileLocation = nexusFileLocation;
	}

	public void setPixelPosition(int pixelPosition) {
		this.pixelPosition = pixelPosition;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("", IProgressMonitor.UNKNOWN);
		String pathToImages = ReconUtil.getReconstructedReducedDataDirectoryPath(nexusFileLocation);

		File imageFile = new File(pathToImages, String.format(ReconUtil.RECONSTRUCTED_IMAGE_FILE_FORMAT, pixelPosition
				/ ProjectionsView.SPLITS));
		logger.debug("Looking for image file {}", imageFile.getPath());
		if (imageFile.exists()) {
			// update monitor
			monitor.worked(1);

			try {
				DataHolder data = new TIFFImageLoader(imageFile.getAbsolutePath()).loadFile();

				// update monitor
				monitor.worked(1);

				Dataset image = data.getDataset(0);
				image.isubtract(image.min());
				image.imultiply(1000.0);

				// update monitor
				monitor.worked(1);

				SDAPlotter.imagePlot(ProjectionsView.PLOT_VIEW_TO_DISPLAY_RECON_IMAGE, image);

				// update monitor
				monitor.worked(1);
			} catch (Exception e) {
				logger.error("Cannot load recon image for display", e);
				return Status.OK_STATUS;
			}
		} else {

			final IViewSite viewSite = viewPart.getViewSite();
			final IStatusLineManager statusLineManager = viewSite.getActionBars().getStatusLineManager();

			final Display display = viewSite.getShell().getDisplay();
			display.asyncExec(new Runnable() {

				@Override
				public void run() {
					statusLineManager.setErrorMessage(ERR_MESSAGE);
					display.timerExec(5000, new Runnable() {

						@Override
						public void run() {
							statusLineManager.setErrorMessage(null);
						}
					});
				}
			});

			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, ERR_MESSAGE);
		}
		return Status.OK_STATUS;
	}
}