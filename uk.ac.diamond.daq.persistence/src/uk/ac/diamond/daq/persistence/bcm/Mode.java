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

package uk.ac.diamond.daq.persistence.bcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;

// TODO report device limits different from stored
// takesnapshot, loadsnapshot
// TODO reconsider safety and activation

/**
 */
@Entity
public class Mode {
	private static final Logger logger = LoggerFactory.getLogger(Mode.class);

	private String name;
	private String description;
	private Snapshot currentSnapshot;
	// scannable name to object reference and limits
	private Map<String, ScannableEntry> scannables = new HashMap<String, ScannableEntry>();
	private boolean disabled = true;
	private static EntityManager em;

	/**
	 * Only to be used by the JPA
	 */
	protected Mode() {
		em = BeamlineConfigurationManager.getEntityManager();
	}

	/**
	 * This is the real constructor. It creates the necessary snapshot too.
	 *
	 * @param name
	 * @param shortDescription
	 */
	protected Mode(String name, String shortDescription) {
		em = BeamlineConfigurationManager.getEntityManager();
		this.name = name;
		this.description = shortDescription;
		currentSnapshot = new Snapshot(name, new Date(), null);
	}

	/**
	 * @return the mode name
	 */
	@Id
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the shortDescription.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The shortDescription to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	protected void restoreAllLimits() throws BcmException {
		for (String sname : scannables.keySet()) {
			try {
				restoreLimits(scannables.get(sname).scannable);
			} catch (DeviceException e) {
				throw new BcmException("Could not set limits on " + sname);
			}
		}
	}

	private void restoreLimits(Scannable s) throws DeviceException {
		if (!(s instanceof ScannableMotion)) {
			// no gda limits for non-motion scannables
			return;
		}
		if (scannables.get(s.getName()) == null) {
			// we don't know about it
			return;
		}
		try {
			((ScannableMotion) s).setLowerGdaLimits(scannables.get(s.getName()).lowerLimit);
			((ScannableMotion) s).setUpperGdaLimits(scannables.get(s.getName()).upperLimit);
		} catch (Exception e) {
			// Hack to make it return deviceException as it should
			throw new DeviceException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Lists the snapshot of current mode
	 *
	 * @return list of snapshots
	 */
	@SuppressWarnings("unchecked")
	protected Collection<Date> getSnapshotList() {

		String qs = "SELECT c.id FROM Snapshot c WHERE c.modename='" + name + "'";
		Query q = em.createNativeQuery(qs);
		List<List<Date>> results = q.getResultList();
		List<Date> shots = new ArrayList<Date>();

		for (List<Date> r : results) {
			shots.add(new BcmDate(r.get(0)));
		}
		return shots;
	}

	/**
	 * load snapshot by id (date)
	 *
	 * @param d
	 * @throws BcmException
	 */
	protected void loadSnapshot(Date d) throws BcmException {
		try {
			loadSnapshot(getSnapshot(d), false);
		} catch (DeviceException e) {
			throw new BcmException(
					"Something is seriously wrong. You should never get a Device exception when not upgrading the snapshot.",
					e);
		}
	}

	/**
	 * load snapshot by id (date)
	 *
	 * @param d
	 * @throws BcmException
	 * @throws DeviceException
	 */
	protected void upgradeSnapshot(Date d) throws BcmException, DeviceException {
		loadSnapshot(getSnapshot(d), true);
	}

	/**
	 *
	 * @param s snapshot
	 * @param upgrade whether to add missing scannables automatically
	 * @throws BcmException
	 * @throws DeviceException
	 */
	private void loadSnapshot(Snapshot s, boolean upgrade) throws BcmException, DeviceException {

		if (disabled)
			throw new BcmException("Action method called on disabled mode " + name);

		if (s == null)
			throw new BcmException("Mode " + name + " cannot switch to snapshot null");

		Snapshot oldSnapshot = currentSnapshot;
		currentSnapshot = s;

		// check the sets of scannables are the same for both mode and snapshot
		if (!scannables.keySet().equals(currentSnapshot.getEntries().keySet())) {
			// not the same
			Set<String> ssn = new TreeSet<String>();
			ssn.addAll(currentSnapshot.getEntries().keySet());

			for (String str : scannables.keySet()) {
				if (!ssn.remove(str)) {
					// scannable not on the snapshots list
					if (upgrade) {
						// FIXME should not modify snapshot
						currentSnapshot
								.addPositionEntry(str, (Double) scannables.get(str).scannable.getPosition(), 0.0);
						Util.printTerm("Warning: Scannable " + str
								+ " was missing from loaded snapshot! Upgraded the snapshot by adding it with its current position.");
					} else {
						currentSnapshot = oldSnapshot;
						throw new BcmException("Cannot load that snapshot: Scannable " + str
								+ " is missing. Consider upgrading.");
					}
				}
			}
			for (String str : ssn) {
				Util.printTerm("Warning: Loaded snapshot has entry for scannable " + str
						+ " which is no longer part of this mode. Entry will be ignored.");
				logger.info("(" + name + ") Extra scannable {} in old snapshot {}.", str, new BcmDate(currentSnapshot
						.getId()));
				// shouldn't hurt to keep it, at least then we get annoyed on every load
				// currentSnapshot.removePositionEntry(str);
			}
		}

		// Display the configuration
		Util.printTerm(currentSnapshot.toString());

		// TODO Instead we could refuse to load the snapshot if it has nompos outside limits
		try {
			// displays scannables that are not at their nominal positions
			reportNonNominalPositions();

		} catch (DeviceException e) {
			logger.error("There was a problem reading motor positions", e);
		}

	}

	/**
	 * update the set of limits stored for this mode using the ones currently set on the scannables
	 */
	protected void saveLimits() {
		for (String sname : scannables.keySet()) {
			if (scannables.get(sname).scannable instanceof ScannableMotion) {
				scannables.get(sname).lowerLimit = Util
						.getLowerLimit((ScannableMotion) scannables.get(sname).scannable);
				scannables.get(sname).upperLimit = Util
						.getUpperLimit((ScannableMotion) scannables.get(sname).scannable);
			} else {
				scannables.get(sname).lowerLimit = null;
				scannables.get(sname).upperLimit = null;
			}
		}
	}

	/**
	 * @return id of the new snapshot
	 * @throws BcmException
	 */
	protected Date takeSnapshot() throws BcmException {

		if (!isSafe()) {
			throw new BcmException("Some mode limits are violated, refusing to take snapshot");
		}

		Snapshot cheese = new Snapshot(name, new Date(), currentSnapshot.getId());

		// FIXME this only works with scannables that deliver stuff that can
		// be casted to Double and has only a single input value
		for (String sname : scannables.keySet()) {
			try {
				// Update nompos
				cheese.addPositionEntry(sname, (Double) scannables.get(sname).scannable.getPosition(), 0.0);
			} catch (DeviceException e) {
				throw new BcmException("Could not get position for " + sname + " while taking snapshot");
			}
		}

		currentSnapshot = cheese;
		return new BcmDate(currentSnapshot.getId());
	}

	/**
	 * set the label (tag) of the currently loaded snapshot
	 *
	 * @param tag
	 */
	protected void tagSnapshot(String tag) {
		currentSnapshot.setTag(tag);
	}

	/**
	 * Prints out a list of violated limits to the terminal.
	 *
	 * @throws DeviceException
	 */
	public void reportViolatedLimits() throws DeviceException {

		Map<String, Double> tm = new TreeMap<String, Double>();

		for (String sname : scannables.keySet()) {
			Double p = (Double) scannables.get(sname).scannable.getPosition();

			if (scannables.get(sname).upperLimit != null && p > scannables.get(sname).upperLimit) {
				tm.put(sname, p);
				continue;
			}
			if (scannables.get(sname).lowerLimit != null && p < scannables.get(sname).lowerLimit) {
				tm.put(sname, p);
				continue;
			}
		}

		if (tm.size() == 0) {
			Util.printTerm("All scannables within their limits");
			return;
		}

		String toPrint = "These scannables violate their limits:\n";

		for (String sname : tm.keySet()) {
			String ll = (scannables.get(sname).lowerLimit != null) ? scannables.get(sname).lowerLimit.toString() : "";
			String ul = (scannables.get(sname).upperLimit != null) ? scannables.get(sname).upperLimit.toString() : "";

			toPrint += String.format("%s\t pos: %f nom:%f lim(%s : %s)\n", sname, tm.get(sname), currentSnapshot
					.getEntries().get(sname).position, ll, ul);
		}

		Util.printTerm(toPrint);

		return;
	}

	/**
	 * Prints a list of motors in non-nominal positions to the terminal
	 *
	 * @throws DeviceException
	 */
	public void reportNonNominalPositions() throws DeviceException {

		Set<String> ts = new TreeSet<String>();

		for (String sname : scannables.keySet()) {
			if (currentSnapshot.getEntries().get(sname).position == null) {
				Util.printTerm(sname + "has no nominal position in the current snapshot");
				continue;
			}
			if (!scannables.get(sname).scannable.isAt(currentSnapshot.getEntries().get(sname).position)) {
				ts.add(sname);
			}
		}

		if (ts.size() == 0) {
			Util.printTerm("All scannables at their nominal positions");
			return;
		}

		String toPrint = "These scannables are not at their nominal positions:\n";

		for (String sname : ts) {
			String ll = (scannables.get(sname).lowerLimit != null) ? scannables.get(sname).lowerLimit.toString() : "";
			String ul = (scannables.get(sname).upperLimit != null) ? scannables.get(sname).upperLimit.toString() : "";

			toPrint += String.format("%s\t pos: %f nom:%f lim(%s : %s)\n", sname, scannables.get(sname).scannable
					.getPosition(), currentSnapshot.getEntries().get(sname).position, ll, ul);
		}

		Util.printTerm(toPrint);

		return;
	}

	/**
	 * @param scannableList
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveToNominalPosition(Scannable... scannableList) throws DeviceException, InterruptedException {

		Map<String, Scannable> busyList = new TreeMap<String, Scannable>();

		for (Scannable scannable : scannableList) {
			if (scannables.get(scannable.getName()) == null) {
				// we don't know about it
				continue;
			}
			if (scannable.isAt(currentSnapshot.getEntries().get(scannable.getName()).position)) {
				// don't move unnecessarily
				continue;
			}
			busyList.put(scannable.getName(), scannable);
			scannable.asynchronousMoveTo(currentSnapshot.getEntries().get(scannable.getName()).position);
		}

		// wait to finish
		for (String sname : busyList.keySet()) {
			while (busyList.get(sname).isBusy())
				try {
					Thread.sleep(175);
				} catch (InterruptedException e) {
					logger.error("Interrupted waiting for moves to finish", e);
					throw e;
				}
			Util.printTerm(sname + " reached " + busyList.get(sname).getPosition());
		}
	}

	protected void moveAllToNominalPosition() throws DeviceException {
		Set<Scannable> sset = new HashSet<Scannable>();
		for (ScannableEntry se : scannables.values()) {
			sset.add(se.scannable);
		}
		try {
			moveToNominalPosition(sset.toArray(new Scannable[0]));
		} catch (InterruptedException e) {
			String msg = "Thread interrupted while waiting for move to complete";
			logger.error(msg, e);
			Thread.currentThread().interrupt();
			throw new DeviceException(msg, e);
		}
	}

	/**
	 * @return current snapshot
	 */
	@OneToOne(cascade = CascadeType.ALL)
	Snapshot getCurrentSnapshot() {
		return currentSnapshot;
	}

	/**
	 * @param currentSnapshot
	 */
	void setCurrentSnapshot(Snapshot currentSnapshot) {
		this.currentSnapshot = currentSnapshot;
	}

	/**
	 * @return map of scannables under control
	 */
	@OneToMany(cascade = CascadeType.PERSIST)
	@MapKey(name = "name")
	Map<String, ScannableEntry> getScannables() {
		return scannables;
	}

	/**
	 * @param sc
	 * @throws BcmException
	 */
	void setScannables(Map<String, ScannableEntry> sc) throws BcmException {
		scannables = sc;
		for (String sname : scannables.keySet()) {
			scannables.get(sname).scannable = Util.getScannableFromJython(sname);
		}
	}

	/**
	 * Add a scannable to this mode's control
	 *
	 * @param scannable
	 * @throws BcmException
	 */
	protected void addScannable(Scannable scannable) throws BcmException {

		if (!isSafe()) {
			throw new BcmException("Some mode limits are violated, refusing to take action.");
		}

		String sname = scannable.getName();
		// test everything is fine
		if (!Util.isScannableNameValid(scannable))
			throw new BcmException("cannot get your scannable from the namespace by its name: " + sname);

		// add to scannables
		if (scannable instanceof ScannableMotion) {
			scannables.put(scannable.getName(), new ScannableEntry(scannable.getName(), scannable, Util
					.getLowerLimit((ScannableMotion) scannable), Util.getUpperLimit((ScannableMotion) scannable)));
		} else {
			scannables.put(scannable.getName(), new ScannableEntry(scannable.getName(), scannable, null, null));
		}
		// take new snapshot with everything in it
		takeSnapshot();
	}

	protected void removeScannable(Scannable s) {
		scannables.remove(s.getName());
		currentSnapshot.removePositionEntry(s.getName());
	}

	/**
	 * Finds the Snapshot with the smallest id (timestamp) bigger or equal to the the one handed in
	 *
	 * @param id
	 *            date
	 * @return s snapshot object
	 * @throws BcmException
	 */
	@SuppressWarnings("unchecked")
	private Snapshot getSnapshot(Date id) throws BcmException {
		String qs = "SELECT MIN(s.id) FROM Snapshot s WHERE s.modeName = '" + name + "' AND s.id >= ? ";

		Query q = em.createNativeQuery(qs);
		q.setParameter(1, id, TemporalType.TIMESTAMP);

		// For reasons unknown getSingleResult retruns a vector with one item
		Date exactid = ((Vector<Date>) q.getSingleResult()).firstElement();

		// this shouldn't happen but getSingleResult seem to be broken
		if (exactid == null)
			throw new BcmException("Could not find a corresponding snapshot");

		Snapshot s = em.find(Snapshot.class, exactid);
		return s;
	}

	/**
	 * set to active state (i.e. load snapshot and do necessary checks)
	 *
	 * @throws BcmException
	 */
	protected void activate() throws BcmException {
		/*
		 * check sanity all scannables in snapshot and vice versa all nompos with scannables limits all current
		 * positions within current limits
		 */

		disabled = false;
		try {
			for (String sname : scannables.keySet()) {
				// the method in Util guarantees it works, or throws a BcmException
				scannables.get(sname).scannable = Util.getScannableFromJython(sname);
				if (!Util.isScannableNameValid(scannables.get(sname).scannable))
					throw new BcmException("scannable " + sname + "in jythonnamespace is improperly defined.");
			}

			restoreAllLimits();

			// displays scannables that are outside limits
			reportViolatedLimits();

		} catch (Exception e) {
			disabled = true;
			throw new BcmException("Exception activating mode " + name, e);
		}
		try {
			loadSnapshot(currentSnapshot, false);
		} catch (DeviceException e) {
			// does never occur as long as upgrade=false (and no strange changes to the code)
		}
	}

	/**
	 * set mode to disabled. This is a protected method called by the beamline configuration manager when unloading the
	 * Mode. This allows us to leak the mode object into userspace for easier operation and still disable it's use. If
	 * the implementation no longer requires that leak, this scheme could be removed.
	 */
	protected void disable() {
		disabled = true;
	}

	/**
	 * @return boolean
	 */
	public boolean isActive() {
		return !disabled;
	}

	// TODO revise safety
	/**
	 * @return boolean
	 */
	public boolean isSafe() {
		if (disabled)
			return false;

		// check everything within limits
		for (String sname : scannables.keySet()) {
			Double p;
			try {
				p = (Double) scannables.get(sname).scannable.getPosition();
			} catch (DeviceException e) {
				Util.printTerm("Exception querying " + sname);
				return false;
			}
			if (scannables.get(sname).upperLimit != null && p > scannables.get(sname).upperLimit) {
				return false;
			}
			if (scannables.get(sname).lowerLimit != null && p < scannables.get(sname).lowerLimit) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "mode " + name + (disabled ? "" : " (active)");
	}

	protected void purgeSnapshots() {
		long date = 0;
		Snapshot s;

		/*
		 * starting 1970 give the next snapshot and delete it, skipping the current one current one should be deleted by
		 * JPA cascade
		 */
		em.getTransaction().begin();
		try {
			while (true) {
				s = getSnapshot(new Date(date));

				if (s != currentSnapshot) {
					em.remove(s);
				} else {
					date = currentSnapshot.getId().getTime() + 1;
				}
			}
		} catch (BcmException e) {
			// no more snapshots except the current
		}
		em.getTransaction().commit();
	}

}
