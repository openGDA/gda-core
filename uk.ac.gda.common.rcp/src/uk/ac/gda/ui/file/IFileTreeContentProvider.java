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

package uk.ac.gda.ui.file;

import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import uk.ac.gda.common.rcp.util.ISortingUtils;

/**
 * A class that provides a tree model relative to the system property gda.data
 */
public class IFileTreeContentProvider implements ITreeContentProvider {

	private IResource rootFile;

	public IFileTreeContentProvider(IResource r) {
		this.rootFile = r;
	}
	
	public void setPath(final IResource r) {
		this.rootFile = r;
	}
	
	public IResource getPath() {
		return rootFile;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		final IResource parFile = (IResource)parentElement;
		if (!parFile.exists()) return null;
		if (!(parFile instanceof IContainer)) return null;
		
		Collection<IResource> sortedFiles;
		try {
			sortedFiles = ISortingUtils.getSortedFileListIgnoreHidden((IContainer)parFile, ISortingUtils.NATURAL_COMPARATOR);
		} catch (CoreException e) {
			return null;
		}
		
		if (sortedFiles==null) return new Object[]{};
		return sortedFiles.toArray(new IResource[sortedFiles.size()]);
	}

	@Override
	public Object getParent(Object element) {
		if (element.equals(rootFile)) return null;
		return ((IResource)element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		final IResource parFile = (IResource)element;
		if (!(parFile instanceof IContainer)) return false;
		
		if (!parFile.exists())   return false;
		if (parFile.isHidden())  return false;
		if (parFile.isPhantom()) return false;
		if (parFile.getLocation().toFile().isHidden()) return false;
		if (!parFile.isAccessible()) return false;
		
		final IContainer container = (IContainer)parFile;
		try {
			final IResource[] members = container.members();
			return members!=null&&members.length>0;
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}