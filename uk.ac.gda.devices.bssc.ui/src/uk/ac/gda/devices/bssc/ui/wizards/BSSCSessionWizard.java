package uk.ac.gda.devices.bssc.ui.wizards;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;

public class BSSCSessionWizard extends Wizard implements INewWizard {
	private BSSCSessionWizardPage page;
	private ISelection selection;
	private double concentration = 0.1;
	private float exposureTemperature = 20;
	private int frames = 10;
	private double timePerFrame = 0.2;
	private String viscosity = "medium";
	private boolean yellowsample = false;

	public BSSCSessionWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new BSSCSessionWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using
	 * wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	private void doFinish(String containerName, String fileName, IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = getContentStream();
			if (file.exists()) {
				throwCoreException("Will not overwrite existing file "+fileName+".");

				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
			throwCoreException("error creating file contents.");
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream getContentStream() {
		BSSCSessionBean sessionBean = new BSSCSessionBean();
		List<TitrationBean> measurements = new ArrayList<TitrationBean>();
		int i = 0;
		LocationBean bufferLocation = null;
		for(short plate: new short[]{1,2,3}) {
			for(char row: new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'}) {
				for (short column = 1; column < 8; column++) {
					LocationBean location = new LocationBean();
					location.setPlate(plate);
					location.setRow(row);
					location.setColumn(column);
					if (column == 1) {
						bufferLocation = location;
						continue;
					}
					i++;
					TitrationBean tibi = new TitrationBean();
					tibi.setLocation(location);
					tibi.setBufferLocation(bufferLocation);
					tibi.setConcentration(concentration);
					tibi.setExposureTemperature(exposureTemperature);
					tibi.setFrames(frames);
					tibi.setRecouperateLocation(null);
					tibi.setTimePerFrame(timePerFrame);
					tibi.setViscosity(viscosity);
					tibi.setYellowSample(yellowsample);
					tibi.setSampleName(String.format("Sample %d", i));
					measurements.add(tibi);
				}
			}
		}
		sessionBean.setMeasurements(measurements);
		
		return BSSCWizardUtils.sessionBeanToStream(sessionBean);
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "uk.ac.gda.devices.bssc", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}