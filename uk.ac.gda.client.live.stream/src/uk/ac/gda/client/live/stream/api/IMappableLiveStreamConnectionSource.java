package uk.ac.gda.client.live.stream.api;

import java.util.Optional;

import org.dawnsci.mapping.ui.MappedDataView;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamException;

/**
 * Allows a connected Live Stream source to be retrieved packaged in a form suitable for display in a
 * {@link MappedDataView}.
 */
public interface IMappableLiveStreamConnectionSource {

	/**
	 * Obtains the packaged stream source identified from the supplied connection
	 *
	 * @return	An {@link Optional} of the mappable version of the default stream source, empty if none has been set.
	 * @throws	LiveStreamException If the connection to the source is unsuccessful
	 */
	public LiveStreamMapObject getLiveStreamMapObjectUsingConnection(final LiveStreamConnection liveStreamConnection)
			throws LiveStreamException;

	/**
	 * Obtains the packaged stream source identified as the default for the beamline. Theis will be the one identified
	 * in the mapping_stage_info bean referenced in the mapping.xml file of the beamline config. If no such reference is
	 * present, an empty {@link Optional} is returned.
	 *
	 * @return	An {@link Optional} of the mappable version of the default stream source, empty if none has been set.
	 * @throws	LiveStreamException If the connection to the source is unsuccessful
	 */
	public Optional<LiveStreamMapObject> getDefaultStreamSource() throws LiveStreamException;
}
