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
package org.eclipse.scanning.api.event.consumer;

import java.util.UUID;

public class ConsumerStatusBean {

	/**
	 * The id of the consumer.
	 */
	private UUID consumerId;

	/**
	 * The name of the queue read by the consumer.
	 */
	private String queueName;

	/**
	 * Beamline that the acquisition server is controlling
	 */
	private String beamline;

	/**
	 * Time that the bean was published by the consumer.
	 */
	private long publishTime;

	/**
	 * Time that the consumer started.
	 */
	private long startTime;

	/**
	 * Provides the consumer name, may be null.
	 */
	private String consumerName;

	/**
	 * The status of the consumer, a {@link ConsumerStatus} enum value.
	 */
	private ConsumerStatus consumerStatus;

	/**
	 * The name of the machine that the consumer is running on.
	 */
	private String hostName;

	public UUID getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(UUID consumerId) {
		this.consumerId = consumerId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
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

	public String getConsumerName() {
		return consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	public ConsumerStatus getConsumerStatus() {
		return consumerStatus;
	}

	public void setConsumerStatus(ConsumerStatus consumerStatus) {
		this.consumerStatus = consumerStatus;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	@Override
	public String toString() {
		return "ConsumerStatusBean [" + "queueName=" + queueName +
				"beamline=" + beamline + ", publishTime=" + publishTime +
				", conceptionTime=" + startTime + ", consumerId=" + consumerId +
				", consumerStatus=" + consumerStatus + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beamline == null) ? 0 : beamline.hashCode());
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		result = prime * result + ((consumerId == null) ? 0 : consumerId.hashCode());
		result = prime * result + ((consumerName == null) ? 0 : consumerName.hashCode());
		result = prime * result + ((consumerStatus == null) ? 0 : consumerStatus.hashCode());
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + (int) (publishTime ^ (publishTime >>> 32));
		result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
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
		ConsumerStatusBean other = (ConsumerStatusBean) obj;
		if (beamline == null) {
			if (other.beamline != null)
				return false;
		} else if (!beamline.equals(other.beamline))
			return false;
		if (startTime != other.startTime)
			return false;
		if (consumerId == null) {
			if (other.consumerId != null)
				return false;
		} else if (!consumerId.equals(other.consumerId))
			return false;
		if (consumerName == null) {
			if (other.consumerName != null)
				return false;
		} else if (!consumerName.equals(other.consumerName))
			return false;
		if (consumerStatus != other.consumerStatus)
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (publishTime != other.publishTime)
			return false;
		if (queueName == null) {
			if (other.queueName != null)
				return false;
		} else if (!queueName.equals(other.queueName))
			return false;
		return true;
	}

}
