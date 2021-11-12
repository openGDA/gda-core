/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.opengda.detector.electronanalyser.client.views.RegionCreatorView;
import org.opengda.detector.electronanalyser.client.views.SequenceCreatorView;

public class SESCreatorPerspective implements IPerspectiveFactory {

	public static final String ID = "org.opengda.detector.electronanalyser.client.ses.creator.perspective";

	private static final String SEQUENCE_CREATOR_FOLDER = "sequenceCreatorFolder";
	private static final String REGION_CREATOR_FOLDER = "regionCreatorFolder";

	private static final String SEQUENCE_CREATOR = SequenceCreatorView.ID;
	private static final String REGION_CREATOR = RegionCreatorView.ID;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		defineLayout(layout);
	}

	private void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout regionFolder = layout.createFolder(REGION_CREATOR_FOLDER, IPageLayout.LEFT, 0.30f, editorArea);
		regionFolder.addView(REGION_CREATOR);

		IFolderLayout sequenceFolder = layout.createFolder(SEQUENCE_CREATOR_FOLDER, IPageLayout.LEFT, 0.70f, editorArea);
		sequenceFolder.addView(SEQUENCE_CREATOR);

	}

}
