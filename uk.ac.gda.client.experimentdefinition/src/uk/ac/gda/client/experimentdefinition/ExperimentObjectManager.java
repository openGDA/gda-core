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

package uk.ac.gda.client.experimentdefinition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.components.ExperimentObjectListener;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.common.rcp.util.EclipseUtils;

/**
 * Organises the list of scans in a multiscan and persists the information in a .scan file.
 * <p>
 * This does not run the scans.
 * <p>
 * This needs to be subclassed by concrete classes to provide the implementation for creating new scans.
 * <p>
 * The concrete class in use should be referenced in a contribution to the uk.ac.gda.client.experimentdefinition
 * extension point
 */
public abstract class ExperimentObjectManager implements IExperimentObjectManager {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentObject.class);
	private static final Pattern UNIQUE_PATTERN = Pattern.compile("(.+)(\\d+)", Pattern.CASE_INSENSITIVE);

	protected transient IFile file;  // the .scan file to persist the list of xml files which make up the scan
	protected List<IExperimentObject> lines;
	protected transient Collection<ExperimentObjectListener> listeners;

	/*
	 * Given a space-delimited string read from the.scan file, this creates a new experiment object which uses the files
	 * whose names make up the given String
	 */
	protected abstract IExperimentObject createNewExperimentObject(String line);

	/**
	 * Instantiates a new IExperimentObject of the type referenced in the uk.ac.gda.client.experimentdefinition
	 * extension point. You would need to call IExperimentObject.createFilesFromTemplates() to create the xml files
	 * which the instance references.
	 *
	 * @return IExperimentObject
	 */
	protected IExperimentObject createNewExperimentObject() {

		try {
			Class<IExperimentObject> exptClass = getExperimentObjectType();
			Constructor<IExperimentObject> cons = exptClass.getConstructor();
			IExperimentObject newObject = cons.newInstance();
			return newObject;
		} catch (Exception e) {
			logger.error("Error creating an object of type " + getExperimentObjectType() + ". The class must have a null constructor");
			return null;
		}
	}

	@Override
	public IExperimentObject addExperiment(IExperimentObject obj) {
		obj.setMultiScanName(this.getName());
		obj.setFolder(getContainingFolder());
		lines.add(obj);
		return obj;
	}

	@Override
	public void insertExperimentAfter(IExperimentObject previous, IExperimentObject toAdd) throws Exception {

		if (lines.contains(toAdd)) {
			lines.remove(toAdd);
		}

		final int index = previous != null ? lines.indexOf(previous) + 1 : 0;

		lines.add(index, toAdd);
		write();
	}

	@Override
	public void addExperimentObjectListener(final ExperimentObjectListener l) {
		if (listeners == null)
			listeners = new HashSet<ExperimentObjectListener>(7);
		listeners.add(l);
	}

//	/**
//	 * @return true if some of the files referenced do not exist.
//	 */
//	@Override
//	public boolean checkError() {
//		boolean error = false;
//		final List<IExperimentObject> runs = getExperimentList();
//		for (IExperimentObject ob : runs) { // Intentionally loop all
//			if (ob.isError())
//				error = true;
//		}
//		return error;
//	}

	@Override
	public IExperimentObject createNewExperiment(String scanName) {
		if (scanName == null)
			scanName = "Scan_1";
		final IExperimentObject ob = createNewExperimentObject();
		ob.setMultiScanName(this.getName());
		ob.setFolder(getContainingFolder());
		ob.createFilesFromTemplates(new XMLCommandHandler());
		ob.setRunName(scanName);
		lines.add(ob);
		write();
		return ob;
	}

	@Override
	public IExperimentObject insertNewExperimentAfter(IExperimentObject previous) {
		final int index = previous != null ? lines.indexOf(previous) + 1 : 0;
		final IExperimentObject ob = createNewExperimentObject();
		ob.setMultiScanName(this.getName());
		ob.setFolder(getContainingFolder());
		ob.createFilesFromTemplates(new XMLCommandHandler());
		ob.setRunName("Scan_"+(index+1));
		lines.add(index, ob);
		write();
		return ob;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExperimentObjectManager other = (ExperimentObjectManager) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (lines == null) {
			if (other.lines != null)
				return false;
		} else if (!lines.equals(other.lines))
			return false;
		return true;
	}

	/**
	 * Indiscriminately fires listeners, telling them to update.
	 */
	@Override
	public void fireExperimentObjectListeners() {
		final ExperimentObjectEvent evt = new ExperimentObjectEvent(this);
		evt.setCompleteRefresh(true);
		notifyExperimentObjectListeners(evt);
	}

	/**
	 * @return File
	 */
	@Override
	public IFolder getContainingFolder() {
		return (IFolder) file.getParent();
	}

	/**
	 * @return error text for what is wrong with the scan
	 */
	@Override
	public String getErrorMessage() {
		final StringBuilder buf = new StringBuilder();
		final List<IExperimentObject> runs = getExperimentList();
		for (IExperimentObject ob : runs) {
			final String errorText = ob.getErrorMessage();
			if (errorText != null) {
				buf.append(ob.getRunName());
				buf.append(":\n");
				buf.append(errorText);
				buf.append("\n");
			}
		}
		return buf.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IExperimentObject> getExperimentObjectType() {
		return (Class<IExperimentObject>) ExperimentObject.class.asSubclass(IExperimentObject.class);
	}

	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public String getName() {
		return file.getName().substring(0, file.getName().indexOf('.'));
	}

	@Override
	public Collection<IFile> getReferencedFiles() {

		final List<IExperimentObject> runs = getExperimentList();
		final Collection<IFile> ret = new HashSet<IFile>(19);
		ret.add(getFile());

		if (runs != null && !runs.isEmpty()) {
			for (IExperimentObject ob : runs) {
				ret.addAll(ob.getFiles());
			}
		}

		return ret;
	}

	@Override
	public List<IExperimentObject> getExperimentList() {
		final List<IExperimentObject> ret = new ArrayList<IExperimentObject>(lines.size());
		for (Object ob : lines)
			if (ob instanceof IExperimentObject)
				ret.add((IExperimentObject) ob);
		return ret;
	}

	protected IFile createCopy(IFile original) throws CoreException {
		IFile newFile = EclipseUtils.getUniqueFile(original, "xml");
		original.copy(newFile.getFullPath(), true, null);
		return newFile;
	}

	@Override
	public String getUniqueName(final String name) {
		final Matcher matcher = UNIQUE_PATTERN.matcher(name);
		int start = 0;
		String frag = name;
		if (matcher.matches()) {
			frag = matcher.group(1);
			start = Integer.parseInt(matcher.group(2));
		}

		return getUniqueName(frag, ++start);
	}

	private String getUniqueName(String frag, int i) {

		final String nameSug = frag + i;
		final List<IExperimentObject> runs = getExperimentList();
		if (runs != null && !runs.isEmpty()) {
			for (IExperimentObject ob : runs) {
				if (ob.getRunName().equals(nameSug)) {
					return getUniqueName(frag, ++i);
				}
			}
		}

		return nameSug;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((lines == null) ? 0 : lines.hashCode());
		return result;
	}

	/**
	 * returns true if no runs in this multiscan.
	 *
	 * @return true/false
	 */
	@Override
	public boolean isEmpty() {
		for (Object ob : lines)
			if (ob instanceof IExperimentObject)
				return false;
		return true;
	}

	/**
	 * @param fileName
	 * @return true if is a file name that the manager references.
	 */
	@Override
	public boolean isFileNameUsed(String fileName) {
		if (fileName == null)
			return false;
		if ("".equals(fileName))
			return false;

		final List<IExperimentObject> runs = getExperimentList();
		for (IExperimentObject ob : runs) {
			for (IFile file : ob.getFiles()) {
				if (fileName.equals(file.getName())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void notifyExperimentObjectListeners(final ExperimentObjectEvent evt) {
		if (listeners == null)
			return;
		for (ExperimentObjectListener l : listeners)
			l.runChangePerformed(evt);
	}

	@Override
	public void load(IFile file) {
		ExperimentFactory.addManager(this);
		setFile(file);

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file.getLocation().toFile()));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}

		try {
			lines = new ArrayList<IExperimentObject>(7);
			String line = null;
			while ((line = reader.readLine()) != null) {
				final IExperimentObject ob = createNewExperimentObject(line);
				lines.add(ob);
			}
		} catch (Exception ne) {
			ne.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void removeExperiment(IExperimentObject runObject) throws Exception {
		try {
			for (Iterator<IExperimentObject> iterator = lines.iterator(); iterator.hasNext();) {
				final IExperimentObject line = iterator.next();
				if (line.equals(runObject)) {
					iterator.remove();
					return;
				}
			}
		} finally {
			write();
		}
	}

	@Override
	public void removeExperimentObjectListener(ExperimentObjectListener l) {
		if (listeners == null)
			return;
		listeners.remove(l);
	}

	@Override
	public void setFile(IFile nameFile) {
		this.file = nameFile;
		notifyExperimentObjectListeners(new ExperimentObjectEvent(this));
	}

	@Override
	public void setName(String text) throws Exception {

		final String origName = file.getName();

		final IFile to = ((IFolder) file.getParent()).getFile(text + ".scan");
		if (!file.equals(to))
			file.move(to.getFullPath(), true, null);
		this.file = to;

		for (IExperimentObject line : lines){
			line.setMultiScanName(text);
		}

		notifyExperimentObjectListeners(new ExperimentObjectEvent(this));
		ExperimentFactory.getExperimentEditorManager().notifyFileNameChange(origName, to);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public void write() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getLocation().toFile()))){
			final Iterator<IExperimentObject> it = lines.iterator();
			while (it.hasNext()) {
				writer.write(it.next().toPersistenceString());
				writer.newLine();
			}
		} catch (IOException e) {
			logger.error("Error writing scan object to file - the scan may not be defined properly!", e);
		} finally {
			try {
				file.refreshLocal(IResource.DEPTH_ZERO, null);
			} catch (CoreException e) {
				logger.debug("Error refreshing the experiment explorer", e);
			}
		}

	}

}
