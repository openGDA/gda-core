/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.synoptics.utils;

import gda.data.PathConstructor;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beamline.synoptics.events.LatestFilenameEvent;


public class DataFileListener implements FileListener, Configurable, IObservable {
	public static final Logger logger = LoggerFactory.getLogger(DataFileListener.class);
	private ObservableComponent observableComponent = new ObservableComponent();
	private String directory = null;
	private boolean server = false;
	private boolean configured=false;
	private FileSystemManager fsManager;
	/**
	 * A flag used to determine if adding files to be monitored should be recursive.
	 */
	private boolean recursive = false;
	
	private List<FileObject> dataFileCollected = Collections.synchronizedList(new ArrayList<FileObject>());
	/**
	 * set the list of directories not to be monitored
	 */
	private String[] excludedDirectory = new String[] {};
	/**
	 * set the extension of file name to be added to data file list
	 */
	private String[] filenameExtensions = new String[] {};
	private GDADataDirectoryMonitor fm;
//	private DefaultFileMonitor fm;
	private String name;
	private FileObject listendir;
	
	public DataFileListener() {
		
	}
	
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getDirectory() == null || getDirectory().isEmpty()) {
				if (isServer()) {
					setDirectory(PathConstructor.createFromDefaultProperty());
				} else {
					setDirectory(PathConstructor.createFromRCPProperties());
				}
			}
			logger.debug("Data directory to monitor: {}", getDirectory());
			try {
				fsManager = VFS.getManager();
				listendir = fsManager.resolveFile(getDirectory());
				fm = new GDADataDirectoryMonitor(this);
				fm.setExcludedDirectory(getExcludedDirectory());
				fm.setFilenameExtensions(getFilenameExtensions());
//				fm = new DefaultFileMonitor(this);
				fm.setRecursive(isRecursive());
				fm.addFile(listendir);
				fm.start();
				setDataFileCollected(fm.getDataFileCollectedSoFar());
//				setDataFileCollected(new ArrayList<FileObject>(Arrays.asList(listendir.getChildren())));
				int i=0;
				for (FileObject file : getDataFileCollected()) {
					logger.debug("initialize detector file {}: {} in the list", i, file.getName());
					i++;
				}				
			} catch (FileSystemException e) {
				logger.error("Cannot resolve file " + getDirectory(), e);
				throw new FactoryException("Failed to resolve directory "+getDirectory(), e);
			} 
			setConfigured(true);
			if (!getDataFileCollected().isEmpty()) {
				int index = getDataFileCollected().size()-1;
				observableComponent.notifyIObservers(this, new LatestFilenameEvent(index, getDataFileCollected().get(index).getName().getPath()));
			} else {
				observableComponent.notifyIObservers(this, new LatestFilenameEvent(null, null));
			}
		}
	}

	public void dispose() {
		fm.stop();
	}
	
	@Override
	public void fileChanged(FileChangeEvent arg0) throws Exception {
		FileObject file = arg0.getFile();
		if (!getDataFileCollected().contains(file)){
			if (FilenameUtils.isExtension(file.getName().getBaseName(), getFilenameExtensions())) {
				getDataFileCollected().add(file);
				logger.debug("File {} changed, add it to the detector file list", file.getName().getPath());
				observableComponent.notifyIObservers(this, new LatestFilenameEvent(getDataFileCollected().size()-1, file.getName().getPath()));
			} else {
				logger.debug("File {} changed, don't add to the detector file list", file.getName().getPath());
			}
		} else {
			int index = getDataFileCollected().indexOf(file);
			logger.debug("File {} changed, it is already in the data file list", file.getName().getPath());
			observableComponent.notifyIObservers(this, new LatestFilenameEvent(index, getDataFileCollected().get(index).getName().getPath()));
		}
	}

	@Override
	public void fileCreated(FileChangeEvent arg0) throws Exception {
		FileObject file = arg0.getFile();
		if (!getDataFileCollected().contains(file)){
			if (FilenameUtils.isExtension(file.getName().getBaseName(), getFilenameExtensions())) {
				getDataFileCollected().add(file);
				logger.debug("File {} created, add it to the detector file list", file.getName().getPath());
				observableComponent.notifyIObservers(this, new LatestFilenameEvent(getDataFileCollected().size()-1, file.getName().getPath()));
			} else {
				logger.debug("File {} created, don't add to the detector file list", file.getName().getPath());
			}
		}
	}

	@Override
	public void fileDeleted(FileChangeEvent arg0) throws Exception {
		FileObject file = arg0.getFile();
		if (getDataFileCollected().contains(file)){
			int index = getDataFileCollected().indexOf(file);
			getDataFileCollected().remove(file);
			logger.debug("File {} deleted, remove it from the detector file list", file.getName().getPath());
			if (index>0) {
				observableComponent.notifyIObservers(this, new LatestFilenameEvent(index-1, getDataFileCollected().get(index-1).getName().getPath())); //on remove go back one
			} else {
				if (getDataFileCollected().isEmpty()) {
					observableComponent.notifyIObservers(this, new LatestFilenameEvent(null, null)); //data file collected empty
				} else {
					observableComponent.notifyIObservers(this, new LatestFilenameEvent(index, getDataFileCollected().get(index).getName().getPath())); //on remove go forward one
				}
			}
		}
	}
	
	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	public boolean isServer() {
		return server;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	public boolean isConfigured() {
		return configured;
	}

	public void setConfigured(boolean configured) {
		this.configured = configured;
	}
	/**
	 * Access method to get the recursive setting when adding files for monitoring.
	 */
	public boolean isRecursive() {
		return this.recursive;
	}

	/**
	 * Access method to set the recursive setting when adding files for monitoring.
	 */
	public void setRecursive(final boolean newRecursive) {
		this.recursive = newRecursive;
	}

	public String[] getExcludedDirectory() {
		return excludedDirectory;
	}

	public void setExcludedDirectory(String[] excludedDirectory) {
		this.excludedDirectory = excludedDirectory;
	}

	public String[] getFilenameExtensions() {
		return filenameExtensions;
	}

	public void setFilenameExtensions(String[] filenameExtensions) {
		this.filenameExtensions = filenameExtensions;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public List<FileObject> getDataFileCollected() {
		return dataFileCollected;
	}

	public void setDataFileCollected(List<FileObject> dataFileCollected) {
		this.dataFileCollected = dataFileCollected;
	}



}
