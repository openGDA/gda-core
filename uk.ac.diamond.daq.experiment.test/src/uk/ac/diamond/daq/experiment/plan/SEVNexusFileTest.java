package uk.ac.diamond.daq.experiment.plan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SEVNexusFileTest {

	private SEVNexusFile nexusFile;
	private INexusFileFactory nff;
	private File tempFile;

	private static final String FILE_PATH = "tempFile.h5";
	private static final String SCANNABLE_NAME = "simx";
	private static final String POSITION_DATABASE_NAME = "Position";


	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() {
		nff = new NexusFileFactoryHDF5();
		var serviceHolder = new ServiceHolder();
		serviceHolder.setNexusFileFactory(nff);
		nexusFile = new SEVNexusFile(SCANNABLE_NAME);
	}

	private NXroot getNexusRoot() throws NexusException {
		try(NexusFile nf = nff.newNexusFile(tempFile.getAbsolutePath(), true)) {
			nf.openToRead();
			TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
			return (NXroot) nexusTree.getGroupNode();
		}

	}
	@Test
	public void testNexusObjects() throws NexusException, IOException {
		tempFile = tempFolder.newFile(FILE_PATH);
		nexusFile.createNexusFile(tempFile.getAbsolutePath());

		int numElements = 10;

		IntStream.range(0, numElements).forEach(
				i -> nexusFile.writeData(i, "Timestamp-"+ i)
			);

		nexusFile.closeNexusFile();

		NXroot root = getNexusRoot();
		NXentry entry = root.getEntry();

		assertNotNull(entry);

		NXdata data = entry.getAllData().get(SCANNABLE_NAME);
		assertEquals(2, data.getAllDatasets().size());
		ILazyDataset dataset = data.getDataset(POSITION_DATABASE_NAME);

		assertEquals(numElements, dataset.getSize());
		assertEquals(1, dataset.getRank());

		try {
			IDataset slice = dataset.getSlice(new int[] {0}, new int[] {5}, null);
			assertEquals(5, slice.getSize());
			assertEquals(0, slice.getInt(0));
		} catch (DatasetException e) {
			e.printStackTrace();
		}

	}



}
