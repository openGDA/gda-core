/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client.scripting;

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

public class JythonPerspective implements IPerspectiveFactory {
	/**
	 * Do not change. Referenced in plugin.xml files and beam line .ini files which do not refactor.
	 */
	public static final String ID = "uk.ac.gda.client.scripting.JythonPerspective";
	public static final String GDA_NAVIGATOR_ID = "uk.ac.gda.client.navigator";

	@Override
	public void createInitialLayout(IPageLayout layout) {
        defineLayout(layout);
        defineActions(layout);
	}

	private void defineActions(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.2, editorArea); //$NON-NLS-1$
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        topLeft.addPlaceholder(GDA_NAVIGATOR_ID);

        IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.7, editorArea); //$NON-NLS-1$
        outputfolder.addView("gda.rcp.jythonterminalview");
        outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
        outputfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
        outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
        outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);

        layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float)0.65, editorArea);

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
        layout.addShowViewShortcut(GDA_NAVIGATOR_ID);
        layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
        layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
        layout.addShowViewShortcut("org.python.pydev.views.PyRefactorView");
        layout.addShowViewShortcut("org.python.pydev.views.PyCodeCoverageView");
        layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");

        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
    }
}