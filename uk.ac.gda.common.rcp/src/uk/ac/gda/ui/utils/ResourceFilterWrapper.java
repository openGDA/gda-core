/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.utils;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IResourceFilterDescription;

/**
 * Class used to hold resource filter definitions See IContainer.createFilter for details of arguments
 */
public class ResourceFilterWrapper {
	public int type;
	public FileInfoMatcherDescription fileInfoMatcherDescription;

	public ResourceFilterWrapper(int type, FileInfoMatcherDescription fileInfoMatcherDescription) {
		super();
		this.type = type;
		this.fileInfoMatcherDescription = fileInfoMatcherDescription;
	}

	public static ResourceFilterWrapper createImportProjectAndFolderFilter(int type,
			FileInfoMatcherDescription fileInfoMatcherDescription) {
		return new ResourceFilterWrapper(type, fileInfoMatcherDescription);
	}

	public static ResourceFilterWrapper createRegexResourceFilterWrapper(int type, String argument) {
		return createImportProjectAndFolderFilter(type, new FileInfoMatcherDescription(
				"org.eclipse.core.resources.regexFilterMatcher", argument));
	}

	public static ResourceFilterWrapper createRegexFolderFilter(String argument, boolean folders, boolean exclude) {
		return createRegexResourceFilterWrapper((exclude ? IResourceFilterDescription.EXCLUDE_ALL
				: IResourceFilterDescription.INCLUDE_ONLY)
				| (folders ? IResourceFilterDescription.FOLDERS : IResourceFilterDescription.FILES)
				| IResourceFilterDescription.INHERITABLE, argument);
	}

}