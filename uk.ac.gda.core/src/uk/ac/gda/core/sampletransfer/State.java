package uk.ac.gda.core.sampletransfer;

import java.util.List;
import java.util.Map;

public enum State {
    IN_AIR,
    IN_HOTEL,
    IN_DOME;

    private static final Map<State, List<Transition>> allowedTransitions;

    static {
        allowedTransitions = Map.of(
            IN_AIR, List.of(Transition.AIR_TO_VACUUM),
            IN_HOTEL, List.of(Transition.HOTEL_TO_DOME),
            IN_DOME, List.of(Transition.DOME_TO_HOTEL)
        );
    }

    public List<Transition> getAllowedTransitions() {
        return allowedTransitions.get(this);
    }
}