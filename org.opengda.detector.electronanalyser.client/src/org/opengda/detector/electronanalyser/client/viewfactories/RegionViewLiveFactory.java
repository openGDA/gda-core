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

package org.opengda.detector.electronanalyser.client.viewfactories;

import org.eclipse.core.runtime.CoreException;
import org.opengda.detector.electronanalyser.client.views.RegionViewLive;

import gda.device.Scannable;
import gda.rcp.views.FindableExecutableExtension;

/**
 * Factory class that creates the RegionView object to be contributed to eclipse's {@link org.eclipse.ui.views} extension point.
 */
public class RegionViewLiveFactory extends RegionViewCreatorFactory implements FindableExecutableExtension {
	private Scannable dcmenergy;
	private Scannable pgmenergy;
	private double pgmEnergyDetectChangeGuiToleranceLevel = 0.075;
	private String currentIterationRemainingTimePV;
	private String iterationLeadPointsPV;
	private String iterationProgressPV;
	private String totalDataPointsPV;
	private String iterationCurrentPointPV;
	private String totalRemianingTimePV;
	private String totalProgressPV;
	private String totalPointsPV;
	private String currentPointPV;
	private String currentIterationPV;
	private String totalIterationsPV;
	private String statePV;
	private String acquirePV;
	private String messagePV;
	private String zeroSuppliesPV;

	@Override
	public Object create() throws CoreException {
		RegionViewLive regionViewLive = (RegionViewLive) super.create();
		if (dcmenergy!=null) regionViewLive.setDcmEnergy(dcmenergy);
		if (pgmenergy!=null) regionViewLive.setPgmEnergy(pgmenergy);
		regionViewLive.setPgmEnergyDetectChangeGuiToleranceLevel(pgmEnergyDetectChangeGuiToleranceLevel);
		regionViewLive.setCurrentIterationRemainingTimePV(getCurrentIterationRemainingTimePV());
		regionViewLive.setIterationLeadPointsPV(getIterationLeadPointsPV());
		regionViewLive.setIterationProgressPV(getIterationProgressPV());
		regionViewLive.setTotalDataPointsPV(getTotalDataPointsPV());
		regionViewLive.setIterationCurrentPointPV(iterationCurrentPointPV);
		regionViewLive.setTotalRemianingTimePV(totalRemianingTimePV);
		regionViewLive.setTotalProgressPV(totalProgressPV);
		regionViewLive.setTotalPointsPV(totalPointsPV);
		regionViewLive.setCurrentPointPV(currentPointPV);
		regionViewLive.setCurrentIterationPV(currentIterationPV);
		regionViewLive.setTotalIterationsPV(totalIterationsPV);
		regionViewLive.setStatePV(statePV);
		regionViewLive.setAcquirePV(acquirePV);
		regionViewLive.setMessagePV(messagePV);
		regionViewLive.setZeroSuppliesPV(zeroSuppliesPV);
		return regionViewLive;
	}

	@Override
	protected RegionViewLive createView() {
		return new RegionViewLive();
	}

	public String getCurrentIterationRemainingTimePV() {
		return currentIterationRemainingTimePV;
	}

	public void setCurrentIterationRemainingTimePV(
			String currentIterationRemainingTimePV) {
		this.currentIterationRemainingTimePV = currentIterationRemainingTimePV;
	}

	public String getIterationLeadPointsPV() {
		return iterationLeadPointsPV;
	}

	public void setIterationLeadPointsPV(String iterationLeadPointsPV) {
		this.iterationLeadPointsPV = iterationLeadPointsPV;
	}

	public String getIterationProgressPV() {
		return iterationProgressPV;
	}

	public void setIterationProgressPV(String iterationProgressPV) {
		this.iterationProgressPV = iterationProgressPV;
	}

	public String getTotalDataPointsPV() {
		return totalDataPointsPV;
	}

	public void setTotalDataPointsPV(String totalDataPointsPV) {
		this.totalDataPointsPV = totalDataPointsPV;
	}
	public String getIterationCurrentPointPV() {
		return iterationCurrentPointPV;
	}

	public void setIterationCurrentPointPV(String iterationCurrentPointPV) {
		this.iterationCurrentPointPV = iterationCurrentPointPV;
	}

	public String getTotalRemianingTimePV() {
		return totalRemianingTimePV;
	}

	public void setTotalRemianingTimePV(String totalRemianingTimePV) {
		this.totalRemianingTimePV = totalRemianingTimePV;
	}

	public String getTotalProgressPV() {
		return totalProgressPV;
	}

	public void setTotalProgressPV(String totalProgressPV) {
		this.totalProgressPV = totalProgressPV;
	}

	public String getTotalPointsPV() {
		return totalPointsPV;
	}

	public void setTotalPointsPV(String totalPointsPV) {
		this.totalPointsPV = totalPointsPV;
	}

	public String getCurrentPointPV() {
		return currentPointPV;
	}

	public void setCurrentPointPV(String currentPointPV) {
		this.currentPointPV = currentPointPV;
	}

	public String getCurrentIterationPV() {
		return currentIterationPV;
	}

	public void setCurrentIterationPV(String currentIterationPV) {
		this.currentIterationPV = currentIterationPV;
	}

	public String getTotalIterationsPV() {
		return totalIterationsPV;
	}

	public void setTotalIterationsPV(String totalIterationsPV) {
		this.totalIterationsPV = totalIterationsPV;
	}

	public String getStatePV() {
		return statePV;
	}

	public void setStatePV(String statePV) {
		this.statePV = statePV;
	}

	public String getAcquirePV() {
		return acquirePV;
	}

	public void setAcquirePV(String acquirePV) {
		this.acquirePV = acquirePV;
	}

	public String getMessagePV() {
		return messagePV;
	}

	public void setMessagePV(String messagePV) {
		this.messagePV = messagePV;
	}

	public String getZeroSuppliesPV() {
		return zeroSuppliesPV;
	}

	public void setZeroSuppliesPV(String zeroSuppliesPV) {
		this.zeroSuppliesPV = zeroSuppliesPV;
	}

	public void setDcmEnergy(Scannable energy) {
		this.dcmenergy=energy;
	}

	public Scannable getDcmEnergy() {
		return this.dcmenergy;
	}

	public void setPgmEnergy(Scannable energy) {
		this.pgmenergy=energy;
	}

	public Scannable getPgmEnergy() {
		return this.pgmenergy;
	}

	public double getPgmEnergyDetectChangeGuiToleranceLevel() {
		return pgmEnergyDetectChangeGuiToleranceLevel;
	}

	public void setPgmEnergyDetectChangeGuiToleranceLevel(double pgmEnergyDetectChangeGuiToleranceLevel) {
		this.pgmEnergyDetectChangeGuiToleranceLevel = pgmEnergyDetectChangeGuiToleranceLevel;
	}
}