/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.junit.Test;
import org.pitest.verifier.mutants.IntMutantVerifier;
import org.pitest.verifier.mutants.MutantVerifier;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.Assert.assertEquals;

public class SwitchMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(new SwitchMutator())
            .notCheckingUnMutatedValues();


    @Test
    public void shouldProvideAMeaningfulName() {
        assertEquals("EXPERIMENTAL_SWITCH", new SwitchMutator().getName());
    }

    @Test
    public void createsMeaningfulDescription() {
        v.forIntFunctionClass(HasIntSwitchWithDefault.class)
                .firstMutantDescription()
                .isEqualTo("Changed switch default to be first case");
    }

    @Test
    public void shouldSwapFirstCaseWithDefaultForInt() {
        IntMutantVerifier<Integer> v2 = v.forIntFunctionClass(HasIntSwitchWithDefault.class);
        v2.firstMutantShouldReturn(0, 0);
        v2.firstMutantShouldReturn(1, 1);
    }

    @Test
    public void shouldSwapFirstCaseWithDefaultForChar() {
        MutantVerifier<Character, Character> v2 = v.forFunctionClass(HasCharSwitchWithDefault.class);
        v2.firstMutantShouldReturn('a', 'a');
        v2.firstMutantShouldReturn('z', 'z');
    }

    @Test
    public void shouldSwapFirstCaseWithDefaultForEnum() {
        MutantVerifier<TimeUnit, Integer> v2 = v.forFunctionClass(HasEnumSwitchWithDefault.class);
        v2.firstMutantShouldReturn(NANOSECONDS, 1);
        v2.firstMutantShouldReturn(MICROSECONDS, 2);
    }

    @Test
    public void shouldReplaceOtherCasesWithDefaultForInt() {
        IntMutantVerifier<Integer> v2 = v.forIntFunctionClass(HasMultipleArmIntSwitchWithDefault.class);
        v2.firstMutantShouldReturn(-8,
                1);
        v2.firstMutantShouldReturn(0,
                0);
        v2.firstMutantShouldReturn(1,
                1);
        v2.firstMutantShouldReturn(2,
                0);
        v2.firstMutantShouldReturn(3,
                1);
        v2.firstMutantShouldReturn(4,
                0);
        v2.firstMutantShouldReturn(8,
                1);
    }

    @Test
    public void shouldReplaceOtherCasesWithoutDefaultForInt() {
        IntMutantVerifier<Integer> v2 = v.forIntFunctionClass(HasMultipleArmIntSwitchWithoutDefault.class);
        v2.firstMutantShouldReturn(-1,
                1);
        v2.firstMutantShouldReturn(0,
                0);
        v2.firstMutantShouldReturn(8,
                1);
        v2.firstMutantShouldReturn(200,
                0);
        v2.firstMutantShouldReturn(400,
                1);
        v2.firstMutantShouldReturn(40000, 0);
        v2.firstMutantShouldReturn(45000, 1);
    }

    @Test
    public void shouldOnlyCreateRequestedMutationForTableSwitches() {
        v.forIntFunctionClass(HasTwoTableSwitchStatements.class)
                .firstMutantShouldReturn(0, 2);
    }

    @Test
    public void shouldOnlyCreateRequestedMutationForLookupSwitches() {
        v.forIntFunctionClass(HasTwoLookupSwitchStatements.class)
                .firstMutantShouldReturn(0, 2);
    }

    private static class HasIntSwitchWithDefault implements IntFunction<Integer> {

        @Override
        public Integer apply(int value) {
            switch (value) {
                case 0:
                    return 1;
                default:
                    return 0;
            }
        }
    }

    private static class HasCharSwitchWithDefault implements Function<Character, Character> {

        @Override
        public Character apply(Character c) {
            char value = c.charValue();
            switch (value) {
                case 'a':
                    return 'z';
                default:
                    return 'a';
            }
        }
    }

    private static class HasEnumSwitchWithDefault implements Function<TimeUnit, Integer> {

        @Override
        public Integer apply(TimeUnit value) {
            switch (value) {
                case NANOSECONDS:
                    return 2;
                default:
                    return 1;
            }
        }
    }

    private static class HasMultipleArmIntSwitchWithDefault implements IntFunction<Integer> {

        @Override
        public Integer apply(int value) {
            switch (value) {
                case 0:
                    return 1;
                case 2:
                    return 2;
                case 4:
                    return 3;
                default:
                    return 0;
            }
        }
    }

    private static class HasMultipleArmIntSwitchWithoutDefault implements
            IntFunction<Integer> {

        @Override
        public Integer apply(int value) {
            switch (value) {
                case 0:
                    return 1;
                case 200:
                    return 2;
                case 40000:
                    return 3;
            }
            return 0;
        }
    }

    private static class HasTwoTableSwitchStatements implements IntFunction<Integer> {

        @Override
        public Integer apply(int value) {
            int i = 1;
            switch (value) {
                case 0:
                    i = 10;
            }

            switch (value) {
                case 0:
                    i = i * 2;
            }

            return i;
        }
    }

    private static class HasTwoLookupSwitchStatements implements
            IntFunction<Integer> {

        @Override
        public Integer apply(int value) {
            int i = 1;
            switch (value) {
                case 100:
                    i = 42;
                    break;
                case 0:
                    i = 10;
            }

            switch (value) {
                case 100:
                    i = 42;
                    break;
                case 0:
                    i = i * 2;
            }

            return i;
        }
    }
}