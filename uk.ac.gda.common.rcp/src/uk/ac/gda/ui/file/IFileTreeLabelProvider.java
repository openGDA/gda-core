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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.gda.common.rcp.util.IFileUtils;
import uk.ac.gda.util.io.FileUtils;

import com.swtdesigner.SWTResourceManager;

/**
 *
 */
public class IFileTreeLabelProvider extends LabelProvider implements ITableLabelProvider {

	
	private boolean isDisposed = false;
	private final Image folderImage,fileImage,nexusImage,asciiImage;
	private final DateFormat dateFormat;
	private final int[] flags;

	/**
	 * The first column is always name.
	 * Short hand for IFileTreeLabelProvider(SIZE,DATE)
	 */
	public IFileTreeLabelProvider() {
		this(IFileConst.SIZE,IFileConst.DATE);
	}

	/**
	 * The first column is always name.
	 * Subsequent columns can be defined using IFileConst flags
	 *                
	 * Set from SIZE, PATH, DATE.
	 * @param flags
	 */
	public IFileTreeLabelProvider(int... flags) {
		
		this.flags       = flags;
		this.folderImage = SWTResourceManager.getImage(IFileTreeLabelProvider.class, "/icons/folder.png");
		this.fileImage   = SWTResourceManager.getImage(IFileTreeLabelProvider.class, "/icons/page.png");
		this.nexusImage  = SWTResourceManager.getImage(IFileTreeLabelProvider.class, "/icons/nexus.png");
		this.asciiImage  = SWTResourceManager.getImage(IFileTreeLabelProvider.class, "/icons/page_white_database.png");
		this.dateFormat  = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss z");

	}
	
	private Image getImageFromFile(IResource file) {

		if (isDisposed()) return null;
		if (!file.exists()) return null;
		if (file instanceof IContainer) {
			return folderImage;
		}
		
		if (file.getName().toLowerCase().endsWith(".nxs")) {
			return nexusImage;
		}
		
		if (file.getName().toLowerCase().endsWith(".dat")) {
			return asciiImage;
		}
		
		return fileImage;
	}

	private String getTextFromFile(IResource element) {
		return element.getName();
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex==0) {
			return getImageFromFile((IResource)element);
		} 
		
		final int flag = flags[columnIndex-1];
		if (flag == IFileConst.PATH) {
			return folderImage;
		}
		
		return null;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		
		final IResource file = (IResource)element;
		if (!file.exists()) return null;

		if (columnIndex==0) {
			return getTextFromFile(file);
		} 
		
		final int flag = flags[columnIndex-1];
		if (flag == IFileConst.DATE) {
			return getDate(element);
		} else if (flag == IFileConst.SIZE) {
			return getSize(element);
		} else if (flag == IFileConst.PATH) {
			return getPath(element);
		}

		return null;
	}
	
	public String getSize(final Object element) {
		try {
			return FileUtils.formatSize(IFileUtils.getLength((IResource)element), 3);
		} catch (Exception e) {
			return "-";
		}
	}
	public String getDate(final Object element) {
		try {
			return dateFormat.format(new Date(IFileUtils.getLastModified((IResource)element)));
		} catch (Exception e) {
			return "-";
		}
	}
	
	public String getPath(final Object element) {
		final IResource res = (IResource)element;
		return res.getParent().getFullPath().toOSString();
	}

	
	@Override
	public void dispose() {
		isDisposed = true;
		super.dispose();
//		if (folderImage!=null&&!folderImage.isDisposed()) folderImage.dispose();
//		if (fileImage!=null&&!fileImage.isDisposed())     fileImage.dispose();
//		if (nexusImage!=null&&!nexusImage.isDisposed())   nexusImage.dispose();
//		if (asciiImage!=null&&!asciiImage.isDisposed())   asciiImage.dispose();
	}


	/**
	 * @return Returns the isDisposed.
	 */
	public boolean isDisposed() {
		return isDisposed;
	}


}
