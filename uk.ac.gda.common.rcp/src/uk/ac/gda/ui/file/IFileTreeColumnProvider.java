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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import uk.ac.gda.common.rcp.util.IFileUtils;
import uk.ac.gda.util.io.FileUtils;

/**
 * A class to deal with tooltips on the TreeColumns
 */
public class IFileTreeColumnProvider extends CellLabelProvider {

	private SimpleDateFormat dateFormat;

	public IFileTreeColumnProvider() {
		this.dateFormat  = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss z");
	}

	@Override
	public void update(ViewerCell cell) {
		
	}
	
	@Override
	public String getToolTipText(Object element) {

		final String path = getFilePath(element);
		if (path == null) return null;
		
		final StringBuilder buf = new StringBuilder();
		buf.append(path);
		buf.append('\n');
		try {
			buf.append(FileUtils.formatSize(getLength(element), 3));
			buf.append('\n');
			buf.append(dateFormat.format(new Date(getLastModified(element))));
		} catch (Exception ne) {
			// Ignored
		}
			
		return buf.toString();
	}

	private long getLength(Object element) throws Exception {
		if (element==null)     return 0;
		if (element instanceof File) {
			final File file = (File)element;
			if (!file.exists()) return 0;
			return file.length();
			
		} else if (element instanceof IResource) {
			IFileUtils.getLength((IResource)element);
		}
		return 0;
	}

	private long getLastModified(Object element) throws Exception {
		if (element==null)     return 0;
		if (element instanceof File) {
			final File file = (File)element;
			if (!file.exists()) return 0;
			return file.length();
			
		} else if (element instanceof IResource) {
			IFileUtils.getLastModified((IResource)element);
		}
		return 0;
	}

	
	private String getFilePath(Object element) {
		
		if (element==null)     return null;
		if (element instanceof File) {
			final File file = (File)element;
			if (!file.exists()) return null;
			return file.getAbsolutePath();
			
		} else if (element instanceof IResource) {
			final IResource file = (IResource)element;
			if (!file.exists()) return null;
			return file.getLocation().toOSString();
		}
		return null;
	}

}
