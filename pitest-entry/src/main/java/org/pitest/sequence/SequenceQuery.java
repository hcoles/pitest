package org.pitest.sequence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SequenceQuery<T> {

  private final Partial<T> token;

  SequenceQuery(Partial<T> token) {
    this.token = token;
  }

  public SequenceQuery<T> then(Match<T> next) {
    return then(new SequenceQuery<>(new Literal<>(next)));
  }

  public SequenceQuery<T> then(SequenceQuery<T> next) {
    final Concat<T> concat = new Concat<>(this.token, next.token);
    return new SequenceQuery<>(concat);
  }

  public SequenceQuery<T> or(SequenceQuery<T> next) {
    final Or<T> or = new Or<>(this.token, next.token);
    return new SequenceQuery<>(or);
  }

  public SequenceQuery<T> thenAnyOf(SequenceQuery<T> left,
      SequenceQuery<T> right) {
    final Or<T> or = new Or<>(left.token, right.token);
    final Concat<T> concat = new Concat<>(this.token, or);
    return new SequenceQuery<>(concat);
  }

  public SequenceQuery<T> zeroOrMore(SequenceQuery<T> next) {
    final Concat<T> concat = new Concat<>(this.token, new Repeat<>(next.token));
    return new SequenceQuery<>(concat);
  }

  public SequenceQuery<T> oneOrMore(SequenceQuery<T> next) {
    final Concat<T> concat = new Concat<>(this.token, new Plus<>(next.token));
    return new SequenceQuery<>(concat);
  }

  public SequenceMatcher<T> compile() {
    return compile(QueryParams.params());
  }

  @SuppressWarnings("unchecked")
  public SequenceMatcher<T> compile(QueryParams<T> params) {
    return new NFASequenceMatcher<T>(params.ignoring(),
        this.token.make(EndMatch.MATCH), params.isDebug());
  }

  interface Partial<T> {
    State<T> make(State<T> andThen);
  }

  static class Literal<T> implements Partial<T> {
    final Match< T> c;

    Literal(Match<  T> p) {
      this.c = p;
    }

    @Override
    public State<T> make(State<T> andThen) {
      return new Consume<>(this.c, andThen);
    }
  }

  static class Or<T> implements Partial<T> {
    final Partial<T> left;
    final Partial<T> right;

    Or(Partial<T> left, Partial<T> right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public State<T> make(State<T> andThen) {
      final State<T> l = this.left.make(andThen);
      final State<T> r = this.right.make(andThen);
      return new Split<>(l, r);
    }

  }

  static class Concat<T> implements Partial<T> {
    final Partial<T> left;
    final Partial<T> right;

    Concat(Partial<T> left, Partial<T> right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public State<T> make(State<T> andThen) {
      return this.left.make(this.right.make(andThen));
    }
  }

  static class Repeat<T> implements Partial<T> {
    final Partial<T> r;

    Repeat(Partial<T> r) {
      this.r = r;
    }

    @Override
    public State<T> make(State<T> andThen) {
      final Split<T> placeHolder = new Split<>(null, null);
      final State<T> right = this.r.make(placeHolder);
      placeHolder.out1 = new Split<>(right, andThen);
      return placeHolder;
    }

  }

  static class Plus<T> implements Partial<T> {
    final Partial<T> r;

    Plus(Partial<T> r) {
      this.r = r;
    }

    @Override
    public State<T> make(State<T> andThen) {
      final Concat<T> concat = new Concat<>(this.r, new Repeat<>(this.r));
      return concat.make(andThen);
    }
  }

}

class NFASequenceMatcher<T> implements SequenceMatcher<T> {

  private final boolean debug;
  private final Match<T> ignore;
  private final State<T> start;

  NFASequenceMatcher(Match<T> ignore, State<T> state, boolean debug) {
    this.ignore = ignore;
    this.start = state;
    this.debug = debug;
  }


  @Override
  public boolean matches(List<T> sequence) {
    return matches(sequence, Context.start(this.debug));
  }

  @Override
  public boolean matches(List<T> sequence, Context initialContext) {
    Set<StateContext<T>> currentState = run(sequence, initialContext);
    return currentState.stream()
            .map(c -> c.state)
            .anyMatch(s -> s != null && s == EndMatch.MATCH);
  }

  @Override
  public List<Context> contextMatches(List<T> sequence, Context initialContext) {
    Set<StateContext<T>> currentState = run(sequence, initialContext);
    return currentState.stream()
            .filter(s -> s.state != null && s.state == EndMatch.MATCH)
            .map(c -> c.context)
            .collect(Collectors.toList());
  }

  private Set<StateContext<T>> run(List<T> sequence, Context initialContext) {
    Set<StateContext<T>> currentState = new HashSet<>();
    addState(currentState, new StateContext<>(this.start, initialContext));

    for (final T t : sequence) {
      // only initial context used in ignore checks
      if (this.ignore.test(initialContext, t).result()) {
        continue;
      }

      final Set<StateContext<T>> nextStates = step(currentState, t);
      currentState = nextStates;

    }
    return currentState;
  }


  private static <T> void addState(Set<StateContext<T>> set, StateContext<T> state) {

    if (state.state instanceof Split) {
      final Split<T> split = (Split<T>) state.state;
      if (split.out1 != null) {
        addState(set, new StateContext<T>(split.out1, state.context));
      }
      if (split.out2 != null) {
        addState(set, new StateContext<T>(split.out2, state.context));
      }
    } else {
      set.add(state);
    }

  }

  private static <T> Set<StateContext<T>> step(Set<StateContext<T>> currentState, T c) {

    // adhoc testing suggests setting the initial HashSet size saves 15% of analysis
    // execution time
    final Set<StateContext<T>> nextStates = new HashSet<>(currentState.size());

    for (final StateContext<T> each : currentState) {
      if (each.state instanceof Consume) {
        final Consume<T> consume = (Consume<T>) each.state;

        final Result<T> result = consume.c.test(each.context, c);
        if (result.result()) {
          // note, context updated here
          addState(nextStates, new StateContext<>(consume.out, result.context()));
        }
      }
    }
    return nextStates;
  }

}

interface State<T> {

}

class Consume<T> implements State<T> {
  final Match<T> c;
  final State<T> out;

  Consume(Match<T> c, State<T> out) {
    this.c = c;
    this.out = out;
  }

}

class Split<T> implements State<T> {
  State<T> out1;
  final State<T> out2;

  Split(State<T> out1, State<T> out2) {
    this.out1 = out1;
    this.out2 = out2;
  }
}

@SuppressWarnings("rawtypes")
enum EndMatch implements State {
  MATCH
}
