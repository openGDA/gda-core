
package gda.device.detector;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.RGBByteDataset;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.images.camera.mjpeg.MjpegInputStream;
import uk.ac.diamond.scisoft.analysis.io.AWTImageUtils;

public class MjpegDetector extends DetectorBase implements NexusDetector {

	private String urlString;
	private MjpegInputStream mjpegStream;

	/**
	 * Connect to the Mjpeg stream
	 *
	 * @throws DeviceException
	 */
	public void connect() throws DeviceException {
		if (StringUtils.isBlank(urlString)) {
			throw new IllegalArgumentException("Url for MjpegDetector has not been set");
		}
		try {
			URL url = new URL(urlString);
			mjpegStream = new MjpegInputStream(url);
			mjpegStream.connect();
		} catch(IOException e) {
			throw new DeviceException("Problem connecting to Mjpeg stream '"+urlString+"'", e);
		}
	}

	/**
	 * Close the connection to the Mjpeg stream
	 *
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (mjpegStream != null) {
			mjpegStream.close();
			mjpegStream = null;
		}
	}

	@Override
	public void collectData() throws DeviceException {
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return createNexusData(readDataset());
	}

	public Dataset readDataset() throws DeviceException {
		if (mjpegStream == null) {
			connect();
		}
		try {
			final ByteArrayInputStream jpegBytes = new ByteArrayInputStream(mjpegStream.getNextFrame());
			BufferedImage bufferedImage = ImageIO.read(jpegBytes);
			RGBByteDataset dataset = (RGBByteDataset) AWTImageUtils.makeDatasets(bufferedImage, true)[0];

			// Collect data from rgb colour channels into datasets
			List<Dataset> rgbData = List.of(dataset.createRedDataset(IntegerDataset.class), dataset.createGreenDataset(IntegerDataset.class), dataset.createBlueDataset(IntegerDataset.class));

			// Create new 3d dataset with each colour channel as a separate slice
			return DatasetFactory.createFromList(rgbData);
		} catch(IOException e) {
			throw new DeviceException("Problem reading Mjpeg stream from "+urlString, e);
		}
	}

	private NexusTreeProvider createNexusData(IDataset dataset) {
		NXDetectorData frame = new NXDetectorData(getExtraNames(), getOutputFormat(), getName());
		INexusTree detTree = frame.getDetTree(getName());
		// Add the image data
		var detNode = NXDetectorData.addData(detTree, "imagedata", new NexusGroupData(dataset), "rgb", 1);
		detNode.addChildNode(new NexusTreeNode("interpretation", NexusExtractor.AttrClassName, detNode, new NexusGroupData("rgb-image")));

		// Add the stream url
		NXDetectorData.addData(detTree, "url", new NexusGroupData(urlString), "", 1);

		// Value displayed during scans
		frame.setPlottableValue(getName(), 1.0);
		return frame;
	}

	public String getUrlString() {
		return urlString;
	}

	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}
}

