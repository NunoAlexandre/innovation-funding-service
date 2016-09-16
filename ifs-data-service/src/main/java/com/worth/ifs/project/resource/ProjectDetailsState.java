package com.worth.ifs.project.resource;

import com.worth.ifs.workflow.resource.ProcessStates;
import com.worth.ifs.workflow.resource.State;

import java.util.*;

import static com.worth.ifs.util.CollectionFunctions.simpleMap;

public enum ProjectDetailsState implements ProcessStates {

    PENDING(State.PENDING),
    DECIDE_IF_READY_TO_SUBMIT(State.PENDING),
    READY_TO_SUBMIT(State.READY_TO_SUBMIT),
    SUBMITTED(State.SUBMITTED);

    //the status string value
    private State backingState;

    private static final Map<String, ProjectDetailsState> assessmentStatesMap;

    static {
        assessmentStatesMap = new HashMap<>();

        for (ProjectDetailsState assessmentState : ProjectDetailsState.values()) {
            assessmentStatesMap.put(assessmentState.getStateName(), assessmentState);
        }
    }

    // creates the enum with the chosen type.
    ProjectDetailsState(State backingState) {
        this.backingState = backingState;
    }

    @Override
    public String getStateName() {
        return backingState.name();
    }

    @Override
    public State getBackingState() {
        return backingState;
    }

    public static List<String> getStates() {
        return new ArrayList<>(assessmentStatesMap.keySet());
    }

    public static List<State> getBackingStates() {
        return simpleMap(ProjectDetailsState.values(), ProcessStates::getBackingState);
    }

    public static ProjectDetailsState getByState(String state) {
       return  assessmentStatesMap.get(state);
    }

    public static ProjectDetailsState fromState(State state) {
        return ProcessStates.fromState(ProjectDetailsState.values(), state);
    }
}
