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

public class PassthroughScanAdapter implements NestableScan {

	private final NestableScan delegate;

	public PassthroughScanAdapter(NestableScan delegate) {
		this.delegate = delegate;
	}

	@Override
	public void pause() {
		delegate.pause();
	}

	@Override
	public void resume() {
		delegate.resume();
	}

	@Override
	public void run() throws Exception {
		delegate.run();
	}

	@Override
	public void runScan() throws InterruptedException, Exception {
		delegate.runScan();
	}

	@Override
	public void doCollection() throws Exception {
		delegate.doCollection();
	}

	@Override
	public void prepareForCollection() throws Exception {
		delegate.prepareForCollection();
	}

	@Override
	public Vector<Scannable> getScannables() {
		return delegate.getScannables();
	}

	@Override
	public void setScannables(Vector<Scannable> allScannables) {
		delegate.setScannables(allScannables);
	}

	@Override
	public Vector<Detector> getDetectors() {
		return delegate.getDetectors();
	}

	@Override
	public void setDetectors(Vector<Detector> allDetectors) {
		delegate.setDetectors(allDetectors);
	}

	@Override
	public boolean isChild() {
		return delegate.isChild();
	}

	@Override
	public void setIsChild(boolean child) {
		delegate.setIsChild(child);
	}

	@Override
	public DataWriter getDataWriter() {
		return delegate.getDataWriter();
	}

	@Override
	public void setDataWriter(DataWriter dh) {
		delegate.setDataWriter(dh);
	}

	@Override
	public void setScanDataPointPipeline(ScanDataPointPipeline scanDataPointPipeline) {
		delegate.setScanDataPointPipeline(scanDataPointPipeline);
	}

	@Override
	public ScanDataPointPipeline getScanDataPointPipeline() {
		return delegate.getScanDataPointPipeline();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public NestableScan getParent() {
		return delegate.getParent();
	}

	@Override
	public void setParent(NestableScan parent) {
		delegate.setParent(parent);
	}

	@Override
	public Scan getChild() {
		return delegate.getChild();
	}

	@Override
	public void setChild(Scan child) {
		delegate.setChild(child);
	}

	@Override
	public IScanStepId getStepId() {
		return delegate.getStepId();
	}

	@Override
	public void setStepId(IScanStepId IScanStepId) {
		delegate.setStepId(IScanStepId);
	}

	@Override
	public void setScanPlotSettings(ScanPlotSettings scanPlotSettings) {
		delegate.setScanPlotSettings(scanPlotSettings);
	}

	@Override
	public ScanPlotSettings getScanPlotSettings() {
		return delegate.getScanPlotSettings();
	}

	@Override
	public int getDimension() {
		return delegate.getDimension();
	}

	@Override
	public int getTotalNumberOfPoints() {
		return delegate.getTotalNumberOfPoints();
	}

	@Override
	public int getScanNumber() {
		return delegate.getScanNumber();
	}

	@Override
	public ScanStatus getStatus() {
		return delegate.getStatus();
	}

	@Override
	public void requestFinishEarly() {
		delegate.requestFinishEarly();
	}

	@Override
	public boolean isFinishEarlyRequested() {
		return delegate.isFinishEarlyRequested();
	}

	@Override
	public void setStatus(ScanStatus status) {
		delegate.setStatus(status);
	}

	@Override
	public ScanInformation getScanInformation() {
		return delegate.getScanInformation();
	}

}
