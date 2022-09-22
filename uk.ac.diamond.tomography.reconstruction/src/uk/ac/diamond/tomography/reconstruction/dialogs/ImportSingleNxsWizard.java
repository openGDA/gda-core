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

package uk.ac.diamond.tomography.reconstruction.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.navigator.CommonNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.views.NexusNavigator;

public class ImportSingleNxsWizard extends Wizard implements IImportWizard {

	private static final String WINDOW_TITLE = "Import NeXus file";

	public class ImportSingleNxsWizardPage extends WizardNewFileCreationPage implements IWizardPage {

		private static final String TXT_BOX_LBL = "Full file location";
		private Composite rootComposite;

		protected ImportSingleNxsWizardPage(IStructuredSelection selection) {
			super("NewNxsFile", selection);
			setTitle(WINDOW_TITLE);
			setDescription("Please select a file to copy into the product.");
		}

		private boolean isValid = false;

		private String fileLocation;

		public String getFileLocation() {
			return fileLocation;
		}

		@Override
		public void createControl(Composite parent) {
			initializeDialogUnits(parent);
			rootComposite = new Composite(parent, SWT.None);
			rootComposite.setLayout(new GridLayout(3, false));

			Label lblFileName = new Label(rootComposite, SWT.None);
			lblFileName.setText(TXT_BOX_LBL);
			lblFileName.setLayoutData(new GridData());

			final Text txtFullFileLocation = new Text(rootComposite, SWT.BORDER);
			txtFullFileLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			txtFullFileLocation.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					// IFile newFileHandle = createFileHandle(new Path(txtFullFileLocation.getText()));
					String fileLocationTxt = txtFullFileLocation.getText();
					File file = new File(fileLocationTxt);

					if (!file.exists()) {
						isValid = false;
					} else {
						if (!file.isDirectory()) {
							isValid = true;
						} else {
							isValid = false;
						}
					}

					if (isValid) {
						fileLocation = fileLocationTxt;
					} else {
						fileLocation = null;
					}

					updatePageStatus();

				}

			});

			Button btnBrowse = new Button(rootComposite, SWT.None);
			btnBrowse.setText("Browse...");
			btnBrowse.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
					dialog.setFileName(txtFullFileLocation.getText());
					String[] filterExtensions = new String[] { "*.nxs" };
					dialog.setFilterExtensions(filterExtensions);
					fileLocation = dialog.open();
					txtFullFileLocation.setText(fileLocation);

					logger.debug("ImportSingleNxsWizard:Full file path :{}", fileLocation);
				}
			});
			setControl(rootComposite);
		}

		@Override
		protected boolean validatePage() {
			return isValid;
		}

		private void updatePageStatus() {
			setPageComplete(validatePage());
		}

		@Override
		public void setVisible(boolean visible) {
			getControl().setVisible(visible);
		}

		@Override
		public Control getControl() {
			return rootComposite;
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(ImportSingleNxsWizard.class);
	private IStructuredSelection selection;
	private ImportSingleNxsWizardPage wizPage;

	public ImportSingleNxsWizard() {
		setWindowTitle(WINDOW_TITLE);
	}

	@Override
	public void addPages() {
		wizPage = new ImportSingleNxsWizardPage(selection);
		addPage(wizPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	@Override
	public boolean performFinish() {

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final IProject tomoSettingsProject = Activator.getDefault().getTomoFilesProject();
					final String fileLocation = wizPage.getFileLocation();
					Path fileLocPath = new Path(fileLocation);
					String fileNameOnly = fileLocPath.lastSegment();

					final IFile nxsFile = tomoSettingsProject.getFile(fileNameOnly);
					if (!nxsFile.exists()) {
						try {
							new WorkspaceModifyOperation() {

								@Override
								protected void execute(IProgressMonitor monitor) throws CoreException,
										InvocationTargetException, InterruptedException {
									try {
										nxsFile.createLink(new Path(fileLocation), IResource.REPLACE, monitor);
									} catch (IllegalArgumentException ex) {
										logger.debug("Problem identified - eclipse doesn't refresh the right folder");
									}
								}
							}.run(monitor);

						} catch (InvocationTargetException e) {
							logger.error("Problem creating links", e);
						} catch (InterruptedException e) {
							logger.error("Problem creating links - interrupted.", e);
						}
					}

				}
			});
		} catch (InvocationTargetException e) {
			logger.error("Problem executing import nexus wizard", e);
		} catch (InterruptedException e) {
			logger.error("Problem executing import nexus wizard - Interrupted", e);
		}

		refreshNexusNavigatorIfOpened();

		return true;
	}

	private void refreshNexusNavigatorIfOpened() {
		IViewPart nexusNavigatorView = null;

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference[] viewReferences = activePage.getViewReferences();

		for (IViewReference iViewReference : viewReferences) {
			if (NexusNavigator.ID.equals(iViewReference.getId())) {
				try {
					nexusNavigatorView = activePage.showView(NexusNavigator.ID);
				} catch (PartInitException e) {
					logger.error("Unable to show Refresh view", e);
				}
				if (nexusNavigatorView instanceof CommonNavigator) {
					CommonNavigator cn = (CommonNavigator) nexusNavigatorView;
					cn.getCommonViewer().refresh();
				}
				break;
			}
		}
	}

}
