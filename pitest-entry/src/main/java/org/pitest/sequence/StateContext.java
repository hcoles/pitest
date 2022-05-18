package org.pitest.sequence;

import java.util.Objects;

/**
 * Pair class to hold state and context.
 */
final class StateContext<T> {

    StateContext(State<T> state, Context context) {
        this.state = state;
        this.context = context;
    }
    final State<T> state;
    final Context context;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StateContext)) {
            return false;
        }
        StateContext<?> that = (StateContext<?>) o;
        return state.equals(that.state) && context.equals(that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, context);
    }
}