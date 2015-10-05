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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileMonitor;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A polling {@link FileMonitor} implementation.<br />
 * <br />
 * The {@link GDADataDirectoryMonitor} is a Thread based polling file system monitor with a 1
 * second delay.<br />
 * <br />
 * <b>Design:</b>
 * <p/>
 * <ul>
 * <li>By design, GDA data files are used to record the original science experiment data, thus, 
 * once created, they are not allowed to be modified or deleted during data collection. 
 * Therefore these file do not need to be monitored directly after creation. This class need
 * only monitor the data directory for the creation of new data file.
 * 
 * <li>There is a Map of monitors known as {@link FileMonitorAgent}. With the thread running,
 * each {@link FileMonitorAgent} object is asked to "check" on the directory it is
 * responsible for. To do this check, the cache is cleared.
 * 
 * <li>New files are detected during each "check" as each directory does a check for new
 * children. If new children are found, create events are fired recursively if recursive
 * descent is enabled.
 * 
 * <li>{@link GDADataDirectoryMonitor} supports both directory filter and file extension filter.
 * </ul>
 * </p>
 * <p/>
 * For performance reasons, added a delay that increases as the number of directories monitored
 * increases. The default is a delay of 1 second for every 1000 directories processed.
 * </p>
 * <p/>
 * <br /><b>Example usage:</b><pre>
 * see {@link DataFileListener}
 *
 * Acknowledgement: This implementation heavily referenced to {@link org.apache.commons.vfs.impl.DefaultFileMonitor}
 */
public class GDADataDirectoryMonitor implements Runnable, FileMonitor {

	public static final Logger logger = LoggerFactory.getLogger(GDADataDirectoryMonitor.class);
	/**
	 * Map from FileName to FileObject being monitored.
	 */
	private final Map<FileName, FileMonitorAgent> monitorMap = new HashMap<FileName, FileMonitorAgent>();
	
	/**
	 * The low priority thread used for checking the files being monitored.
	 */
	private Thread monitorThread;
	
	/**
	 * File objects to be removed from the monitor map.
	 */
	private Stack<FileObject> deleteStack = new Stack<FileObject>();
	
	/**
	 * File objects to be added to the monitor map.
	 */
	private Stack<FileObject> addStack = new Stack<FileObject>();
	
	/**
	 * A flag used to determine if the monitor thread should be running.
	 */
	private boolean shouldRun = true;
	
	/**
	 * A flag used to determine if adding files to be monitored should be recursive.
	 */
	private boolean recursive = false;
	
	/**
	 * Set the delay between checks
	 */
	private long delay = 1000;
	
	/**
	 * Set the number of files to check until a delay will be inserted
	 */
	private int checksPerRun = 1000;
	
	/**
	 * A listener object that if set, is notified on file creation and deletion.
	 */
	private final FileListener fileListener;

	private ArrayList<FileObject> dataFileCollectedSoFar = new ArrayList<FileObject>();
	/**
	 * set the list of directories not to be monitored
	 */
	private String[] excludedDirectory = new String[] {};
	/**
	 * set the extension of file name to be added to data file list
	 */
	private String[] filenameExtensions = new String[] {};

	public GDADataDirectoryMonitor(final FileListener listener) {
		this.fileListener=listener;
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

	/**
	 * Access method to get the current FileListener object notified when there are changes with the files added.
	 */
	FileListener getFileListener() {
		return this.fileListener;
	}

	/**
	 * If a file is a folder, add it to be monitored, else it is a normal file add it to the data collected list. In GDA
	 * we do not need to monitor data file directly as once it been created it cannot be either modified or deleted. we
	 * only need to monitor the raw data folder to pick up new file created by the GDA servers.
	 */
	@Override
	public void addFile(final FileObject file) {
		_addFile(file); 
		try {
			// add all direct children too
			if (file.getType().hasChildren()) {
				// Traverse the children
				final FileObject[] children = file.getChildren();
				if (children==null) {
					return;
				}
				for (int i = 0; i < children.length; i++) {
					_addFile(children[i]);
				}
			}
		} catch (FileSystemException fse) {
			logger.error(fse.getLocalizedMessage(), fse);
		}
	}

	/**
	 * Adds a directory or folder to be monitored.
	 */
	private void _addFile(final FileObject file) {
		if (getExcludedDirectory().length > 0) {
			// don't register excluded directories
			for (String each : getExcludedDirectory()) {
				if (file.getName().getBaseName().contentEquals(each)) {
					logger.debug("exclude directory {} ",file.getName().getBaseName() );
					return;
				}
			}
		}
		synchronized (this.monitorMap) {
			if (this.monitorMap.get(file.getName()) == null) {
				this.monitorMap.put(file.getName(), new FileMonitorAgent(this, file));

				try {
					if (this.getFileListener() != null) {
						file.getFileSystem().addListener(file, this.getFileListener());
						logger.debug("Add file listener {} to direcory {}", this.getFileListener().getClass().getName(), file.getName().getPath());
					}

					if (file.getType().hasChildren() && this.recursive) { //folder
						// Traverse the children
						final FileObject[] children = file.getChildren();
						for (int i = 0; i < children.length; i++) {
								this.addFile(children[i]); // Add depth first
						}
					} else { //file
						if (FilenameUtils.isExtension(file.getName().getBaseName(), getFilenameExtensions())) {
							getDataFileCollectedSoFar().add(file);
							logger.debug("{}: added to the data file list", file.getURL());
						}
					}
				} catch (FileSystemException fse) {
					logger.error(fse.getLocalizedMessage(), fse);
				}

			}
		}
	}

	/**
	 * Removes a file from being monitored.
	 */
	@Override
	public void removeFile(final FileObject file) {
		synchronized (this.monitorMap) {
			FileName fn = file.getName();
			if (this.monitorMap.get(fn) != null) {
				FileObject parent;
				try {
					parent = file.getParent();
				} catch (FileSystemException fse) {
					parent = null;
				}

				this.monitorMap.remove(fn);

				if (parent != null) { // Not the root
					FileMonitorAgent parentAgent = this.monitorMap.get(parent.getName());
					if (parentAgent != null) {
						parentAgent.resetChildrenList();
					}
				}
			}
		}
	}

	/**
	 * Queues a file for removal from being monitored.
	 */
	protected void queueRemoveFile(final FileObject file) {
		this.deleteStack.push(file);
	}

	/**
	 * Get the delay between runs
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * Set the delay between runs
	 */
	public void setDelay(long delay) {
		if (delay > 0) {
			this.delay = delay;
		} else {
			this.delay = 1000;
		}
	}

	/**
	 * get the number of files to check per run
	 */
	public int getChecksPerRun() {
		return checksPerRun;
	}

	/**
	 * set the number of files to check per run. a additional delay will be added if there are more files to check
	 * 
	 * @param checksPerRun
	 *            a value less than 1 will disable this feature
	 */
	public void setChecksPerRun(int checksPerRun) {
		this.checksPerRun = checksPerRun;
	}

	/**
	 * Queues a file for addition to be monitored.
	 */
	protected void queueAddFile(final FileObject file) {
		this.addStack.push(file);
	}

	/**
	 * Starts monitoring the files that have been added.
	 */
	public void start() {
		if (this.monitorThread == null) {
			this.monitorThread = new Thread(this);
			this.monitorThread.setDaemon(true);
			this.monitorThread.setPriority(Thread.MIN_PRIORITY);
		}
		this.monitorThread.start();
	}

	/**
	 * Stops monitoring the files that have been added.
	 */
	public void stop() {
		this.shouldRun = false;
	}

	/**
	 * Asks the agent for each file being monitored to check its file for changes.
	 */
	@Override
	public void run() {
		mainloop: while (!Thread.currentThread().isInterrupted() && this.shouldRun) {
			while (!this.deleteStack.empty()) {
				this.removeFile(this.deleteStack.pop());
			}

			// For each entry in the map
			Object fileNames[];
			synchronized (this.monitorMap) {
				fileNames = this.monitorMap.keySet().toArray();
			}
			for (int iterFileNames = 0; iterFileNames < fileNames.length; iterFileNames++) {
				FileName fileName = (FileName) fileNames[iterFileNames];
				FileMonitorAgent agent;
				synchronized (this.monitorMap) {
					agent = this.monitorMap.get(fileName);
				}
				if (agent != null) {
					agent.check();
				}

				if (getChecksPerRun() > 0) {
					if ((iterFileNames % getChecksPerRun()) == 0) {
						try {
							Thread.sleep(getDelay());
						} catch (InterruptedException e) {

						}
					}
				}

				if (Thread.currentThread().isInterrupted() || !this.shouldRun) {
					continue mainloop;
				}
			}

			while (!this.addStack.empty()) {
				this.addFile(this.addStack.pop());
			}

			try {
				Thread.sleep(getDelay());
			} catch (InterruptedException e) {
				continue;
			}
		}

		this.shouldRun = true;
	}

	/**
	 * File monitor agent.
	 */
	private static class FileMonitorAgent {
		private final FileObject file;
		private final GDADataDirectoryMonitor fm;

		private boolean exists;
		private long timestamp;
		private Map<FileName, Object> children = null;

		private FileMonitorAgent(GDADataDirectoryMonitor fm, FileObject file) {
			this.fm = fm;
			this.file = file;

			this.refresh();
			this.resetChildrenList();

			try {
				this.exists = this.file.exists();
			} catch (FileSystemException fse) {
				this.exists = false;
			}

			try {
				this.timestamp = this.file.getContent().getLastModifiedTime();
			} catch (FileSystemException fse) {
				this.timestamp = -1;
			}

		}

		private void resetChildrenList() {
			try {
				if (this.file.getType().hasChildren()) {
					this.children = new HashMap<FileName, Object>();
					FileObject[] childrenList = this.file.getChildren();
					for (int i = 0; i < childrenList.length; i++) {
						this.children.put(childrenList[i].getName(), new Object()); // null?
					}
				}
			} catch (FileSystemException fse) {
				this.children = null;
			}
		}

		/**
		 * Clear the cache and re-request the file object
		 */
		private void refresh() {
			try {
				this.file.refresh();
			} catch (FileSystemException fse) {
				logger.error(fse.getLocalizedMessage(), fse);
			}
		}

		/**
		 * Recursively fires create events for all children if recursive descent is enabled. Otherwise the create event
		 * is only fired for the initial FileObject.
		 */
		private void fireAllCreate(FileObject child) {
			// Add listener so that it can be triggered
			if (this.fm.getFileListener() != null) {
				child.getFileSystem().addListener(child, this.fm.getFileListener());
			}

			((AbstractFileSystem) child.getFileSystem()).fireFileCreated(child);

			// Remove it because a listener is added in the queueAddFile
			if (this.fm.getFileListener() != null) {
				child.getFileSystem().removeListener(child, this.fm.getFileListener());
			}

			this.fm.queueAddFile(child); // Add

			try {

				if (this.fm.isRecursive()) {
					if (child.getType().hasChildren()) {
						FileObject[] newChildren = child.getChildren();
						for (int i = 0; i < newChildren.length; i++) {
							fireAllCreate(newChildren[i]);
						}
					}
				}

			} catch (FileSystemException fse) {
				logger.error(fse.getLocalizedMessage(), fse);
			}
		}

		/**
		 * Only checks for new children. If children are removed, they'll eventually be checked.
		 */
		private void checkForNewChildren() {
			try {
				if (this.file.getType().hasChildren()) {
					FileObject[] newChildren = this.file.getChildren();
					if (this.children != null) {
						// See which new children are not listed in the current children map.
						Map<FileName, Object> newChildrenMap = new HashMap<FileName, Object>();
						Stack<FileObject> missingChildren = new Stack<FileObject>();

						for (int i = 0; i < newChildren.length; i++) {
							newChildrenMap.put(newChildren[i].getName(), new Object()); // null ?
							// If the child's not there
							if (!this.children.containsKey(newChildren[i].getName())) {
								missingChildren.push(newChildren[i]);
							}
						}

						this.children = newChildrenMap;

						// If there were missing children
						if (!missingChildren.empty()) {

							while (!missingChildren.empty()) {
								FileObject child = missingChildren.pop();
								this.fireAllCreate(child);
							}
						}

					} else {
						// First set of children - Break out the cigars
						if (newChildren.length > 0) {
							this.children = new HashMap<FileName, Object>();
						}
						for (int i = 0; i < newChildren.length; i++) {
							this.children.put(newChildren[i].getName(), new Object()); // null?
							this.fireAllCreate(newChildren[i]);
						}
					}
				}
			} catch (FileSystemException fse) {
				logger.error(fse.getLocalizedMessage(), fse);
			}
		}

		private void check() {
			this.refresh();

			try {
				// If the file existed and now doesn't
				if (this.exists && !this.file.exists()) {
					this.exists = this.file.exists();
					this.timestamp = -1;

					// Fire delete event

					((AbstractFileSystem) this.file.getFileSystem()).fireFileDeleted(this.file);

					// Remove listener in case file is re-created. Don't want to fire twice.
					if (this.fm.getFileListener() != null) {
						this.file.getFileSystem().removeListener(this.file, this.fm.getFileListener());
					}

					// Remove from map
					this.fm.queueRemoveFile(this.file);
				} else if (this.exists && this.file.exists()) {

					// Check the timestamp to see if it has been modified
					if (this.timestamp != this.file.getContent().getLastModifiedTime()) {
						this.timestamp = this.file.getContent().getLastModifiedTime();
						// Fire change event

						// Don't fire if it's a folder because new file children
						// and deleted files in a folder have their own event triggered.
						if (!this.file.getType().hasChildren()) {
							((AbstractFileSystem) this.file.getFileSystem()).fireFileChanged(this.file);
						}
					}

				}

				this.checkForNewChildren();

			} catch (FileSystemException fse) {
				logger.error(fse.getLocalizedMessage(), fse);
			}
		}

	}

	public String[] getFilenameExtensions() {
		return filenameExtensions;
	}

	public void setFilenameExtensions(String[] filenameExtensions) {
		this.filenameExtensions = filenameExtensions;
	}

	public String[] getExcludedDirectory() {
		return excludedDirectory;
	}

	public void setExcludedDirectory(String[] excludedDirectory) {
		this.excludedDirectory = excludedDirectory;
	}

	public ArrayList<FileObject> getDataFileCollectedSoFar() {
		return dataFileCollectedSoFar;
	}

	public void setDataFileCollectedSoFar(ArrayList<FileObject> dataFileCollectedSoFar) {
		this.dataFileCollectedSoFar = dataFileCollectedSoFar;
	}

}
