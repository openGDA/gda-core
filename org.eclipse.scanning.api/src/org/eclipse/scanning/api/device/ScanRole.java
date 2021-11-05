package org.eclipse.scanning.api.device;

import org.eclipse.dawnsci.nexus.NexusScanInfo;

/**
 * An enumeration of the roles that a device can have in a scan.
 *
 * @author Matthew Dickie
 */
public enum ScanRole {

	DETECTOR(NexusScanInfo.ScanRole.DETECTOR),
	SCANNABLE(NexusScanInfo.ScanRole.SCANNABLE),
	MONITOR_PER_POINT(NexusScanInfo.ScanRole.MONITOR_PER_POINT),
	MONITOR_PER_SCAN(NexusScanInfo.ScanRole.MONITOR_PER_SCAN);

	private NexusScanInfo.ScanRole nexusScanRole;

	private ScanRole(NexusScanInfo.ScanRole nexusScanRole) {
		this.nexusScanRole = nexusScanRole;
	}

	/**
	 * Get the equivalent constant value in {@link org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole}.
	 * <p>
	 * Implementation note: These two constants exist, one in scanning, one in Nexus, as nexus should
	 * not depend on scanning, and scanning's concept of ScanRole should not depend on nexus.
	 * TODO: is there any sensible way to avoid this duplication?
	 *
	 * @return the equivalent {@link org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole}
	 */
	public NexusScanInfo.ScanRole toNexusScanRole() {
		return nexusScanRole;
	}

}
