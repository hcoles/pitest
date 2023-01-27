package org.pitest.verifier.interceptors;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.Repository;
import org.pitest.classpath.DefaultCodeSource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class Verifier {

    private final Sample sample;
    private final MutationInterceptor interceptor;
    private final Mutater mutator;
    private final Predicate<MutationDetails> match;


    public Verifier(Sample sample, MutationInterceptor interceptor, Mutater m) {
        this(sample,interceptor, m, mut -> true);
    }

    public Verifier(Sample sample, MutationInterceptor interceptor, Mutater m, Predicate<MutationDetails> match) {
        this.sample = sample;
        this.interceptor = interceptor;
        this.mutator = m;
        this.match = match;
    }

    public Verifier forMethod(String name) {
        return new Verifier(sample, interceptor, mutator, match.and(m -> m.getMethod().equals(name)));
    }

    public MutantVerifier forAnyCode() {
        return forMutantsMatching(m -> true);
    }

    public MutantVerifier forCodeMatching(Predicate<AbstractInsnNode> match) {
        return forMutantsMatching(mutates(match, sample).and(this.match));
    }

    public Collection<MutationDetails>  findMutants() {
        List<MutationDetails> mutations = mutator.findMutations(sample.className);
        return filter(sample.clazz, mutations, mutator);
    }

    public MutantVerifier forMutantsMatching(Predicate<MutationDetails> match) {
        List<MutationDetails> mutations = mutator.findMutations(sample.className);
        Collection<MutationDetails> afterFiltering = filter(sample.clazz, mutations, mutator);
        List<MutationDetails> filtered = new ArrayList<>(mutations);
        filtered.removeAll(afterFiltering);

        return new MutantVerifier(sample, this.match.and(match), mutations, afterFiltering, filtered);
    }

    private Collection<MutationDetails> filter(ClassTree clazz,
                                               List<MutationDetails> mutations, Mutater mutator) {
        interceptor.begin(clazz);
        final Collection<MutationDetails> actual = interceptor.intercept(mutations, mutator);
        interceptor.end();
        return actual;
    }

    private Predicate<MutationDetails> mutates(Predicate<AbstractInsnNode> match, Sample s) {
        return m -> match.test(s.clazz.method(m.getId().getLocation()).get().instruction(m.getInstructionIndex()));
    }

}
