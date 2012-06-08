/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.structure;

import gda.analysis.datastructure.ManagedDataObject;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Code originally written by SLAC TEAM (AIDA) Modified at Diamond to make it more general A project which is an in
 * memory object which holds the scan data The purpose of this is to allow later manipulation of data in jython or java
 * without reading it in again etc... for example....curve fitting of data etc...
 */
public class Project extends ManagedDataObject implements Configurable, Serializable, Findable {
	
	private static final Logger logger = LoggerFactory.getLogger(Project.class);
	
	private Folder root;

	private Path currentPath;

	private String separatorChar = "/";

	private boolean readOnly;

	private ArrayList<String> mountList = new ArrayList<String>();

	private int mountCount = 0;

	private boolean overwrite = true;

	/**
	 * Create a new Project.
	 * 
	 * @param name
	 */
	public Project(String name) {
		this.setName(name);
		// Set the root directory
		// root = new Folder("/");
		root = new Folder();
		// Set the current path
		currentPath = new Path();

		// this.add("/", "/", root);
	}

	/**
	 * Finds an object from given path
	 * 
	 * @param path
	 * @return A ManagedObject. This could be a Folder, data object etc..
	 * @throws IllegalArgumentException
	 */
	private Object findObject(String path) throws IllegalArgumentException {

		Path p = new Path(currentPath, path);
		// quick root directory check
		if (p.toString().equals("/"))
			return root;

		// Load the folder data
		Folder folder = checkFolder(p.parent());
		if (folder == null)
			throw new IllegalArgumentException("Can not find Folder: " + p.parent());
		// Get the individual Object in the folder from the name
		Object obj = folder.getChild(p.getName());
		return obj;
	}

	/**
	 * Get the Object at a given path in the Project. The path can either be absolute or relative to the current working
	 * directory.
	 * 
	 * @param path
	 *            The path.
	 * @return The corresponding ManagedDataObject.
	 * @throws IllegalArgumentException
	 *             If the path does not correspond to an Object.
	 */
	public Object find(String path) throws IllegalArgumentException {
		Object obj = findObject(path);
		if (obj == null || obj instanceof Folder)
			throw new IllegalArgumentException("The path " + path + " does not correspond to a Object");
		if (obj instanceof Link)
			obj = find(((Link) obj).path().toString());
		return obj;
	}

	/**
	 * Get the Project at a given mount point.
	 * 
	 * @param path
	 *            Mount Point.
	 * @return The corresponding Project.
	 * @throws IllegalArgumentException
	 *             If the path does not correspond to a mount point.
	 */
	public Project findProject(String path) throws IllegalArgumentException {
		Object mp = findObject(path);
		if (!(mp instanceof MountPoint))
			throw new IllegalArgumentException("The given path " + path + " does not correspond to a mount point.");
		return ((MountPoint) mp).getProject();
	}

	/**
	 * Change to a given directory.
	 * 
	 * @param path
	 *            The absolute or relative path of the directory we are changing to.
	 * @throws IllegalArgumentException
	 *             If the path does not exist or path is not a directory.
	 */
	public void cd(String path) throws IllegalArgumentException {
		Path newPath = new Path(currentPath, path);
		Object obj = findObject(newPath.toString());
		// Object obj = findObject(currentPath.toString());

		if (obj == null)
			throw new IllegalArgumentException("Path does not exist" + path + "\t" + newPath);

		if (obj instanceof Link) {
			newPath = ((Link) obj).path();
			obj = findObject(((Link) obj).path().toString());
		}
		if (!(obj instanceof Folder))
			throw new IllegalArgumentException("Path: " + path + " is not a folder");

		currentPath = newPath;

	}

	/**
	 * Get the path of the current working directory.
	 * 
	 * @return The path of the current working directory.
	 */
	public String pwd() {
		return currentPath.toString();
	}

	/**
	 * List, into a given output stream Non-recursive
	 * 
	 * @throws IllegalArgumentException
	 *             If the path does not exist.
	 */
	public void ls() throws IllegalArgumentException {
		ls(".", false);
	}

	/**
	 * List, into a given output stream, all the ManagedObjects, including directories (but not "." and ".."), in a
	 * given path. Directories end with "/". Non-recursive
	 * 
	 * @param path
	 *            The path where the list has to be performed (by default the current directory "."). in all the
	 *            directories under path (the default is <code>false</code>.
	 * @throws IllegalArgumentException
	 *             If the path does not exist.
	 */
	public void ls(String path) throws IllegalArgumentException {
		ls(path, false);
	}

	/**
	 * List, into a given output stream, all the ManagedObjects, including directories (but not "." and ".."), in a
	 * given path. Directories end with "/". The list can be recursive.
	 * 
	 * @param path
	 *            The path where the list has to be performed (by default the current directory ".").
	 * @param recursive
	 *            If <code>true</code> the list is extended recursively in all the directories under path (the default
	 *            is <code>false</code>.
	 * @throws IllegalArgumentException
	 *             If the path does not exist.
	 */

	public void ls(String path, boolean recursive) {
		StringBuffer output = new StringBuffer();
		// ls is a recursive call..pass a string buffer into which the data is
		// stored
		ls(path, recursive, output);
		// JythonServerFacade.getInstance().print(output.toString());
		logger.debug(output.toString());

	}

	private void ls(String path, boolean recursive, StringBuffer output) throws IllegalArgumentException {

		Path p = new Path(currentPath, path);
		// Message.debug(p.toString());
		// Gets the last object in the path (must be a folder)
		Object obj = checkFolder(p);

		if (obj == null)
			throw new IllegalArgumentException("Wrong path " + path);
		// add the seperator characters
		if (obj instanceof Folder) {
			if (!path.endsWith(separatorChar))
				path += separatorChar;
		}
		if (obj instanceof Folder) {
			// Number of objects in the folder
			int childNumb = ((Folder) obj).getChildCount();
			// Get one of the objects
			// String testname = ((Folder) obj).getChildName(0);

			// Loop over the objects in the folder
			for (int i = 0; i < childNumb; i++) {
				// Get one of the objects
				String name = ((Folder) obj).getChildName(i);
				if (((Folder) obj).isFolder(name)) {
					output.append(new String(path + name + separatorChar + " \n"));

					if (recursive)
						ls(path + name, recursive, output);
				} else if (((Folder) obj).isLink(name)) {
					Object child = ((Folder) obj).getChild(name);
					output.append(new String(path + name + " -> " + ((Link) child).path().toString() + " \n"));
				} else {
					output.append(new String(path + name + " \n"));
				}
			}
		} else if (obj instanceof Link) {
			output.append(new String(path + " -> " + ((Link) obj).path().toString() + " \n"));
		} else {
			output.append(new String(path + " \n"));
		}
	}

	/**
	 * Create a new directory. Given a path only the last directory in it is created if all the intermediate
	 * subdirectories already exist.
	 * 
	 * @param path
	 *            The absolute or relative path of the new directory.
	 * @throws IllegalArgumentException
	 *             If a subdirectory within the path does not exist or it is not a directory. Also if the directory
	 *             already exists.
	 */
	public void mkdir(String path) throws IllegalArgumentException {
		// Path
		Path p = new Path(currentPath, path);
		// Remove '/'
		if (path.endsWith(separatorChar))
			path = path.substring(0, path.length() - 1);
		// Break path up into objects
		Object[] mo = findAlreadyCreatedObjects(p);
		// Check the directory or something with its name
		// doesn't already exist.
		if (mo[mo.length - 1] != null)
			throw new ProjectObjectAlreadyExistException("mkdir: Directory already exists: " + path);

		// Check there is a potentially a parent directory
		Object obj = mo[mo.length - 2];
		if (obj == null)
			throw new IllegalArgumentException("Cannot create directory, no parent " + path);

		// Check it is not a mounted file
		if (obj instanceof FileMountPoint)
			throw new IllegalArgumentException("Cannot create directory : This is a mounted file" + path);
		// if the parent is actually a folder then add the file
		if (obj instanceof Folder) {
			Folder parent = (Folder) obj;
			if (parent.getChild(p.getName()) != null)
				throw new IllegalArgumentException(p.getName() + " already exists");
			Folder child = new Folder();
			parent.add(p.getName(), child);
		} else
			throw new IllegalArgumentException("Cannot create directory " + path);
	}

	/**
	 * Create a directory recursively. Given a path the last directory and all the intermediate non-existing
	 * subdirectories are created.
	 * 
	 * @param path
	 *            The absolute or relative path of the new directory.
	 * @throws IllegalArgumentException
	 *             If an intermediate subdirectory is not a directory.
	 */
	public void mkdirs(String path) throws IllegalArgumentException {
		Path p = new Path(currentPath, path);
		// Break path up into objects
		Object[] mo = findAlreadyCreatedObjects(p);
		// folder to be created
		Object child = mo[mo.length - 1];
		// if it doesn't already exist
		if (child == null) {
			Folder folder = null;
			for (int i = 0; i < mo.length - 1; i++) {
				if (mo[i] instanceof FileMountPoint)
					throw new IllegalArgumentException("Path: " + path + " contains a mounted file");

				if (!(mo[i] instanceof Folder))
					throw new IllegalArgumentException("Path: " + path + " contains not Folder");

				folder = (Folder) mo[i];
				child = mo[i + 1];
				if (child == null) {
					child = new Folder();
					folder.add((p.toString(i, i)).substring(1), child);
					mo[i + 1] = child;

				}
			}
		} else if (!(child instanceof Folder))
			throw new IllegalArgumentException(path + " is not a folder");
	}

	/**
	 * Remove a directory and all the contents underneath.
	 * 
	 * @param path
	 *            The absolute or relative path of the directory to be removed.
	 * @throws IllegalArgumentException
	 *             If path does not exist or if it is not a directory.
	 */
	public void rmdir(String path) throws IllegalArgumentException {
		Path p = new Path(currentPath, path);
		// Split path into objects
		Object[] mo = findAlreadyCreatedObjects(p);
		// target folder
		Object target = mo[mo.length - 1];
		if (target == null)
			throw new IllegalArgumentException("Directory does not exist: " + path);
		if (!(target instanceof Folder))
			throw new IllegalArgumentException(path + " is not a folder");
		if (target == root)
			throw new IllegalArgumentException("Cannot delete root");
		Folder folder = (Folder) mo[mo.length - 2];
		// p.getName();
		// folder.remove(folder.getChildName(target));
		folder.remove(p.getName());
	}

	/**
	 * Remove an Object by specifying its path. If the path points to a mount point, the mount point should first
	 * commit, then close and delete the project object.
	 * 
	 * @param path
	 *            The absolute or relative path of the Object to be removed.
	 * @throws IllegalArgumentException
	 *             If path does not exist.
	 */
	public void rm(String path) throws IllegalArgumentException {
		Path p = new Path(currentPath, path);

		Object[] mo = findAlreadyCreatedObjects(p);

		Object target = mo[mo.length - 1];

		if (target == null)
			throw new IllegalArgumentException("Object does not exist: " + path);
		if (target instanceof Folder)
			throw new IllegalArgumentException(path + " is a folder");
		if (mo[mo.length - 2] instanceof FileMountPoint)
			throw new IllegalArgumentException(path + " contains a mounted file");
		if (target == root)
			throw new IllegalArgumentException("Cannot delete root");
		Folder folder = (Folder) mo[mo.length - 2];
		p.getName();
		folder.remove(p.getName());
		// folder.remove(folder.getChildName(target));
	}

	/**
	 * Move an Object or a directory from one directory to another.
	 * 
	 * @param oldPath
	 *            The path of the Object or direcoty to be moved.
	 * @param newPath
	 *            The path of the diretory in which the object has to be moved to.
	 * @throws IllegalArgumentException
	 *             If either path does not exist.
	 */
	public void mv(String oldPath, String newPath) throws IllegalArgumentException {

		// Find the object to be moved and the folder that is containing it.
		Path fromPath = new Path(currentPath, oldPath);
		Folder fromFolder;
		Object objToMove;
		String nameOfObjToMove;
		try {
			fromFolder = (Folder) findObject(fromPath.parent().toString());
			objToMove = findObject(fromPath.toString());
			nameOfObjToMove = fromPath.getName();
			// nameOfObjToMove = fromFolder.getChildName(objToMove);
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("Illegal \"from\"-path " + oldPath
					+ "; it does not correspond to an Object.");
		}
		if (objToMove == null)
			throw new IllegalArgumentException("Cannot move. Path " + fromPath.toString()
					+ " corresponds to a null object.");

		// The fullpath of the destination
		Path toPath = new Path(currentPath, newPath);

		// Get the destination object, if it exists.
		Object destinationObject = null;
		try {
			destinationObject = findObject(toPath.toString());
		} catch (IllegalArgumentException iae) {
		}

		String newName = null;

		if (destinationObject != null) {
			if (!(destinationObject instanceof Folder)) {
				if (objToMove instanceof Folder)
					throw new IllegalArgumentException(
							"Cannot move a folder on an existing Object that is not a folder.");
				else if (overwrite) {
					// fromFolder.getChildName(objToMove);
					newName = nameOfObjToMove;
					rm(toPath.toString());
				} else
					throw new IllegalArgumentException(
							"Cannot move. An object exists in the final path and the overwrite flag is set to false.");
			}
		} else
			newName = toPath.getName();

		// Get the destination folder
		Path destinationFolderPath;
		if (destinationObject != null && destinationObject instanceof Folder)
			destinationFolderPath = toPath;
		else
			destinationFolderPath = toPath.parent();

		// Remove the object from the original directory.
		fromFolder.remove(nameOfObjToMove);

		// Rename the object if necessary
		// if (newName != null)
		// objToMove.setName(newName);

		// Find the folder in which the object has to be moved
		try {
			add(destinationFolderPath.toString(), newName, objToMove, overwrite, false);
		} catch (Throwable t) {
			// Maybe a better message is required if the final object cannot
			// be
			// overwritten.
			throw new IllegalArgumentException("Illegal \"to\"-path " + newPath
					+ ". Cannot move the object to this path.");
		}
		// Folder destinationFolder =
		// (Folder) findObject(destinationFolderPath.toString());
		// if ((fromFolder.isBeingWatched() ||
		// destinationFolder.isBeingWatched()))
		// fireStateChanged(new projectEvent(this, projectEvent.NODE_MOVED,
		// toPath.toArray(), null, fromPath.toArray()));

	}

	/**
	 * Commit any open transaction to the underlying store(s). It flushes objects into the disk for non-memory-mapped
	 * stores.
	 */
	public void commit() {
	}

	/**
	 * Set the strategy of what should happen if two objects have the same path. Default is overwrite.
	 */
	public void setOverwrite() {
		setOverwrite(true);
	}

	/**
	 * Set the strategy of what should happen if two objects have the same path. Default is overwrite.
	 * 
	 * @param overwrite
	 *            <code>true</code> to enable overwriting.
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * Copy an object from a path to another.
	 * 
	 * @param oldPath
	 *            The path of the object to be copied.
	 * @param newPath
	 *            The path where the object is to be copied.
	 * @throws IllegalArgumentException
	 *             If either path does not exist.
	 */
	public void cp(String oldPath, String newPath) throws IllegalArgumentException {
		cp(oldPath, newPath, false);
	}

	/**
	 * Copy an object from a path to another.
	 * 
	 * @param oldPath
	 *            The path of the object to be copied.
	 * @param newPath
	 *            The path where the object is to be copied.
	 * @param recursive
	 *            <code>true</code> if a recursive copy has to be performed.
	 * @throws IllegalArgumentException
	 *             If either path does not exist.
	 */
	public void cp(String oldPath, String newPath, boolean recursive) throws IllegalArgumentException {

		// Find the object to be moved and the folder that is containing it.
		Path fromPath = new Path(currentPath, oldPath);
		Object objToCopy;
		try {
			objToCopy = findObject(fromPath.toString());
		}

		catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("Illegal \"from\"-path " + oldPath
					+ "; it does not correspond to a Object.");
		}

		if (objToCopy == null)
			throw new IllegalArgumentException("Cannot copy. Path " + fromPath.toString()
					+ " corresponds to a null object.");
		//
		// Folder fromFolder = (Folder)
		// findObject(fromPath.parent().toString());
		String fromName = fromPath.getName();
		// String fromName = fromFolder.getChildName(objToCopy);
		// The fullpath of the destination
		Path toPath = new Path(currentPath, newPath);

		// Get the destination object, if it exists.
		Object destinationObject = null;
		try {
			destinationObject = findObject(toPath.toString());
		} catch (IllegalArgumentException iae) {
		}

		if (destinationObject != null) {
			if (!(destinationObject instanceof Folder)) {
				if (objToCopy instanceof Folder)
					throw new IllegalArgumentException(
							"Cannot copy a folder on an existing Object that is not a folder.");
				else if (overwrite) {
					rm(toPath.toString());
				} else
					throw new IllegalArgumentException(
							"Cannot copy. An object exists in the final path and the overwrite flag is set to false.");
			} else
				toPath = new Path(toPath, fromName);
		}
		// Make a copy of the Object
		// Folder folder = (Folder) findObject(fromPath.parent().toString());
		String name = fromPath.getName();
		// String name = folder.getChildName(objToCopy);
		copyObject(toPath.toString(), name, objToCopy);

		if (recursive && objToCopy instanceof Folder) {
			String fromPathString = fromPath.toString();
			String[] objsToCopy = listObjectNames(fromPathString);
			for (int i = 0; i < objsToCopy.length; i++) {
				String objRelName = objsToCopy[i].substring(fromPathString.length() + 1);
				Path endPath = new Path(toPath, objRelName);
				cp(objsToCopy[i], endPath.toString(), recursive);
			}
		}

	}

	/**
	 * Create a symbolic link to an object in the Iproject.
	 * 
	 * @param path
	 *            The absolute or relative path of the object to be linked.
	 * @param alias
	 *            The absolute or relative name of the link.
	 * @throws IllegalArgumentException
	 *             If path or any subidrectory within path does not exist.
	 */
	public void symlink(String path, String alias) throws IllegalArgumentException {

		// Find the object to be linked.
		Path fromPath = new Path(currentPath, path);
		Object objToLink;
		try {
			objToLink = findObject(fromPath.toString());
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("Illegal \"from\"-path " + path
					+ "; it does not correspond to an IManagedObejct.");
		}
		if (objToLink == null)
			throw new IllegalArgumentException("Cannot copy. Path " + fromPath.toString()
					+ " corresponds to a null object.");

		// Check that there is no Object (other than a folder) where
		// the link is going to be.
		Path toPath = new Path(currentPath, alias);
		Object aliasObj = null;
		Folder aliasFolder = null;
		try {
			aliasObj = findObject(toPath.toString());
			aliasFolder = (Folder) findObject(toPath.parent().toString());
		} catch (IllegalArgumentException iae) {
		}

		if (aliasObj != null) {
			if (!(aliasObj instanceof Folder)) {
				throw new IllegalArgumentException(
						"There is already an object, other than a directory, in the \"alias\" location " + alias);
			}

			toPath = new Path(toPath, fromPath.getName());
			aliasFolder = (Folder) aliasObj;
		} else if (aliasFolder == null) {
			throw new IllegalArgumentException("Illegal \"alias\" " + alias);
		}

		checkForCircularLink(fromPath, toPath);

		Link link = new Link(toPath.getName(), fromPath);
		aliasFolder.add(link.getName(), link);

	}

	private void checkForCircularLink(Path from, Path to) {
		if (from.toString().equals(to.toString()))
			throw new IllegalArgumentException("Circular link " + from.toString() + " -> " + to.toString());
		Object fromObj = null;
		try {
			fromObj = findObject(from.toString());
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("The link chain somewhere is broken");
		}
		if (fromObj == null)
			throw new IllegalArgumentException("The link chain somewhere is broken");

		if (fromObj instanceof Link)
			checkForCircularLink(((Link) fromObj).path(), to);
	}

	/**
	 * Mounts a project within another (target) project. A project can only be mounted once. Example:
	 * 
	 * <pre>
	 * target.mount(&quot;/home/tonyj&quot;, project, &quot;/&quot;);
	 * </pre>
	 * 
	 * @param path
	 *            The path in the target project
	 * @param project
	 *            The project to mount within the target project
	 * @param projectPath
	 *            The mount point within the project to be mounted.
	 * @throws IllegalArgumentException
	 */
	public void mount(String path, Project project, String projectPath) throws IllegalArgumentException {
		Path p = new Path(currentPath, path);
		Object[] mo = findAlreadyCreatedObjects(p);
		Object obj = mo[mo.length - 1];
		if (obj != null)
			throw new IllegalArgumentException(path + " already exists");

		Object f = mo[mo.length - 2];

		if (f == null) {
			f = checkFolder(p);
		}
		if (f == null)
			throw new IllegalArgumentException("Folder does not exist: " + p.parent());

		Folder parent = (Folder) f;
		Project t = project;

		Path mountPath = new Path(new Path(), projectPath);

		Object mountPoint = t.checkFolder(mountPath);

		if (mountPoint == null)
			throw new IllegalArgumentException("Can not find Mount Point: " + projectPath);

		if (mountPoint instanceof Folder) {
			MountPoint mp = new MountPoint(p, t, (Folder) mountPoint, mountPath);
			// quick root directory check

			if (mountPath.toString().equals("/")) {
				// Add this folder to the folder
				parent.add(p.getName(), mp);

			} else {
				parent.add(p.getName(), mp);
				t.incrementMountCount();
				mountList.add(p.toString());
			}
		} else
			throw new IllegalArgumentException(projectPath + " does not point to a folder");

	}

	/**
	 * Mounts a project within another (target) project. A project can only be mounted once. Example:
	 * 
	 * <pre>
	 * target.mount(&quot;/home/tonyj&quot;, project, &quot;/&quot;);
	 * </pre>
	 * 
	 * @param path
	 *            The path in the target project
	 * @param filename
	 * @param type
	 * @throws IllegalArgumentException
	 */
	public void mountFile(String path, String filename, String type) throws IllegalArgumentException {
		Path p = new Path(currentPath, path);
		Object[] mo = findAlreadyCreatedObjects(p);
		Object obj = mo[mo.length - 1];
		if (obj != null)
			throw new IllegalArgumentException(path + " already exists");

		Object f = mo[mo.length - 2];

		if (f == null) {
			f = checkFolder(p);
		}
		if (f == null)
			throw new IllegalArgumentException("Folder does not exist: " + p.parent());

		FileMountPoint fmp = new FileMountPoint(filename, type);
		Folder parent = (Folder) f;
		parent.add(p.getName(), fmp);

	}

	/**
	 * Unmount a subproject at a given path (mount point). Whenever a project is destroyed it first unmounts all
	 * dependent projects.
	 * 
	 * @param path
	 *            The path of the subproject to be unmounted.
	 * @throws IllegalArgumentException
	 *             If path does not exist.
	 */
	public void unmount(String path) throws IllegalArgumentException {
		Path p = new Path(currentPath, path);
		Object[] mo = findAlreadyCreatedObjects(p);

		Object obj = mo[mo.length - 1];
		if (obj == null)
			throw new IllegalArgumentException("Path does not exist: " + p.parent());
		if (mo[mo.length - 2] == null)
			throw new IllegalArgumentException("Folder does not exist: " + p.parent());

		if (obj instanceof MountPoint) {
			MountPoint mp = (MountPoint) obj;
			Folder parent = (Folder) mo[mo.length - 2];
			// parent.remove(parent.getChildName(mp));
			parent.remove(p.getName());
			mountList.remove(p.toString());
			mp.unmount();
		} else
			throw new IllegalArgumentException("Not a mount point");
	}

	/**
	 * Method to check if it is read only.
	 * 
	 * @return If it is read only.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * This "add" method is called from the IStore, and can create new folders if it is needed. Does not overwrite
	 * existing objects, just skip them.
	 * 
	 * @param path
	 * @param name
	 * @param child
	 */
	public void add(String path, String name, Object child) {
		add(path, name, child, false, true);
	}

	/**
	 * This "add" method is called from Factories (HistogramFactory, ...), and can create new folders if it is needed.
	 * It does overwrite existing objects.
	 * 
	 * @param path
	 * @param name
	 * @param child
	 */
	public void addFromFactory(String path, String name, Object child) {
		Path fullPath = new Path(currentPath, path);
		add(fullPath.toString(), name, child, true, false);
	}

	private void add(String path, String name, Object child, boolean overwrite, boolean createNewDirs) {
		// Current path
		Path folderPath = new Path(currentPath, path);
		// Object path...i.e. new path name
		Path objectPath = new Path(currentPath, (path + separatorChar + name));
		// Break path up into objects
		Object[] mo = findAlreadyCreatedObjects(objectPath);
		// Check if the object list is ok
		if (mo == null || mo.length < 1)
			throw new IllegalArgumentException("Invalid path: +" + objectPath.toString());
		// Get the folder the object is to go into
		Object o = mo[mo.length - 1];
		if (o != null) {
			if (overwrite)
				rm(objectPath.toString());
			else
				return;
		}
		if (mo[mo.length - 2] != null && !(mo[mo.length - 2] instanceof Folder))
			throw new IllegalArgumentException("Illegal path for add: " + path);
		if (mo[mo.length - 2] instanceof FileMountPoint)
			throw new IllegalArgumentException("Cannot add objects to the mounted file: " + path);
		// the parent directory doesn't exist!!
		if (mo[mo.length - 2] == null) {
			if (createNewDirs) {
				mkdirs(path);
				mo = findAlreadyCreatedObjects(objectPath);
			} else
				throw new RuntimeException("Some directories in the given path " + folderPath + " do not exist");
		}
		Folder f = (Folder) mo[mo.length - 2];
		f.add(name, child);
	}

	/**
	 * Return all Object in the path, from the Root down to the last path node (inclusive). No interaction with the
	 * Store - no new objects are created here. If some objects in the path do not exist in the project, fill "null".
	 * This method can be used on folder or object
	 * 
	 * @param path
	 * @return all Object in the path,
	 */
	private Object[] findAlreadyCreatedObjects(Path path) {
		int size = path.size();
		Object[] mo = new Object[size + 1];

		int depth = 0;
		mo[0] = root;
		Folder here = root;
		Object obj = null;
		for (Iterator<String> i = path.iterator(); i.hasNext();) {
			depth++;
			String name = i.next();
			obj = here.getChild(name);
			if (obj instanceof Link)
				mo[depth] = findObject(((Link) obj).path().toString());
			else
				mo[depth] = obj;
			if (obj == null) {
				break;
			}
			if (obj instanceof Folder)
				here = (Folder) obj;
			else
				break;
		}
		return mo;
	}

	/**
	 * Returns the last object in the Path, the object must be a folder
	 * 
	 * @param path
	 *            Path to the folder that needs to be checked and, possibly, filled.
	 * @return the last object in the Path, the object must be a folder. If object is a Folder, check if it has been
	 *         filled and fill it from the Store if needed.
	 * @throws IllegalArgumentException
	 *             If the path does not exist in Store, or if it is not a directory.
	 */
	private Folder checkFolder(Path path) throws IllegalArgumentException {
		Folder folder = null;
		Object[] mo = findAlreadyCreatedObjects(path);

		if (mo == null || mo.length == 0)
			throw new IllegalArgumentException("Invalid path: +" + path.toString());
		Object obj = mo[mo.length - 1];

		if (obj != null && !(obj instanceof Folder))
			throw new IllegalArgumentException("Path does not point to a directory: " + path.toString());
		if (obj != null) {
			folder = (Folder) obj;
		}
		return folder;
	}

	private int incrementMountCount() {
		mountCount++;
		return mountCount;
	}

	protected int decrementMountCount() {
		mountCount--;
		return mountCount;
	}

	@Override
	public void configure() throws FactoryException {

	}

	@Override
	public String getType() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setName(String name) {

	}

	private void copyObject(String path, String name, Object obj) {
		if (obj instanceof Folder) {
			mkdirs(path);
		} else if (obj instanceof Link) {
			Path newPath = new Path(currentPath, path);
			Link link = new Link(newPath.getName(), ((Link) obj).path());
			add(newPath.parent().toString(), name, link, true, false);
		} else if (obj != null) {
			Path newPath = new Path(currentPath, path);
			try {
				add(newPath.parent().toString(), name, ObjectCloner.deepCopy(obj), true, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("Cannot copy Object " + name + "\t" + obj);
		}
	}

	/**
	 * Get the list of names of the Objects under a given path, including directories (but not "." and "..").
	 * Directories end with "/". The returned names are appended to the given path unless the latter is ".". in all the
	 * directories under path (the default is <code>false</code>.
	 * 
	 * @return list of names of objects
	 * @throws IllegalArgumentException
	 *             If the path does not exist.
	 */
	public String[] listObjectNames() throws IllegalArgumentException {
		return listObjectNames(currentPath.getName());
	}

	/**
	 * Get the list of names of the Objects under a given path, including directories (but not "." and "..").
	 * Directories end with "/". The returned names are appended to the given path unless the latter is ".".
	 * 
	 * @param path
	 *            The path where the list has to be performed (by default the current directory "."). in all the
	 *            directories under path (the default is <code>false</code>.
	 * @return List of objects
	 * @throws IllegalArgumentException
	 *             If the path does not exist.
	 */
	public String[] listObjectNames(String path) throws IllegalArgumentException {
		return listObjectNames(path, false);
	}

	/**
	 * Get the list of names of the Objects under a given path, including directories (but not "." and "..").
	 * Directories end with "/". The returned names are appended to the given path unless the latter is ".".
	 * 
	 * @param path
	 *            The path where the list has to be performed (by default the current directory ".").
	 * @param recursive
	 *            If <code>true</code> the list is extended recursively in all the directories under path (the default
	 *            is <code>false</code>.
	 * @return String array of object names
	 * @throws IllegalArgumentException
	 *             If the path does not exist.
	 */
	public String[] listObjectNames(String path, boolean recursive) throws IllegalArgumentException {
		Path p = new Path(currentPath, path);
		Object obj = checkAndFillFolder(p);
		if (obj == null)
			throw new IllegalArgumentException("Wrong path " + path);
		if (obj instanceof Folder) {
			if (!path.endsWith(separatorChar))
				path += separatorChar;
		}
		Vector<String> names = new Vector<String>();
		if (obj instanceof Folder) {
			int childNumb = ((Folder) obj).getChildCount();
			for (int i = 0; i < childNumb; i++) {
				// Object child = ((Folder)obj).getChild(i);
				String childName = ((Folder) obj).getChildName(i);
				if (((Folder) obj).isFolder(childName)) {
					names.add(path + childName + separatorChar);
					if (recursive) {
						String[] childNames = listObjectNames(path + childName, recursive);
						for (int j = 0; j < childNames.length; j++)
							names.add(childNames[j]);
					}
				} else
					names.add(path + childName);
			}
		} else
			names.add(path);

		String[] objNames = new String[names.size()];
		for (int i = 0; i < names.size(); i++)
			objNames[i] = names.get(i);
		return objNames;
	}

	/**
	 * Returns the last object in the Path. If object is a Folder, check if it has been filled and fill it from the
	 * Store if needed.
	 * 
	 * @param path
	 *            Path to the folder that needs to be checked and, possibly, filled.
	 * @return Object The last object in the path.
	 * @throws IllegalArgumentException
	 *             If the path does not exist in Store.
	 */
	private Object checkAndFillFolder(Path path) throws IllegalArgumentException {
		Object[] mo = findAlreadyCreatedObjects(path);
		if (mo == null || mo.length == 0)
			throw new IllegalArgumentException("Invalid path: +" + path.toString());
		Object obj = mo[mo.length - 1];
		return obj;
	}

}