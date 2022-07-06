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

package uk.ac.gda.ui.content;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

/**
 * File proposals. You must set the proposal adapter to replace if using this:
 * 
 * <code>
 * <p>
 		FileContentProposalProvider prov = new FileContentProposalProvider();<br>
 		ContentProposalAdapter ad = new ContentProposalAdapter(filePath, new TextContentAdapter(), prov, null, null);<br>
		ad.setProposalAcceptanceStyle(<b>ContentProposalAdapter.PROPOSAL_REPLACE</b>);<br>
   </p>
   </code>
 * 
 * @author Matthew Gerring
 *
 */
public class FileContentProposalProvider implements IContentProposalProvider {

	private class FilenameChecker implements FileFilter {
		@Override
		public boolean accept(File file) {
			if (file == null)       return true;
			if (file.isDirectory()) return true;
			if (filterExtensionProv==null) return true;
			final String [] filters = filterExtensionProv.getFilterExtensions();
			if (filters==null) return true;
			for (int i = 0; i < filters.length; i++) {
				String filter = filters[i].replace(".", "\\.");
				filter = filters[i].replace("*", ".*");
				if (file.getName().matches(filter)) return true;
			}
			return false;
		}
	}

	private IFilterExtensionProvider filterExtensionProv;
	private IContentProposal[] currentProposals;
	public int getSize() {
		return currentProposals!=null ? currentProposals.length : -1;
	}
	
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		final File dir = getDirectory(contents, position);
		if (dir == null) return new IContentProposal[0];
		IContentProposal[] ret = getProposals(contents, dir);
		if (ret == null) return new IContentProposal[0];
		return ret;
	}
	
	public String getFirstPath() {
		return currentProposals!=null ? currentProposals[0].getContent() : null;
	}

    private IContentProposal[] getProposals(String existing, File dir) {
		final File      [] fa  = dir.listFiles(new FilenameChecker());
		if (fa == null) return null;
		List<IContentProposal> ret = new ArrayList<IContentProposal>(fa.length);
		//ret.add(new FileContentProposal(dir));
		for (int i = 0; i < fa.length; i++) {
			final File file = fa[i];
			try {
				final String path  = file.getCanonicalPath();
				final int    index = path.indexOf(existing);
				if (index!=0) continue;
			} catch (IOException e) {
				continue;
			}
			ret.add(new FileContentProposal(file));
		}
		if (ret.isEmpty()) return null;
		this.currentProposals = ret.toArray(new IContentProposal[ret.size()]);
		return currentProposals;
	}

	private File getDirectory(String contents, int position) {
		if (contents==null) return null;
		if (position>-1) contents = contents.substring(0,position);
		int index = contents.lastIndexOf('/');
		if (index<0) index = contents.lastIndexOf('\\');
		if (index>-1)contents = contents.substring(0,index);
		return new File(contents);
	}

	
    class FileContentProposal implements IContentProposal {

		private final File   file;
		FileContentProposal(final File file) {
			this.file     = file;
		}

		@Override
		public String getContent() {
			return getLabel();
		}

		@Override
		public int getCursorPosition() {
			return getLabel().length();
		}

		@Override
		public String getDescription() {
			//return FileUtils.getSystemInfo(file);
			return null;
		}

		@Override
		public String getLabel() {
			return file.getAbsolutePath();
			
		}

	}

	/**
	 * @return Returns the filterExtensionProv.
	 */
	public IFilterExtensionProvider getFilterExtensionProv() {
		return filterExtensionProv;
	}

	/**
	 * @param filterExtensionProv The filterExtensionProv to set.
	 */
	public void setFilterExtensionProv(IFilterExtensionProvider filterExtensionProv) {
		this.filterExtensionProv = filterExtensionProv;
	}


}

	