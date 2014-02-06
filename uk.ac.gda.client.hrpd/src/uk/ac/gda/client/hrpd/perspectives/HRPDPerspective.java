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

package uk.ac.gda.client.hrpd.perspectives;

import gda.rcp.views.JythonTerminalView;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;
import org.python.pydev.ui.wizards.files.PythonModuleWizard;
import org.python.pydev.ui.wizards.files.PythonPackageWizard;
import org.python.pydev.ui.wizards.files.PythonSourceFolderWizard;
import org.python.pydev.ui.wizards.project.PythonProjectWizard;

import uk.ac.gda.client.XYPlotView;
import uk.ac.gda.client.liveplot.LivePlotView;
import uk.ac.gda.client.scripting.JythonPerspective;

public class HRPDPerspective implements IPerspectiveFactory {

	public static final String ID="uk.ac.gda.client.hrpd.perspective";
	
	private static final String TERMINAL_FOLDER = "terminalFolder";
	private static final String PROJ_FOLDER = "projFolder";
	private static final String Scan_PLOT_FOLDER = "scanPlotFolder";
	private static final String DETECTOR_PLOT_FOLDER = "detectorPlotFolder";
	//the following two ID recorded here as they use generic PartView Classes
	private static final String MAC_PLOT_VIEW_ID = "uk.ac.gda.client.hrpd.views.MACPlotView";
	private static final String GDA_NAVIGATOR_VIEW_ID = "uk.ac.gda.client.navigator";
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		defineLayout(layout);
		defineActions(layout);
	}

	private void defineActions(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout topLeft = layout.createFolder(PROJ_FOLDER, IPageLayout.LEFT, (float)0.17, editorArea); //$NON-NLS-1$
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        topLeft.addPlaceholder(GDA_NAVIGATOR_VIEW_ID);
        
        IFolderLayout scanPlotFolder=layout.createFolder(Scan_PLOT_FOLDER, IPageLayout.LEFT, (float)0.7, editorArea); //$NON-NLS-1$
        scanPlotFolder.addView(LivePlotView.ID);

        IFolderLayout detectorPlotFolder=layout.createFolder(DETECTOR_PLOT_FOLDER, IPageLayout.BOTTOM, (float)0.5, Scan_PLOT_FOLDER); //$NON-NLS-1$
        detectorPlotFolder.addView(MAC_PLOT_VIEW_ID);

        IFolderLayout terminalfolder= layout.createFolder(TERMINAL_FOLDER, IPageLayout.BOTTOM, (float)0.6, editorArea); //$NON-NLS-1$
        terminalfolder.addView(JythonTerminalView.ID);
        terminalfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        terminalfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
        terminalfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
        terminalfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
        terminalfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
        
        // add status here
        //IFolderLayout sideStatusFolder = layout.createFolder("statusFolder", IPageLayout.RIGHT, 0.5f, Scan_PLOT_FOLDER);

        layout.addPerspectiveShortcut(JythonPerspective.ID);
        
        layout.addShowViewShortcut(MAC_PLOT_VIEW_ID);
        layout.addShowViewShortcut(JythonTerminalView.ID);
        layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
	}

	private void defineLayout(IPageLayout layout) {
        layout.addNewWizardShortcut(PythonProjectWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonSourceFolderWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonPackageWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonModuleWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$

        layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
        layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(GDA_NAVIGATOR_VIEW_ID);
        layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
        layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
        layout.addShowViewShortcut("org.python.pydev.views.PyRefactorView");
        layout.addShowViewShortcut("org.python.pydev.views.PyCodeCoverageView");
        layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");
        
        layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);	
    }

}
