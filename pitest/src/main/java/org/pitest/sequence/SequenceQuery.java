package org.pitest.sequence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    return compile(QueryParams.<T>params());
  }

  @SuppressWarnings("unchecked")
  public SequenceMatcher<T> compile(QueryParams<T> params) {
    return new NFASequenceMatcher<>(params.ignoring(),
        this.token.make(EndMatch.MATCH), params.isDebug());
  }

  interface Partial<T> {
    State<T> make(State<T> andThen);
  }

  static class Literal<T> implements Partial<T> {
    Match< T> c;

    Literal(Match<  T> p) {
      this.c = p;
    }

    @Override
    public State<T> make(State<T> andThen) {
      return new Consume<>(this.c, andThen);
    }
  }

  static class Or<T> implements Partial<T> {
    Partial<T> left;
    Partial<T> right;

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
    Partial<T> left;
    Partial<T> right;

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
    Partial<T> r;

    Repeat(Partial<T> r) {
      this.r = r;
    }

    @Override
    public State<T> make(State<T> andThen) {
      final Split<T> placeHolder = new Split<>(null, null);
      final State<T> right = this.r.make(placeHolder);
      final Split<T> split = new Split<>(right, andThen);

      placeHolder.out1 = split;
      return placeHolder;
    }

  }

  static class Plus<T> implements Partial<T> {
    Partial<T> r;

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
    return matches(sequence, Context.start(sequence, this.debug));
  }

  @Override
  public boolean matches(List<T> sequence, Context<T> context) {
    Set<State<T>> currentState = new HashSet<>();
    addstate(currentState, this.start);

    for (final T t : sequence) {
      context.moveForward();

      if (this.ignore.test(context, t)) {
        continue;
      }

      final Set<State<T>> nextStates = step(context, currentState, t);
      currentState = nextStates;
    }
    return isMatch(currentState);
  }


  private static <T> void addstate(Set<State<T>> set, State<T> state) {
    if (state == null) {
      return;
    }
    if (state instanceof Split) {
      final Split<T> split = (Split<T>) state;
      addstate(set, split.out1);
      addstate(set, split.out2);
    } else {
      set.add(state);
    }

  }

  private static <T> Set<State<T>> step(Context<T> context, Set<State<T>> currentState, T c) {

    final Set<State<T>> nextStates = new HashSet<>();
    for (final State<T> each : currentState) {
      if (each instanceof Consume) {
        final Consume<T> consume = (Consume<T>) each;
        if (consume.c.test(context, c)) {
          addstate(nextStates, consume.out);
        }
      }
    }
    return nextStates;
  }

  private static <T> boolean isMatch(Set<State<T>> currentState) {
    return currentState.contains(EndMatch.MATCH);
  }

}

interface State<T> {

}

class Consume<T> implements State<T> {
  final Match< T> c;
  State<T>           out;

  Consume(Match<T> c, State<T> out) {
    this.c = c;
    this.out = out;
  }

  boolean matches(Context<T> context, T t) {
    return this.c.test(context, t);
  }
}

class Split<T> implements State<T> {
  State<T> out1;
  State<T> out2;

  Split(State<T> out1, State<T> out2) {
    this.out1 = out1;
    this.out2 = out2;
  }
}

@SuppressWarnings("rawtypes")
enum EndMatch implements State {
  MATCH;
}
