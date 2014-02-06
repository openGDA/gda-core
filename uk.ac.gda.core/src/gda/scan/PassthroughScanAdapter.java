/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.scan;

import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.Scannable;

import java.util.Vector;

public class PassthroughScanAdapter implements Scan{
	
	private final Scan delegate;

	public void pause() {
		delegate.pause();
	}

	public void resume() {
		delegate.resume();
	}

	public void run() throws Exception {
		delegate.run();
	}

	public void runScan() throws InterruptedException, Exception {
		delegate.runScan();
	}

	public void doCollection() throws Exception {
		delegate.doCollection();
	}

	public void prepareForCollection() throws Exception {
		delegate.prepareForCollection();
	}

	public Vector<Scannable> getScannables() {
		return delegate.getScannables();
	}

	public void setScannables(Vector<Scannable> allScannables) {
		delegate.setScannables(allScannables);
	}

	public Vector<Detector> getDetectors() {
		return delegate.getDetectors();
	}

	public void setDetectors(Vector<Detector> allDetectors) {
		delegate.setDetectors(allDetectors);
	}

	public boolean isChild() {
		return delegate.isChild();
	}

	public void setIsChild(boolean child) {
		delegate.setIsChild(child);
	}

	public DataWriter getDataWriter() {
		return delegate.getDataWriter();
	}

	public void setDataWriter(DataWriter dh) {
		delegate.setDataWriter(dh);
	}

	public void setScanDataPointPipeline(ScanDataPointPipeline scanDataPointPipeline) {
		delegate.setScanDataPointPipeline(scanDataPointPipeline);
	}

	public ScanDataPointPipeline getScanDataPointPipeline() {
		return delegate.getScanDataPointPipeline();
	}

	public String getName() {
		return delegate.getName();
	}

	public Scan getParent() {
		return delegate.getParent();
	}

	public void setParent(Scan parent) {
		delegate.setParent(parent);
	}

	public Scan getChild() {
		return delegate.getChild();
	}

	public void setChild(Scan child) {
		delegate.setChild(child);
	}

	public IScanStepId getStepId() {
		return delegate.getStepId();
	}

	public void setStepId(IScanStepId IScanStepId) {
		delegate.setStepId(IScanStepId);
	}

	public void setScanPlotSettings(ScanPlotSettings scanPlotSettings) {
		delegate.setScanPlotSettings(scanPlotSettings);
	}

	public ScanPlotSettings getScanPlotSettings() {
		return delegate.getScanPlotSettings();
	}

	public int getDimension() {
		return delegate.getDimension();
	}

	public int getTotalNumberOfPoints() {
		return delegate.getTotalNumberOfPoints();
	}

	public Long getScanNumber() {
		return delegate.getScanNumber();
	}

	public ScanStatus getStatus() {
		return delegate.getStatus();
	}

	public PassthroughScanAdapter(Scan delegate) {
		this.delegate = delegate;
	}

}
