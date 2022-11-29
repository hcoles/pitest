package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Filters out mutations in Enum constructors, these are called only once
 * per instance so are effectively static initializers.
 *
 * This overlaps with the StaticInitializerInterceptor, and could
 * probably be removed. Left in place for now as it is computationally less
 * expensive.
 *
 * Also filters mutants is the compiler generated valueOf and values methods.
 */
public class EnumFilter implements MutationInterceptor {

    private boolean isEnum;
    private ClassTree currentClass;

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        this.isEnum = clazz.rawNode().superName.equals("java/lang/Enum");
        this.currentClass = clazz;
    }

    @Override
    public Collection<MutationDetails> intercept(
            Collection<MutationDetails> mutations, Mutater m) {
        if (isEnum) {
            return mutations.stream()
                    .filter(makeMethodFilter(currentClass).negate())
                    .collect(Collectors.toList());
        }
        return mutations;

       }

    private Predicate<MutationDetails> makeMethodFilter(ClassTree currentClass) {
        Location valueOf = Location.location(currentClass.name(), "valueOf", "(Ljava/lang/String;)L" + currentClass.name().asInternalName() + ";");
        Location values = Location.location(currentClass.name(), "values", "()[L" + currentClass.name().asInternalName() + ";");

        return m -> isInEnumConstructor(m) || m.getId().getLocation().equals(valueOf) || m.getId().getLocation().equals(values);
    }

    private boolean isInEnumConstructor(MutationDetails m) {
        return m.getMethod().equals("<init>");
    }

    @Override
    public void end() {
        isEnum = false;
        currentClass = null;
    }

}

