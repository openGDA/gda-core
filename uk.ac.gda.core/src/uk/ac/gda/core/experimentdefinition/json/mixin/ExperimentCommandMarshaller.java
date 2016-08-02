package uk.ac.gda.core.experimentdefinition.json.mixin;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshaller;

import uk.ac.gda.client.experimentdefinition.ui.handlers.ExperimentCommand;

/**
 * Marshaller for {@link ExperimentCommand} objects.
 */
public class ExperimentCommandMarshaller implements IMarshaller {

	@Override
	public Class<?> getObjectClass() {
		return null;
	}

	@Override
	public Class<?> getSerializerClass() {
		return null;
	}

	@Override
	public Class<?> getDeserializerClass() {
		return null;
	}

	@Override
	public Class<ExperimentCommand> getMixinAnnotationType() {
		return ExperimentCommand.class;
	}

	@Override
	public Class<ExperimentCommandMixIn> getMixinAnnotationClass() {
		return ExperimentCommandMixIn.class;
	}

}
