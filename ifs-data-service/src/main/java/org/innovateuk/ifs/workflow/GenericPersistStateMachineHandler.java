package org.innovateuk.ifs.workflow;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.listener.AbstractCompositeListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;

/**
 * Copy of the Spring State Machine recipe {@link org.springframework.statemachine.recipes.persist.PersistStateMachineHandler}
 * with the exception that this Handler is generic and so is able to support State Machines that aren't constrained to String
 * states.  This gives us type safety with our states.
 *
 * In addition, this version uses a stateMachineListener rather than an Interceptor as it triggers more appropriate lifecycle
 * events for persisting state changes when dealing with events that cause self-transitions or when evaluating pseudo-state
 * effects
 */
public class GenericPersistStateMachineHandler<StateType, EventType> extends LifecycleObjectSupport {

    private final StateMachine<StateType, EventType> stateMachine;
    private final GenericCompositePersistStateChangeListener listeners = new GenericCompositePersistStateChangeListener();

    /**
     * Instantiates a new persist state machine handler.
     *
     * @param stateMachine the state machine
     */
    public GenericPersistStateMachineHandler(StateMachine<StateType, EventType> stateMachine) {
        Assert.notNull(stateMachine, "State machine must be set");
        this.stateMachine = stateMachine;
        this.stateMachine.addStateListener(new PersistingStateMachineListener());
    }

    private class PersistingStateMachineListener extends StateMachineListenerAdapter<StateType, EventType> {

        @Override
        public void stateContext(StateContext<StateType, EventType> stateContext) {
            if (stateContext.getStage() == StateContext.Stage.STATE_CHANGED) {
                listeners.onPersist(stateContext.getTarget(), stateContext.getMessage(), stateContext.getTransition(), stateContext.getStateMachine());
            }
        }
    }

    /**
     * Handle event with entity.
     *
     * @param event the event
     * @param state the state
     * @return true if event was accepted
     */
    public boolean handleEventWithState(Message<EventType> event, StateType state) {
        stateMachine.stop();
        List<StateMachineAccess<StateType, EventType>> withAllRegions = stateMachine.getStateMachineAccessor().withAllRegions();
        for (StateMachineAccess<StateType, EventType> a : withAllRegions) {
            a.resetStateMachine(new DefaultStateMachineContext<>(state, null, null, null));
        }
        stateMachine.start();
        return stateMachine.sendEvent(event);
    }

    /**
     * Adds the persist state change listener.
     *
     * @param listener the listener
     */
    public void addPersistStateChangeListener(GenericPersistStateChangeListener<StateType, EventType> listener) {
        listeners.register(listener);
    }

    /**
     * The listener interface for receiving persistStateChange events.
     * The class that is interested in processing a persistStateChange
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addPersistStateChangeListener</code> method. When
     * the persistStateChange event occurs, that object's appropriate
     * method is invoked.
     */
    public interface GenericPersistStateChangeListener<StateType, EventType> {

        /**
         * Called when state needs to be persisted.
         *
         * @param state the state
         * @param message the message
         * @param transition the transition
         * @param stateMachine the state machine
         */
        void onPersist(State<StateType, EventType> state, Message<EventType> message, Transition<StateType, EventType> transition,
                       StateMachine<StateType, EventType> stateMachine);
    }

    private class GenericCompositePersistStateChangeListener extends AbstractCompositeListener<GenericPersistStateChangeListener<StateType, EventType>> implements
            GenericPersistStateChangeListener<StateType, EventType> {

        @Override
        public void onPersist(State<StateType, EventType> state, Message<EventType> message, Transition<StateType, EventType> transition, StateMachine<StateType, EventType> stateMachine) {

            for (Iterator<GenericPersistStateChangeListener<StateType, EventType>> iterator = getListeners().reverse(); iterator.hasNext();) {
                GenericPersistStateChangeListener<StateType, EventType> listener = iterator.next();
                listener.onPersist(state, message, transition, stateMachine);
            }
        }
    }
}

