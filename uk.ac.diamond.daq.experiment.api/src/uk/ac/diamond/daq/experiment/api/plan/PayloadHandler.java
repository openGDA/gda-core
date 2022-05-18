package uk.ac.diamond.daq.experiment.api.plan;

public interface PayloadHandler<P extends Payload> {

	Class<?> getSourceClass();

	Class<P> getTargetClass();

	P wrap(Object rawPayload);

	Object handle(P payload);

}
