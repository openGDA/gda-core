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

package uk.ac.gda.common.rcp.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;

import uk.ac.gda.util.beans.xml.URLResolver;

/**
 * Eclipse utilities
 *
 */
public class EclipseUtils {

	/**
	 * Get the file path from a FileStoreEditorInput
	 * 
	 * @param fileInput
	 * @return URI or null
	 */
	public static URI getFileURI(IEditorInput fileInput) {
		if (fileInput instanceof IURIEditorInput) {
			URI uri = ((IURIEditorInput)fileInput).getURI();
			return uri;
		} 
		return null;
	}

	/**
	 * 
	 * @param fileInput
	 * @return File or null
	 */
	public static File getFile(IEditorInput fileInput) {
		URI uri = getFileURI(fileInput);
		return uri == null ? null : new File(uri);
	}

	/**
	 * 
	 * @param fileInput
	 * @return path or null
	 */
	public static String getFilePath(IEditorInput fileInput) {
		File f = getFile(fileInput);
		return f == null || !f.exists() ? null : f.getPath();
	}

	/**
	 * Try to determine the IFile from the edit input
	 * @param input
	 * @return file
	 */
	public static IFile getIFile(IEditorInput input) {
		if (input instanceof FileEditorInput) {
			return ((FileEditorInput)input).getFile();
		}
		return (IFile)input.getAdapter(IFile.class);
	}

	/**
	 * 
	 * @param input
	 * @return file name or null
	 */
	public static String getFileName(IEditorInput input) {
		File f = getFile(input);
		return f == null || !f.exists() ? null : f.getName();
	}

	/**
	 * @param bundleUrl 
	 * @return bundleUrl
	 */
	public static URL getAbsoluteUrl(final URL bundleUrl) {
		if (bundleUrl==null) return null;
		if (bundleUrl.toString().startsWith("bundle"))
			try {
				return FileLocator.resolve(bundleUrl);
			} catch (IOException e) {
				return bundleUrl;
	        } 
		return bundleUrl;
	}
	
	/**
	 * Gets the page, even during startup.
	 * @return the page
	 */
	public static IWorkbenchPage getPage() {
		IWorkbenchPage activePage = EclipseUtils.getActivePage();
		if (activePage!=null) return activePage;
		return EclipseUtils.getDefaultPage();
	}
	
	/**
	 * @return IWorkbenchPage
	 */
	public static IWorkbenchPage getActivePage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
		if (window==null) return null;
		return window.getActivePage();
	}
	
	/**
	 * @return IWorkbenchPage
	 */
	public static IEditorPart getActiveEditor() {
		final IWorkbenchPage page = EclipseUtils.getPage();
		return page.getActiveEditor();
	}

	
	/**
	 * @return IWorkbenchPage
	 */
	public static IWorkbenchPage getDefaultPage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow[] windows = bench.getWorkbenchWindows();
		if (windows==null) return null;
		
		return windows[0].getActivePage();
	}

	private static URLResolver resolver;
	/**
	 * Returns a URLResolver to transform bundle urls
	 * to absolute.
	 * 
	 * @return URLResolver
	 */
	public static URLResolver getUrlResolver() {
		if (resolver==null) resolver = new URLResolver() {
			@Override
			public URL resolve(URL url) {
				return EclipseUtils.getAbsoluteUrl(url);
			}
		};
		return resolver;
	}

	/**
	 * Declare a builder id in a project, this is then called to build it.
	 * @param project
	 * @param id
	 * @throws CoreException 
	 */
	public static void addBuilderToProject(IProject project, String id, IProgressMonitor monitor) throws CoreException {
		
		if (!project.isOpen()) return;
		
		IProjectDescription des = project.getDescription();
		
		ICommand[] cmds = des.getBuildSpec();
		for( ICommand cmd : cmds){
			if (cmd.getBuilderName().equals(id)) return;
		}
		
		ICommand com = des.newCommand();
		com.setBuilderName(id);
		List<ICommand> coms = new ArrayList<ICommand>(cmds.length+1);
		coms.addAll(Arrays.asList(cmds));
		coms.add(com);
		
		des.setBuildSpec(coms.toArray(new ICommand[0]));
		
		project.setDescription(des, monitor);
	}

	/**
	 * 
	 * @param project
	 * @param id
	 * @throws CoreException
	 */
	public static void removeBuilderFromProject(IProject project, String id, IProgressMonitor monitor) throws CoreException {
		
		if (!project.isOpen()) return;
		
		IProjectDescription des = project.getDescription();
		
		ICommand[] cmds = des.getBuildSpec();
		Vector<ICommand> newCmds = new Vector<ICommand>();
		for( ICommand cmd : cmds){
			if (!cmd.getBuilderName().equals(id))
				newCmds.add(cmd);
		}
		des.setBuildSpec(newCmds.toArray(new ICommand[0]));
		
		project.setDescription(des, monitor);
	}
	
	/**
	 * Checks of the id passed in == the current perspectives.
	 * @param id
	 * @return true if is
	 */
	public static boolean isActivePerspective(final String id) {
		
		final IWorkbenchPage page = getActivePage();
		if (page==null) return false;
		
		try {
			return id.equals(page.getPerspective().getId());
		} catch (Exception ignored) {
			return false;
		}
	}

	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	public static void delay(long waitTimeMillis) {
		delay(waitTimeMillis, false);
	}

	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 * @param returnInsteadOfSleep
	 *            Once there is nothing left to do return instead of sleep. In practice this means that async messages
	 *            should be complete before this method returns (unless it times out first)
	 */
	public static void delay(long waitTimeMillis, boolean returnInsteadOfSleep) {

		Display display = Display.getCurrent();

		// If this is the UI thread,
		// then process input.

		if (display != null) {
			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
			while (System.currentTimeMillis() < endTimeMillis) {
				try {
					if (!display.readAndDispatch()) {
						if (returnInsteadOfSleep)
							break;
						display.sleep();
					}
				} catch (Exception ne) {
					try {
						if (returnInsteadOfSleep)
							break;
						Thread.sleep(waitTimeMillis);
					} catch (InterruptedException e) {
						// Ignored
					}
					break;
				}
			}
			display.update();
		}
		// Otherwise, perform a simple sleep.

		else {
			try {
				if (!returnInsteadOfSleep)
					Thread.sleep(waitTimeMillis);
			} catch (InterruptedException e) {
				// Ignored.
			}
		}
	}
	
	/**
	 * Perform like a normal {@link Thread#join(long)} but process UI events while waiting for join
	 * 
	 * @param thread
	 *            Thread to "join" on
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	public static void threadJoin(Thread thread, long waitTimeMillis) {
		Display display = Display.getCurrent();

		// If this is the UI thread,
		// then process input.

		if (display != null) {
			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
			while (System.currentTimeMillis() < endTimeMillis && thread.isAlive()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			display.update();
		}
		// Otherwise, perform a simple join.

		else {
			try {
				thread.join(waitTimeMillis);
			} catch (InterruptedException e) {
				// Ignored.
			}
		}
	}

	/**
	 * Gets a unique file. The file must have a parent of IFolder.
	 * @param file
	 * @return new file, not created.
	 */
	public static IFile getUniqueFile(IFile file, final String extension) {

		return getUniqueFile(file, null, extension);
	}
	
	/**
	 * Gets a unique file. The file must have a parent of IFolder.
	 * @param file
	 * @return new file, not created.
	 */
	public static IFile getUniqueFile(IFile file, final String conjunctive, final String extension) {
		
		final String name = file.getName();
		final Matcher matcher = Pattern.compile("(.+)(\\d+)\\."+extension, Pattern.CASE_INSENSITIVE).matcher(name);
		int start   = 0;
		String frag = name.substring(0,name.lastIndexOf("."));
		if (matcher.matches()) {
			frag  = matcher.group(1);
			start = Integer.parseInt(matcher.group(2));
		}
		
		if (conjunctive!=null) {
			frag = frag+conjunctive;
		}
		
		// First try without a start position
		final IContainer parent = file.getParent();
		final IFile newFile;
		if (parent instanceof IFolder) {
			newFile = ((IFolder)parent).getFile(frag+"."+extension);
		} else if (parent instanceof IProject) {
			newFile = ((IProject)parent).getFile(frag+"."+extension);
		} else {
			newFile = null;
		}
		if (newFile!=null&&!newFile.exists()) return newFile;
		
		return getUniqueFile(parent, frag, ++start, extension);
	}
	
	
	public static IFile getUniqueFile(IContainer parent, String filename, final String extension){
		final Matcher matcher = Pattern.compile("(.+)(\\d+)\\."+extension, Pattern.CASE_INSENSITIVE).matcher(filename);
		int start   = 0;
		String frag = filename.substring(0,filename.lastIndexOf("."));
		if (matcher.matches()) {
			frag  = matcher.group(1);
			start = Integer.parseInt(matcher.group(2));
		}
		return getUniqueFile(parent, frag, start, extension);
	}

	private static IFile getUniqueFile(IContainer parent, String frag, int start, final String extension) {
		final IFile file;
		if (parent instanceof IFolder) {
			file = ((IFolder)parent).getFile(frag+start+"."+extension);
		} else if (parent instanceof IProject) {
			file = ((IProject)parent).getFile(frag+start+"."+extension);
		} else {
			throw new RuntimeException("The parent is neither a project nor a folder.");
		}
		if (!file.exists()) return file;
		return getUniqueFile(parent, frag, ++start, extension);
	}

	private static final Pattern UNIQUE_PATTERN = Pattern.compile("(.+)(\\d+)", Pattern.CASE_INSENSITIVE);

	public static String getUnique(IResource res) {
		final String name = res.getName();
		final Matcher matcher = UNIQUE_PATTERN.matcher(name);
		int start   = 0;
		String frag = name.indexOf(".")>-1
		            ? name.substring(0,name.lastIndexOf("."))
		            : name;
		if (matcher.matches()) {
			frag  = matcher.group(1);
			start = Integer.parseInt(matcher.group(2));
		}
		
		return getUnique(res.getParent(), frag, ++start);
	}
	
	private static String getUnique(IContainer parent, String frag, int start) {
		final IFile file;
		final IFolder folder;
		if (parent instanceof IFolder) {
			file = ((IFolder)parent).getFile(frag+start);
			folder = ((IFolder)parent).getFolder(frag+start);
		} else if (parent instanceof IProject) {
			file = ((IProject)parent).getFile(frag+start);
			folder = ((IProject)parent).getFolder(frag+start);
		} else {
			throw new RuntimeException("The parent is niether a project nor a folder.");
		}
		if (!file.exists()&&!folder.exists()) return file.getName();
		return getUnique(parent, frag, ++start);
	}

	
	// Source code and JavaDoc adapted from org.eclipse.ui.internal.util.Util.getAdapter
	/**
	 * If it is possible to adapt the given object to the given type, this
	 * returns the adapter. Performs the following checks:
	 * 
	 * <ol>
	 * <li>Returns <code>sourceObject</code> if it is an instance of the
	 * adapter type.</li>
	 * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
	 * <li>If sourceObject is not an instance of PlatformObject (which would have
	 * already done so), the adapter manager is queried for adapters</li>
	 * </ol>
	 * 
	 * Otherwise returns null.
	 * 
	 * @param sourceObject
	 *            object to adapt, or null
	 * @param adapterType
	 *            type to adapt to
	 * @return a representation of sourceObject that is assignable to the
	 *         adapter type, or null if no such representation exists
	 */
	public static Object getAdapter(Object sourceObject, Class<?> adapterType) {
		Assert.isNotNull(adapterType);
	    if (sourceObject == null) {
	        return null;
	    }
	    if (adapterType.isInstance(sourceObject)) {
	        return sourceObject;
	    }
	
	    return ResourceUtil.getAdapter(sourceObject, adapterType, true);
	}

	/**
	 * Opens an external editor on a file path
	 * @param file
	 * @throws PartInitException
	 */
	public static IEditorPart openEditor(IFile file) throws PartInitException {
		
		final IWorkbenchPage page = EclipseUtils.getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
        if (desc == null) desc =  PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName()+".txt");
		return page.openEditor(new FileEditorInput(file), desc.getId());
	}

	/**
	 * Opens an external editor on a file path
	 * @param filename
	 * @throws PartInitException
	 */
	public static IEditorPart openExternalEditor(String filename) throws PartInitException {
		
		final IWorkbenchPage page = EclipseUtils.getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename);
        if (desc == null) desc =  PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename+".txt");
		final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(filename));
        return page.openEditor(new FileStoreEditorInput(externalFile), desc.getId());
	}

	/**
	 * Returns the active project based on active selection
	 */
	public static IProject getActiveProject() {
		
		final IWorkbenchPage page = EclipseUtils.getActivePage();
		if (page==null) return null;
		
		final IEditorPart activeEditor = page.getActiveEditor();
		if (activeEditor!=null) {
			final IEditorInput input = activeEditor.getEditorInput();
			if (input instanceof FileEditorInput) {
				return ((FileEditorInput)input).getFile().getProject();
			}
		}
		
		final ISelectionService service = page.getWorkbenchWindow().getSelectionService();
		final ISelection        sel     = service.getSelection();
		if (!(sel instanceof IStructuredSelection)) return null;
		
		final IStructuredSelection ss = (IStructuredSelection) sel;
		final Object          element = ss.getFirstElement();
		if (element instanceof IResource) return ((IResource)element).getProject();
		
		if (!(element instanceof IAdaptable)) return null;
		IAdaptable adaptable = (IAdaptable)element;
		Object adapter = adaptable.getAdapter(IResource.class);
		return  ((IResource)adapter).getProject();
	}
	
	/**
	 * Activate the view @ID if it exists, nothing if it does not
	 */
	@SuppressWarnings("unused")
	public static void activateView(String ID){
		IViewReference[] viewReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences();
		boolean found = false;
		for (IViewReference view : viewReferences) {
			if (view.getId().equals(ID)) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.activate(view.getView(true).getViewSite().getPart());
				found = true;
				break;
			}

		}
	}

	
	/**
	 * Thread Safe. This is a hard busy setter - it overrides everything and sets everything busy.
	 * 
	 * Always use with try, finally. Only use when you are sure that there is no other alternative.
	 * For instance @see Job class.
	 * 
	 * @param isBusy
	 */
	public static void setBusy(final boolean isBusy) {
		
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display==null || display.isDisposed()) return;

		display.syncExec(new Runnable() {
			@Override
			public void run() {
				Cursor cursor  = display.getSystemCursor(SWT.CURSOR_WAIT);
				Shell[] shells = display.getShells();

				for (int i = 0; i < shells.length; i++) {
					if (isBusy) {
						shells[i].setCursor(cursor);
					} else {
						shells[i].setCursor(null);
					}
				}
			}
		});
	}
	
	public static IViewPart findView(String id) {
		IViewPart view = null;
		IWorkbench wb = PlatformUI.getWorkbench();
		for (IWorkbenchWindow win : wb.getWorkbenchWindows()) {
			for (IWorkbenchPage page : win.getPages()) {
				view = page.findView(id);
				if (view != null) {
					return view;
				}
			}
		}
		return null;
	}
}

	
