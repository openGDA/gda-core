/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.event.scan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;


/**
 * This bean is used to disseminate messages about what has happened
 * to the scan while it is being written.
 * <p>
 * Do not extend this class to allow arbitrary information to be sent.
 * The event encapsulated by this bean should be sending just the information
 * defined here, metadata that cannot circumvent the nexus file.
 * <p>
 * For instance adding a dynamic set of information, a map perhaps, would
 * allow information which should be saved in the Nexus file to circumvent
 * the file and be set in the event. It was decided in various meetings
 * that doing this could mean that some data is not recorded as it should be
 * in nexus. Therefore these events are simply designed to contain events
 * not data. They are not the same as the old ScanDataPoint system in GDA
 *
 * @author Matthew Gerring
 *
 */
public final class ScanBean extends StatusBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 8191863784268392626L;

	// Field required to start a scan, may be null.
	private ScanRequest scanRequest;

	// General Information
	private String  deviceName;
	private String  beamline;

	// Where are we in the scan
	private int point;
	private int size;
	private IPosition position;

	// Dataset information
	private String  filePath;
	private String  datasetPath;
	private int     scanNumber;

	public ScanBean() {
        super();
	}

	public ScanBean(ScanRequest req) {
        super();
        this.scanRequest = req;
        this.status = Status.SUBMITTED;
		setHostName(getLocalHostName());
		setName(createNameFromRequest(req));
	}

	private String getLocalHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "<error>";
		}
	}

	private String createNameFromRequest(ScanRequest req) {

		String sname = "Scan";
		if (req.getSampleData()!=null               &&
			req.getSampleData().getName()!=null     &&
			req.getSampleData().getName().length()>0) {

			sname = req.getSampleData().getName();
		}

		StringBuilder buf = new StringBuilder();
		buf.append(sname);
		buf.append(" [");
		for (Iterator<Object> it = req.getCompoundModel().getModels().iterator(); it.hasNext();) {
			Object model = it.next();
			if (model instanceof AbstractPointsModel) {
				buf.append(((AbstractPointsModel)model).getSummary());
			} else {
				buf.append(model);
			}
			if (it.hasNext()) buf.append(", ");
		}

		if (req.getDetectors()==null || req.getDetectors().isEmpty()) {
			buf.append("] ");

		} else {
			buf.append("] with Detectors [");
			for (Iterator<String> it = req.getDetectors().keySet().iterator(); it.hasNext();) {
				String name = it.next();
				buf.append(name);
				if (it.hasNext()) buf.append(", ");
			}
			buf.append("] ");
		}

	    return buf.toString();

	}

	public ScanBean(ScanBean scanBean) {
		merge(scanBean);
	}

	@Override
	public <T extends IdBean> void merge(T with) {
		super.merge(with);
		final ScanBean scanBean = (ScanBean) with;
		this.scanRequest = scanBean.scanRequest;
		this.deviceName = scanBean.deviceName;
		this.beamline = scanBean.beamline;
		this.point = scanBean.point;
		this.size = scanBean.size;
		this.position = scanBean.position;
		this.filePath = scanBean.filePath;
		this.datasetPath = scanBean.datasetPath;
		this.scanNumber = scanBean.scanNumber;
	}

	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getScanNumber() {
		return scanNumber;
	}
	public void setScanNumber(int scanNumber) {
		this.scanNumber = scanNumber;
	}

	public String getDatasetPath() {
		return datasetPath;
	}
	public void setDatasetPath(String datasetPath) {
		this.datasetPath = datasetPath;
	}

	public String getBeamline() {
		return beamline;
	}

	public void setBeamline(String beamline) {
		this.beamline = beamline;
	}

	@Override
	public String toString() {
		return "ScanBean [deviceName=" + deviceName
				+ ", beamline=" + beamline
				+ ", point=" + point
				+ ", size=" + size
				+ ", position=" + position
				+ ", filePath=" + filePath
				+ ", scanNumber=" + scanNumber
				+ ", datasetPath=" + datasetPath
				+ " "+super.toString()+"]";
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int frame) {
		this.point = frame;
	}

	/**
	 * NOTE the size is the approximate size of the scan
	 * by using the iterators. It is legal to do logic
	 * in an iterator - this means that the overall size
	 * is not constant for some custom scan types! However
	 * for the vast majority of linear scans size and shape
	 * are constant.
	 *
	 */
	public int getSize() {
		return size;
	}

	/**
	 * NOTE the size is the approximate size of the scan
	 * by using the iterators. It is legal to do logic
	 * in an iterator - this means that the overall size
	 * is not constant for some custom scan types! However
	 * for the vast majority of linear scans size and shape
	 * are constant.
	 *
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}

	public IPosition getPosition() {
		return position;
	}

	public void setPosition(IPosition value) {
		this.position = value;
	}

	public void putPosition(String name, int index, Object val) {
		IPosition tmp = new MapPosition(name, index, val);
		this.position = tmp.compound(position);
	}

	/**
	 * @return whether the scan has just started, i.e. transitioned from a {@link Status#PREPARING} state
	 * 		to a {@link Status#RUNNING} state.
	 */
	public boolean scanStart() {
		return Status.PREPARING == previousStatus && Status.RUNNING == status;
	}

	/**
	 * @return whether the scan has just ended, i.e. transitioned from a RUNNING/RESUMED state to a post-running state.
	 */
	public boolean scanEnd() {
		if (previousStatus == null || status == null) return false;
		return previousStatus.isRunning() && status.isFinal();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((beamline == null) ? 0 : beamline.hashCode());
		result = prime * result + ((datasetPath == null) ? 0 : datasetPath.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + point;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + scanNumber;
		result = prime * result + ((scanRequest == null) ? 0 : scanRequest.hashCode());
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanBean other = (ScanBean) obj;
		if (beamline == null) {
			if (other.beamline != null)
				return false;
		} else if (!beamline.equals(other.beamline))
			return false;
		if (datasetPath == null) {
			if (other.datasetPath != null)
				return false;
		} else if (!datasetPath.equals(other.datasetPath))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (point != other.point)
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (scanNumber != other.scanNumber)
			return false;
		if (scanRequest == null) {
			if (other.scanRequest != null)
				return false;
		} else if (!scanRequest.equals(other.scanRequest))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	public ScanRequest getScanRequest() {
		return scanRequest;
	}

	public void setScanRequest(ScanRequest scanRequest) {
		this.scanRequest = scanRequest;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
}