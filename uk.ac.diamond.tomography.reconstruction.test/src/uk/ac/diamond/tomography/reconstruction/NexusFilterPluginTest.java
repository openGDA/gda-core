/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf.object.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf5.model.internal.HierarchicalDataFileModel;
import org.eclipse.dawnsci.hdf5.model.internal.IHierarchicalDataFileGetReader;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.navigator.CommonViewer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor.Operation;
import uk.ac.diamond.tomography.reconstruction.views.NexusFilterDescriptor;
import uk.ac.diamond.tomography.reconstruction.views.NexusNavigator;

@SuppressWarnings("restriction")
public class NexusFilterPluginTest {
	private static NexusNavigator navigatorView;
	private static IProject project;
	private static IWorkspaceRoot root;

	@BeforeClass
	public static void beforeClass() throws Exception {
		IIntroPart part = PlatformUI.getWorkbench().getIntroManager().getIntro();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(part);
		navigatorView = (NexusNavigator) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(NexusNavigator.ID);
		assertNotNull(navigatorView);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		project = root.getProject("NexusFilterPluginTestProject");
		// at this point, no resources have been created
		assertTrue(!project.exists());
		project.create(null);
		project.open(null);

		createNexusFile(project.getFile("kichwa1_1.nxs"), "kichwa1", "1");
		createNexusFile(project.getFile("kichwa1_2.nxs"), "kichwa1", "2");
		IFile file = project.getFile("kichwa2_1.nxs");
		createNexusFile(file, "kichwa2", "1");

		root.refreshLocal(IResource.DEPTH_INFINITE, null);
		navigatorView.getCommonViewer().expandToLevel(file, AbstractTreeViewer.ALL_LEVELS);
	}

	private void setFilter(String nexusFilterPath, INexusFilterDescriptor.Operation nexusFilterOperation,
			String[] nexusFilterOperands) {
		INexusFilterDescriptor descriptor = new NexusFilterDescriptor(nexusFilterPath, nexusFilterOperation,
				nexusFilterOperands);
		navigatorView.getFilterPathProvider().setFilterDescriptor(descriptor);
	}

	private void setFilterOff() {
		navigatorView.getFilterPathProvider().setFilterDescriptor(null);
	}

	@Before
	public void before() {
		setFilterOff();
	}

	@AfterClass
	public static void afterClass() throws CoreException {
		project.delete(true, true, null);
		root.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	private static void createNexusFile(IFile file, String name, String value) throws Exception {
		final String absolutePath = file.getRawLocation().toOSString();
		// Create a file and verify it
		IHierarchicalDataFile writer = HierarchicalDataFactory.getWriter(absolutePath);
		writer.createStringDataset(name, value, writer.getRoot());
		writer.close();

		HierarchicalDataFileModel model = new HierarchicalDataFileModel(new IHierarchicalDataFileGetReader() {

			@Override
			public IHierarchicalDataFile getReader() throws Exception {
				return HierarchicalDataFactory.getReader(absolutePath);
			}
		});

		assertEquals(value, model.getPath("/" + name));
	}

	/**
	 * Call this to stop at this point and be able to interact with the UI
	 */
	public void readAndDispatchForever() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	@Test
	public void test_Off() {
		assertDisplayedContent("kichwa1_1.nxs", "kichwa1_2.nxs", "kichwa2_1.nxs");
	}

	@Test
	public void test_CONTAINS() {
		setFilter("/kichwa1", Operation.CONTAINS, null);
		assertDisplayedContent("kichwa1_1.nxs", "kichwa1_2.nxs");

		setFilter("/kichwa2", Operation.CONTAINS, null);
		assertDisplayedContent("kichwa2_1.nxs");

		setFilter("/not_in_any_file", Operation.CONTAINS, null);
		assertDisplayedContent();
	}

	@Test
	public void test_DOES_NOT_CONTAIN() {
		setFilter("/kichwa1", Operation.DOES_NOT_CONTAIN, null);
		assertDisplayedContent("kichwa2_1.nxs");

		setFilter("/kichwa2", Operation.DOES_NOT_CONTAIN, null);
		assertDisplayedContent("kichwa1_1.nxs", "kichwa1_2.nxs");

		setFilter("/not_in_any_file", Operation.DOES_NOT_CONTAIN, null);
		assertDisplayedContent("kichwa1_1.nxs", "kichwa1_2.nxs", "kichwa2_1.nxs");
	}

	private void assertDisplayedContent(String... expected) {
		String[] displayedContent = getDisplayedContent();
		assertEquals(expected.length, displayedContent.length);
		for (int i = 0; i < expected.length; i++) {
			// we check starts with because the tree item may have been decorated
			assertTrue(displayedContent[i].startsWith(expected[i]));
		}
	}

	private String[] getDisplayedContent() {
		CommonViewer viewer = navigatorView.getCommonViewer();
		Tree tree = (Tree) viewer.getControl();
		for (TreeItem treeItem : tree.getItems()) {
			if ("NexusFilterPluginTestProject".equals(treeItem.getText())) {
				TreeItem[] items = treeItem.getItems();
				String[] itemTexts = new String[items.length];
				for (int i = 0; i < itemTexts.length; i++) {
					itemTexts[i] = items[i].getText();
				}
				return itemTexts;
			}
		}
		return null;
	}
}
