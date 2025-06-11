package uk.ac.gda.core.sampletransfer;

/**
 * Represents a message that includes the name of the sequence and the command to be executed.
 *
 * Components:
 * - sequence: The name of the sequence
 * - sample: The sample selected by the user
 * - command: The command to be executed on the sequence {@link SequenceCommand}
 */
public class SequenceRequest {

    private Sequence sequence;
    private SampleSelection sample;
    private SequenceCommand command;

    public SequenceRequest(Sequence sequence, SampleSelection sample, SequenceCommand command) {
        this.sequence = sequence;
        this.sample = sample;
        this.command = command;
    }

    public SequenceRequest() {

    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public SampleSelection getSample() {
        return sample;
    }

    public void setSample(SampleSelection sample) {
        this.sample = sample;
    }

    public SequenceCommand getCommand() {
        return command;
    }

    public void setCommand(SequenceCommand command) {
        this.command = command;
    }
}

