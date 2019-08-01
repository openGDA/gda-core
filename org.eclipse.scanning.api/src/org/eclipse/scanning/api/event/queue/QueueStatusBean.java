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
package org.eclipse.scanning.api.event.queue;

import java.util.UUID;

public class QueueStatusBean {

	/**
	 * The unique id of the job queue.
	 */
	private UUID queueId;

	/**
	 * The name of the queue.
	 */
	private String submissionQueueName;

	/**
	 * Beamline that the acquisition server is controlling
	 */
	private String beamline;

	/**
	 * Time that the bean was published.
	 */
	private long publishTime;

	/**
	 * Time that the job queue's consumer thread started.
	 */
	private long startTime;

	/**
	 * Provides the job queue name, may be null.
	 */
	private String jobQueueName;

	/**
	 * The status of the queue, a {@link QueueStatus} enum value.
	 */
	private QueueStatus queueStatus;

	/**
	 * The name of the machine that the consumer is running on.
	 */
	private String hostName;

	public UUID getQueueId() {
		return queueId;
	}

	public void setQueueId(UUID queueId) {
		this.queueId = queueId;
	}

	public String getQueueName() {
		return submissionQueueName;
	}

	public void setQueueName(String queueName) {
		this.submissionQueueName = queueName;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(long publishTime) {
		this.publishTime = publishTime;
	}

	public String getBeamline() {
		return beamline;
	}

	public void setBeamline(String beamline) {
		this.beamline = beamline;
	}

	public String getJobQueueName() {
		return jobQueueName;
	}

	public void setJobQueueName(String jobQueueName) {
		this.jobQueueName = jobQueueName;
	}

	public QueueStatus getJobQueueStatus() {
		return queueStatus;
	}

	public void setQueueStatus(QueueStatus queueStatus) {
		this.queueStatus = queueStatus;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	@Override
	public String toString() {
		return "QueueStatusBean [" + "queueName=" + submissionQueueName +
				"beamline=" + beamline + ", publishTime=" + publishTime +
				", conceptionTime=" + startTime + ", consumerId=" + queueId +
				", queueStatus=" + queueStatus + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beamline == null) ? 0 : beamline.hashCode());
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		result = prime * result + ((queueId == null) ? 0 : queueId.hashCode());
		result = prime * result + ((jobQueueName == null) ? 0 : jobQueueName.hashCode());
		result = prime * result + ((queueStatus == null) ? 0 : queueStatus.hashCode());
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + (int) (publishTime ^ (publishTime >>> 32));
		result = prime * result + ((submissionQueueName == null) ? 0 : submissionQueueName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueueStatusBean other = (QueueStatusBean) obj;
		if (beamline == null) {
			if (other.beamline != null)
				return false;
		} else if (!beamline.equals(other.beamline))
			return false;
		if (startTime != other.startTime)
			return false;
		if (queueId == null) {
			if (other.queueId != null)
				return false;
		} else if (!queueId.equals(other.queueId))
			return false;
		if (jobQueueName == null) {
			if (other.jobQueueName != null)
				return false;
		} else if (!jobQueueName.equals(other.jobQueueName))
			return false;
		if (queueStatus != other.queueStatus)
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (publishTime != other.publishTime)
			return false;
		if (submissionQueueName == null) {
			if (other.submissionQueueName != null)
				return false;
		} else if (!submissionQueueName.equals(other.submissionQueueName))
			return false;
		return true;
	}

}
