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

package gda.server.beamlineConfigurationManager;

// TODO improve reporting snapshots: by tag, time(range)

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Localizable;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.persistence.LocalDatabase.LocalDatabaseException;
import gda.util.persistence.LocalObjectShelf;
import gda.util.persistence.LocalObjectShelfManager;
import gda.util.persistence.LocalPersistence;
import gda.util.persistence.ObjectShelfException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>Beamline Configuration Manager</b>
 * <p>
 * The 'beamline configuration manager' provides an easy way to save, load switch between different beamline modes.
 * <p>
 * This controller might be used to store a number of successful configurations or to save a number of interim snapshots
 * throughout the day that can be easily returned to. It is not currently designed to automatically store the beamline
 * state after each move command. No GUI is available today, but after it has been used in practice this could be added
 * on request.
 * <p>
 * Can only be instantiated using server.xml file.
 */
public class BeamlineConfigurationManager implements Findable, Configurable, Localizable, IObservable {
	private static final Logger logger = LoggerFactory.getLogger(BeamlineConfigurationManager.class);
	private static EntityManager em;
	private boolean local = true;
	private String name = "";
	private Map<String, Mode> activeModes = new HashMap<String, Mode>();
	private LocalObjectShelf bcmShelve;
	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * format for parsing and outputting snapshot ids
	 */
	public static final String dateFormat = "yyyyMMdd HH:mm:ss.SSS";

	/**
	 * @throws BcmException
	 */
	public BeamlineConfigurationManager() throws BcmException {
		// ensure singleton?
		if (em != null) {
			// we have been initialized before!
			throw new BcmException("please get my instance off the finder.");
		}
	}

	/**
	 * {@inheritDoc} Reads from LocalParameters the last mode used on this beamline, and switches to it. If last mode
	 * cannot be determined, then disables configuration management.
	 *
	 * @see gda.factory.Configurable#configure()
	 */
	@Override
	public void configure() throws FactoryException {

		try {
			em = LocalPersistence.createPersistenceEntityManagerFactory("BcmPersistenceUnit").createEntityManager();
		} catch (LocalDatabaseException e) {
			throw new FactoryException(e.getMessage());
		}

		try {
			bcmShelve = LocalObjectShelfManager.open("gda.util.bcm");
			String[] lastUsedModesList = (String[]) bcmShelve.getValue("lastusedmodes", null); // A list of modes
			if (lastUsedModesList != null) {
				for (String modeName : lastUsedModesList) {
					loadMode(modeName);
				}
			} else {
				String msg = "<< the beamline configuration manager did not load any modes as none were set when the gda was last stopped. >>";
				logger.info(msg);
				//Removed as this stops the GDA from running up in the spring configuration
				//Util.printTerm(msg);
			}
		} catch (ObjectShelfException e) {
			throw new FactoryException(e.getMessage());
		} catch (LocalDatabaseException e) {
			throw new FactoryException(e.getMessage());
		} catch (BcmException e) {
			throw new FactoryException(e.getMessage());
		}
	}

	/**
	 * remove specified mode from the active set
	 *
	 * @param modeName
	 * @throws ObjectShelfException
	 */
	public void ejectMode(String modeName) throws ObjectShelfException {

		if (activeModes.get(modeName) == null) {
			Util.printTerm("Mode " + modeName + " not currently loaded here.");
			return;
		}

		activeModes.get(modeName).disable();
		activeModes.remove(modeName);
		Util.printTerm("Mode " + modeName + " unloaded.");
		bcmShelve.addValue("lastusedmodes", activeModes.keySet().toArray(new String[0]));
	}

	/**
	 * Default change-mode method. Changes mode and switches to the last configuration used in this mode. If last
	 * configuration is not known then switch to this mode and then turn off configuration control.
	 *
	 * @param newModeName
	 *            name of mode to switch to
	 * @throws BcmException
	 * @throws ObjectShelfException
	 */
	public void loadMode(String newModeName) throws BcmException, ObjectShelfException {

		if (activeModes.get(newModeName) != null) {
			Util.printTerm("Mode " + newModeName + " already loaded.");
			return;
		}

		Mode nm = getMode(newModeName);

		// Stop if mode is not in database
		if (null == nm) {
			Util.printTerm(String.format("Could not enable mode=%s: not found", newModeName));
			return;
		}

		// check there are no duplicate scannables
		Set<String> newScannables = new HashSet<String>();
		newScannables.addAll(nm.getScannables().keySet());
		if (newScannables.removeAll(getAllScannableNames())) {
			Set<String> offendingScannables = new TreeSet<String>();
			offendingScannables.addAll(nm.getScannables().keySet());
			offendingScannables.removeAll(newScannables);

			String list = "";
			for (String s : offendingScannables)
				list += s + " ";
			Util.printTerm("The following scannables are members of existing modes: " + list);
			throw new BcmException("cannot load mode, conflicting scannables: ", offendingScannables);
		}

		nm.activate();
		// Change mode
		activeModes.put(newModeName, nm);

		String msg = "<< beamline mode " + newModeName + " activated with snapshot "
				+ new BcmDate(nm.getCurrentSnapshot().getId()) + ">>";
		logger.info(msg);
		Util.printTerm(msg);

		bcmShelve.addValue("lastusedmodes", activeModes.keySet().toArray(new String[0]));
	}

	/**
	 * Add scannable to named mode's control
	 *
	 * @param modeName
	 * @param scannable
	 * @throws BcmException
	 */
	public void addScannable(String modeName, Scannable scannable) throws BcmException {

		if (activeModes.get(modeName) == null) {
			Util.printTerm("Cannot add scannables to inactive mode " + modeName);
			throw new BcmException("cannot add scannable to inactive mode");
		}

		if (getAllScannableNames().contains(scannable.getName())) {
			Util.printTerm("The scannables is members of loaded modes: ");
			throw new BcmException("scannable conflicts with active modes, cannot add");
		}

		activeModes.get(modeName).addScannable(scannable);

		em.getTransaction().begin();
		em.merge(activeModes.get(modeName));
		em.getTransaction().commit();
	}

	/**
	 * get a collection of scannable names that are controlled by the active modes
	 *
	 * @return set of scannable name
	 */
	public Collection<String> getAllScannableNames() {
		Set<String> allScannables = new HashSet<String>();

		for (Mode m : activeModes.values()) {
			allScannables.addAll(m.getScannables().keySet());
		}
		return allScannables;
	}

	/**
	 * get a collection of scannable names that are controlled by an active mode
	 * @param modeName
	 * @return set of scannable name
	 * @throws BcmException
	 */
	public Collection<String> getAllScannableNames(String modeName) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + " is not active.");

		Set<String> allScannables = new HashSet<String>();

		allScannables.addAll(activeModes.get(modeName).getScannables().keySet());

		return allScannables;
	}
	/**
	 * see that all active modes are properly activated and safe
	 *
	 * @return okness
	 */
	public boolean okayToOpenShutter() {
		for (Mode m : activeModes.values()) {
			if (!m.isActive() && !m.isSafe())
				return false;
		}
		return true;
	}


	/**
	 * Creates a new mode entity in table, and loads it
	 *
	 * @param mname
	 *            must not exist
	 * @param shortDescription
	 *            for showing in client
	 * @throws BcmException
	 *             if mode exists
	 * @throws ObjectShelfException
	 */
	public void addMode(String mname, String shortDescription) throws BcmException, ObjectShelfException {
		if (getMode(mname) == null) {

			em.getTransaction().begin();
			em.persist(new Mode(mname, shortDescription));
			em.getTransaction().commit();

			logger.info("Added new mode " + mname + "=" + shortDescription);
		} else {
			throw new BcmException("Could not make new mode: a mode with name " + mname + " exists already");
		}

		loadMode(mname);
	}

	/**
	 * @param scannableList
	 * @throws DeviceException
	 */
	public void moveToNominalPosition(Scannable... scannableList) throws DeviceException {
		for (Mode m : activeModes.values()) {
			Util.printTerm("Mode: " + m.getName());
			try {
				m.moveToNominalPosition(scannableList);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				String msg = getName() + "Thread interrupted while moving motors to nominal position";
				logger.error(msg);
				throw new DeviceException(msg, e);
			}
		}
		Util.printTerm("done");
	}

	/**
	 * @throws DeviceException
	 */
	public void moveAllToNominalPosition() throws DeviceException {
		for (Mode m : activeModes.values()) {
			Util.printTerm("Mode: " + m.getName());
			m.moveAllToNominalPosition();
		}
		Util.printTerm("done");
	}

	/**
	 * @param modeName
	 * @param tag
	 * @throws BcmException
	 */
	public void tagSnapshot(String modeName, String tag) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + " is not active.");
		activeModes.get(modeName).tagSnapshot(tag);
	}

	/**
	 * @param modeName
	 * @return id of the new snapshot
	 * @throws BcmException
	 */
	public Date takeSnapshot(String modeName) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + " is not active.");
		Date retval = activeModes.get(modeName).takeSnapshot();
		em.getTransaction().begin();
		em.merge(activeModes.get(modeName));
		em.getTransaction().commit();
		return retval;
	}

	private Mode getMode(String modeName) {
		Mode mode = em.find(Mode.class, modeName);
		return mode;
	}

	/**
	 * @param modeName
	 * @param dateStr
	 * @throws BcmException
	 * @throws DeviceException
	 */
	public void upgradeSnapshot(String modeName, String dateStr) throws BcmException, DeviceException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + " is not active.");

		try {
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat.substring(0, dateStr.length()));
			Date id = formatter.parse(dateStr);
			activeModes.get(modeName).upgradeSnapshot(id);
		} catch (ParseException e) {
			throw new BcmException("could not parse your date " + dateStr);
		}
	}

	/**
	 * @param modeName
	 * @param id
	 * @throws BcmException
	 * @throws DeviceException
	 */
	public void upgradeSnapshot(String modeName, Date id) throws BcmException, DeviceException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + " is not active.");
		activeModes.get(modeName).upgradeSnapshot(id);
	}

	/**
	 * @param modeName
	 * @param dateStr
	 * @throws BcmException
	 */
	public void loadSnapshot(String modeName, String dateStr) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + " is not active.");

		try {
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat.substring(0, dateStr.length()));
			Date id = formatter.parse(dateStr);
			activeModes.get(modeName).loadSnapshot(id);
		} catch (ParseException e) {
			throw new BcmException("could not parse your date " + dateStr);
		}
	}

	/**
	 * @param modeName
	 * @param id
	 * @throws BcmException
	 */
	public void loadSnapshot(String modeName, Date id) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + " is not active.");
		activeModes.get(modeName).loadSnapshot(id);
	}

	/**
	 * @throws DeviceException
	 */
	public void status() throws DeviceException {
		if (activeModes.size() == 0) {
			Util.printTerm("Idle. No Modes active.");
			return;
		}
		for (String mname : activeModes.keySet()) {
			Util.printTerm("Mode " + mname);
			activeModes.get(mname).reportNonNominalPositions();
			activeModes.get(mname).reportViolatedLimits();
		}
	}

	/**
	 * @param modeName
	 * @throws BcmException
	 */
	public void updateLimits(String modeName) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + "is not active.");
		activeModes.get(modeName).saveLimits();
	}

	/**
	 * @param modeName
	 * @param s
	 * @throws BcmException
	 */
	public void removeScannable(String modeName, Scannable s) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + "is not active.");
		activeModes.get(modeName).removeScannable(s);
	}

	/**
	 * @param modeName
	 * @throws BcmException
	 */
	public void restoreLimits(String modeName) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + "is not active.");
		activeModes.get(modeName).restoreAllLimits();
	}

	/**
	 * @param modeName
	 * @throws BcmException
	 */
	public void saveLimits(String modeName) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + "is not active.");
		activeModes.get(modeName).saveLimits();
	}

	/**
	 * Deletes a mode entity. Mode entity must have no associated configurations.
	 *
	 * @param modeName
	 *            name of mode to delete
	 * @throws BcmException
	 *             if mode does not exist
	 */
	public void removeMode(String modeName) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) != null)
			throw new BcmException("Mode " + modeName + "is active.");
		if (getMode(modeName) == null)
			throw new BcmException("Could not delete mode: no mode with name " + modeName + " exists");

		// delete the mode
		Mode mode = em.find(Mode.class, modeName);
		mode.purgeSnapshots();
		em.getTransaction().begin();
		em.remove(mode);
		em.getTransaction().commit();

		logger.info("Deleted mode " + modeName);
	}

	/**
	 * Gets the names of all modes.
	 *
	 * @return a list active mode names
	 */
	public Collection<String> getActiveModeList() {

		List<String> modes = new ArrayList<String>();

		modes.addAll(activeModes.keySet());

		return modes;
	}

	/**
	 * Gets the names of all modes.
	 *
	 * @return a list of mode name
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> getModeList() {

		Query q = em.createNativeQuery("SELECT name FROM Mode");

		List<List<String>> results = q.getResultList();
		List<String> modes = new ArrayList<String>();

		for (List<String> r : results) {
			modes.add(r.get(0));
		}
		return modes;
	}

	/**
	 * Gets the names of the snapshots in a mode.
	 *
	 * @param modeName
	 * @return a list of snapshot ids
	 * @throws BcmException
	 */
	public Collection<Date> getSnapshots(String modeName) throws BcmException {
		if (modeName == null)
			throw new BcmException("mode name null pointer.");
		if (activeModes.get(modeName) == null)
			throw new BcmException("Mode " + modeName + "is not active.");
		return activeModes.get(modeName).getSnapshotList();
	}

	/**
	 * @return the EntityManager
	 */
	static EntityManager getEntityManager() {
		return em;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

	// Implements Localizable interface.
	@Override
	public boolean isLocal() {
		return local;
	}
	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	// Implement Findable interface.
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
}
