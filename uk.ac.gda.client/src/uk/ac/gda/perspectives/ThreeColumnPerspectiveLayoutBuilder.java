/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.perspectives;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supports {@link IPerspectiveFactory#createInitialLayout(IPageLayout)} in the creation of a generic "three
 * columns" layout. Using the left column as example, there are two main methods to add views:
 * <ol>
 * <li>{@link #addViewToLeftFolder(String, boolean)} - simply adds a view to the left column</li>
 * <li>{@link #addFolderThenViewToLeftFolder(String, boolean, float)} - creates a new folder below the left column, with the specified
 * ratio, then adds the view to the folder. Any following call of {@link #addViewToLeftFolder(String, boolean)} will add views to
 * this folder.</li>
 * </ol>
 *
 * Same for the central and right columns.
 *
 * @author Maurizio Nagni
 */
public class ThreeColumnPerspectiveLayoutBuilder {

	private final String perspectiveName;
	private final IPageLayout pageLayout;
	private final Folder mainFolder;

	private Folder leftFolder;
	private Folder centralFolder;
	private Folder rightFolder;

	private float leftFolderRatio;
	private float centralFolderRatio;
	private float rightFolderRatio;

	public static final float DEFAULT_LEFT_RATIO = 0.2f;
	public static final float DEFAULT_CENTRE_RATIO = 0.65f;
	public static final float DEFAULT_RIGHT_RATIO = 0.25f;

	private static final Logger logger = LoggerFactory.getLogger(ThreeColumnPerspectiveLayoutBuilder.class);

	/**
	 * Instantiate a helper using the default columns ratio ({@link #DEFAULT_LEFT_RATIO}, {@link #DEFAULT_CENTRE_RATIO}, {@link #DEFAULT_RIGHT_RATIO});
	 *
	 * @param perspectiveName
	 *            the perspective using this instance. Only for logging reason.
	 * @param pageLayout
	 *            the perspective root page
	 */
	public ThreeColumnPerspectiveLayoutBuilder(String perspectiveName, IPageLayout pageLayout) {
		this(perspectiveName, pageLayout, DEFAULT_LEFT_RATIO, DEFAULT_CENTRE_RATIO, DEFAULT_RIGHT_RATIO);
	}

	/**
	 * Splits, horizontally the page in three columns according to the specified ratios. A ratio specifies how to divide
	 * the space currently occupied by the reference part, in the range 0.05f to 0.95f. Values outside this range will
	 * be clipped to facilitate direct manipulation. The part at left gets the specified ratio of the current space and
	 * the part at right gets the rest.
	 *
	 * @param perspectiveName
	 *            the perspective using this instance. Only for logging reason.
	 * @param pageLayout
	 *            the perspective root page
	 * @param leftFolderRatio
	 *            the ratio for the left column
	 * @param centralFolderRatio
	 *            the ratio for the central column
	 * @param rightFolderRatio
	 *            the ratio for the right column
	 */
	public ThreeColumnPerspectiveLayoutBuilder(String perspectiveName, IPageLayout pageLayout, float leftFolderRatio,
			float centralFolderRatio, float rightFolderRatio) {
		this.pageLayout = pageLayout;
		this.leftFolderRatio = leftFolderRatio;
		this.centralFolderRatio = centralFolderRatio;
		this.rightFolderRatio = rightFolderRatio;
		this.perspectiveName = perspectiveName;
		mainFolder = new Folder(pageLayout);
		initDefaultLayout();
		logger.debug("Created {}", this);
	}

	/**
	 * Adds a view to the left folder.
	 *
	 * @param viewId
	 *            the view ID to add
	 * @param closeable set the view as closable
	 */
	public void addViewToLeftFolder(String viewId, boolean closeable) {
		leftFolder.addView(viewId, closeable);
	}

	/**
	 * Creates a new folder below the left column, with the specified ratio, then adds the view to the folder. Any
	 * following call of {@link #addViewToRightFolder(String, boolean)} will add views to this new folder.
	 *
	 * @param viewId
	 *            the view ID to add
	 * @param closeable set the view as closable
	 * @param ratio
	 *            the ratio how divide the vertical space
	 */
	public void addFolderThenViewToLeftFolder(String viewId, boolean closeable, float ratio) {
		leftFolder = newFolder(IPageLayout.BOTTOM, ratio, leftFolder);
		addViewToLeftFolder(viewId, closeable);
	}

	/**
	 * Adds a view to the central folder.
	 *
	 * @param viewId
	 *            the view ID to add
	 * @param closeable set the view as closable
	 */
	public void addViewToCentralFolder(String viewId, boolean closeable) {
		centralFolder.addView(viewId, closeable);
	}

	/**
	 * Creates a new folder below the central column, with the specified ratio, then adds the view to the folder. Any
	 * following call of {@link #addViewToRightFolder(String, boolean)} will add views to this new folder.
	 *
	 * @param viewId
	 *            the view ID to add
	 * @param closeable set the view as closable
	 * @param ratio
	 *            the ratio how divide the vertical space
	 */
	public void addFolderThenViewToCentralFolder(String viewId, boolean closeable, float ratio) {
		centralFolder = newFolder(IPageLayout.BOTTOM, ratio, centralFolder);
		addViewToCentralFolder(viewId, closeable);
	}

	/**
	 * Adds a view to the right area.
	 *
	 * @param viewId
	 *            the view ID to add
	 * @param closeable set the view as closable
	 */
	public void addViewToRightFolder(String viewId, boolean closeable) {
		rightFolder.addView(viewId, closeable);
	}

	/**
	 * Creates a new folder below the right column, with the specified ratio, then adds the view to the folder. Any
	 * following call of {@link #addViewToRightFolder(String, boolean)} will add views to this new folder.
	 *
	 * @param viewId
	 *            the view ID to add
	 * @param closeable set the view as closable
	 * @param ratio
	 *            the ratio how divide the vertical space
	 */
	public void addFolderThenViewToRightFolder(String viewId, boolean closeable, float ratio) {
		rightFolder = newFolder(IPageLayout.BOTTOM, ratio, rightFolder);
		addViewToRightFolder(viewId, closeable);
	}

	@Override
	public String toString() {
		return "PerspectiveHelper [perspectiveName=" + perspectiveName + ", leftFolderRatio=" + leftFolderRatio
				+ ", centralFolderRatio=" + centralFolderRatio + ", rightFolderRatio=" + rightFolderRatio + "]";
	}

	private void initDefaultLayout() {
		pageLayout.setEditorAreaVisible(false);
		leftFolder = newFolder(IPageLayout.LEFT, leftFolderRatio, mainFolder);
		centralFolder = newFolder(IPageLayout.LEFT, centralFolderRatio, mainFolder);
		rightFolder = newFolder(IPageLayout.LEFT, rightFolderRatio, mainFolder);
	}

	private Folder newFolder(int relationship, float ratio, Folder area) {
		String folderId = UUID.randomUUID().toString();
		IFolderLayout newFolder = pageLayout.createFolder(folderId, relationship, ratio, area.getRefId());
		return new Folder(folderId, newFolder, pageLayout);
	}

	/**
	 * Wraps essential folder elements.
	 */
	private class Folder {
		private final String refId;
		private final IFolderLayout folderLayout;
		private final IPageLayout pageLayout;

		public Folder(IPageLayout pageLayout) {
			this(null, null, pageLayout);
		}

		public Folder(String refId, IFolderLayout folderLayout, IPageLayout pageLayout) {
			this.refId = refId;
			this.folderLayout = folderLayout;
			this.pageLayout = pageLayout;
		}

		public String getRefId() {
			return Optional.ofNullable(refId).orElse(getPageLayout().getEditorArea());
		}

		public IFolderLayout getFolderLayout() {
			return folderLayout;
		}

		public IPageLayout getPageLayout() {
			return pageLayout;
		}

		public void addView(String viewId, boolean closeable) {
			getFolderLayout().addView(viewId);
			getPageLayout().getViewLayout(viewId).setCloseable(closeable);
		}
	}
}
