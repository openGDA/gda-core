package uk.ac.gda.core.experimentdefinition.json.mixin;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshaller;

import gda.commandqueue.Command;

/**
 * Marshaller for {@link Command} objects.
 */
public class CommandMarshaller implements IMarshaller {

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
	public Class<Command> getMixinAnnotationType() {
		return Command.class;
	}

	@Override
	public Class<CommandMixIn> getMixinAnnotationClass() {
		return CommandMixIn.class;
	}

}
