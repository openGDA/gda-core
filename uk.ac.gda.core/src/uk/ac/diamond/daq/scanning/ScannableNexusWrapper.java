package uk.ac.diamond.daq.scanning;

import org.eclipse.dawnsci.analysis.api.dataset.Dtype;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;

import gda.device.Scannable;

/**
 * Class provides a default implementation which will write any GDA8 scannable to NeXus
 *
 * @author Matthew Gerring
 */
class ScannableNexusWrapper implements IScannable<Object>, INexusDevice<NXpositioner> {

	public static final String FIELD_NAME_DEMAND_VALUE = NXpositioner.NX_VALUE + "_demand";

	private Scannable scannable;
	private ILazyWriteableDataset lzDemand;
	private ILazyWriteableDataset lzValue;

	ScannableNexusWrapper(Scannable scannable) {
		this.scannable = scannable;

	}

	@Override
	public NexusObjectProvider<NXpositioner> getNexusProvider(NexusScanInfo info) {
		return new DelegateNexusProvider<NXpositioner>(scannable.getName(), NexusBaseClass.NX_POSITIONER, NXpositioner.NX_VALUE, info, this);
	}

	@Override
	public NXpositioner createNexusObject(NexusNodeFactory nodeFactory, NexusScanInfo info) {

		final NXpositioner positioner = nodeFactory.createNXpositioner();
		positioner.setNameScalar(scannable.getName());

		this.lzDemand = positioner.initializeLazyDataset(FIELD_NAME_DEMAND_VALUE, 1, Dtype.FLOAT64);
		lzDemand.setChunking(new int[]{1});

		this.lzValue = positioner.initializeLazyDataset(NXpositioner.NX_VALUE, info.getRank(), Dtype.FLOAT64);
		lzValue.setChunking(info.createChunk(1)); // TODO Might be slow, need to check this

		return positioner;
	}

	@Override
	public void setLevel(int level) {
		scannable.setLevel(level);
	}

	@Override
	public int getLevel() {
		return scannable.getLevel();
	}

	@Override
	public String getName() {
		return scannable.getName();
	}

	@Override
	public void setName(String name) {
		scannable.setName(name);
	}

	@Override
	public Object getPosition() throws Exception {
		return scannable.getPosition();
	}

	@Override
	public void setPosition(Object value) throws Exception {
		scannable.moveTo(value);
	}

	@Override
	public void setPosition(Object value, IPosition position) throws Exception {

		// Move the scannable blocks until completed
		scannable.moveTo(value);

		if (position != null)
			write(value, scannable.getPosition(), position);
	}

	private void write(Object demand, Object actual, IPosition loc) throws Exception {


		if (actual!=null) {
			// write actual position
			final IDataset newActualPositionData = DatasetFactory.createFromObject(actual);
			SliceND sliceND = NexusScanInfo.createLocation(lzValue, loc.getNames(), loc.getIndices());
			lzValue.setSlice(null, newActualPositionData, sliceND);
		}

		if (demand!=null) {
			int index = loc.getIndex(getName());
			if (index<0) {
				throw new Exception("Incorrect data index for scan for value of '"+getName()+"'. The index is "+index);
			}
			final int[] startPos = new int[] { index };
			final int[] stopPos = new int[] { index + 1 };

			// write demand position
			final IDataset newDemandPositionData = DatasetFactory.createFromObject(demand);
			lzDemand.setSlice(null, newDemandPositionData, startPos, stopPos, null);
		}
	}

}
