/*
 * Copyright 2011 Henry Coles
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
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.config;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.ArgumentPropagationMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ABSMutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOD1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOD2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOR1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOR2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOR3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOR4Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR4Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR5Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR6Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.OBBN1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.OBBN2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.OBBN3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR4Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR5Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.UOI1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.UOI2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.UOI3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.UOI4Mutator;

public class MutatorTest {

  @Test
  public void providesINVERT_NEGS() {
    assertProvides("INVERT_NEGS");
  }

  @Test
  public void providesRETURN_VALS() {
    assertProvides("RETURN_VALS");
  }

  @Test
  public void providesINLINE_CONSTS() {
    assertProvides("INLINE_CONSTS");
  }

  @Test
  public void providesMATH() {
    assertProvides("MATH");
  }

  @Test
  public void providesVOID_METHOD_CALLS() {
    assertProvides("VOID_METHOD_CALLS");
  }

  @Test
  public void providesNEGATE_CONDITIONALS() {
    assertProvides("NEGATE_CONDITIONALS");
  }

  @Test
  public void providesCONDITIONALS_BOUNDARY() {
    assertProvides("CONDITIONALS_BOUNDARY");
  }

  @Test
  public void providesINCREMENTS() {
    assertProvides("INCREMENTS");
  }

  @Test
  public void providesREMOVE_INCREMENTS() {
    assertProvides("REMOVE_INCREMENTS");
  }

  @Test
  public void providesNON_VOID_METHOD_CALLS() {
    assertProvides("NON_VOID_METHOD_CALLS");
  }

  @Test
  public void providesCONSTRUCTOR_CALLS() {
    assertProvides("CONSTRUCTOR_CALLS");
  }

  @Test
  public void providesRemoveConditionalsMutators() {
    // Note name changes from EQ_IF and ORD_IF
    assertProvides("REMOVE_CONDITIONALS_EQUAL_IF");
    assertProvides("REMOVE_CONDITIONALS_EQUAL_ELSE");
    assertProvides("REMOVE_CONDITIONALS_ORDER_IF");
    assertProvides("REMOVE_CONDITIONALS_ORDER_ELSE");
  }

  @Test
  public void providesRemoveConditionalsGroup() {
    assertGroupHasSize("REMOVE_CONDITIONALS", 4);
  }

  @Test
  public void providesTRUE_RETURNS() {
    assertProvides("TRUE_RETURNS");
  }

  @Test
  public void providesFALSE_RETURNS() {
    assertProvides("FALSE_RETURNS");
  }

  @Test
  public void providesPRIMITIVE_RETURNS() {
    assertProvides("PRIMITIVE_RETURNS");
  }

  @Test
  public void providesEMPTY_RETURNS() {
    assertProvides("EMPTY_RETURNS");
  }

  @Test
  public void providesNULL_RETURNS() {
    assertProvides("NULL_RETURNS");
  }

  @Test
  public void providesRETURNSGroup() {
    assertGroupHasSize("RETURNS",5);
  }

  @Test
  public void providesRemoveSwitchGroup() {
    assertGroupHasSize("REMOVE_SWITCH", 100);
  }

  @Test
  public void providesDefaultsGroup() {
    assertGroupHasSize("DEFAULTS", 12);
  }

  @Test
  public void providesOldDefaultsGroup() {
    assertGroupHasSize("OLD_DEFAULTS", 7);
  }

  @Test
  public void providesStrongerGroup() {
    assertGroupHasSize("STRONGER", 15);
  }

  @Test
  public void providesExperimentalMutators() {
    assertProvides("EXPERIMENTAL_MEMBER_VARIABLE");
    assertProvides("EXPERIMENTAL_SWITCH");
    assertProvides("EXPERIMENTAL_ARGUMENT_PROPAGATION");
    assertProvides("EXPERIMENTAL_NAKED_RECEIVER");
    assertProvides("EXPERIMENTAL_BIG_INTEGER");
  }

  @Test
  public void providesResearchMutators() {
    assertProvides("AOR1");
    assertProvides("AOR2");
    assertProvides("AOR3");
    assertProvides("AOR4");

    assertProvides("ABS");

    assertProvides("AOD1");
    assertProvides("AOD2");

    assertProvides("CRCR1");
    assertProvides("CRCR2");
    assertProvides("CRCR3");
    assertProvides("CRCR4");
    assertProvides("CRCR5");
    assertProvides("CRCR6");

    assertProvides("OBBN1");
    assertProvides("OBBN2");
    assertProvides("OBBN3");

    assertProvides("ROR1");
    assertProvides("ROR2");
    assertProvides("ROR3");
    assertProvides("ROR4");
    assertProvides("ROR5");

    assertProvides("UOI1");
    assertProvides("UOI2");
    assertProvides("UOI3");
    assertProvides("UOI4");
  }

  @Test
  public void providesAORGroup() {
    assertGroupHasSize("AOR", 4);
  }

  @Test
  public void providesAODGroup() {
    assertGroupHasSize("AOD", 2);
  }

  @Test
  public void providesCRCRGroup() {
    assertGroupHasSize("CRCR", 6);
  }

  @Test
  public void providesOBBNGroup() {
    assertGroupHasSize("OBBN", 3);
  }

  @Test
  public void providesRORGroup() {
    assertGroupHasSize("ROR", 5);
  }

  @Test
  public void providesUOIGroup() {
    assertGroupHasSize("UOI", 4);
  }

  @Test
  public void shouldReturnRequestedMutators() {
    assertThat(parseStrings("MATH", "INVERT_NEGS")).containsAll(
        asList(MathMutator.MATH,
            InvertNegsMutator.INVERT_NEGS));
  }

  @Test
  public void shouldNotCreateDuplicatesWhenRequestedDirectly() {
    assertThat(parseStrings("MATH", "MATH")).hasSize(1);
  }

  @Test
  public void shouldNotCreateDuplicatesWhenRequestedViaGroup() {
    assertThat(parseStrings("MATH", "DEFAULTS")).hasSameSizeAs(
        parseStrings("DEFAULTS"));
  }

  @Test
  public void allContainsReplaceMethodMutator() {
    assertThat(Mutator.all()).contains(
        ArgumentPropagationMutator.EXPERIMENTAL_ARGUMENT_PROPAGATION);
  }

  @Test
  public void allMutatorIdsReturnsKeysForAllMutators() {
    List<String> incompleteSample = asList("DEFAULTS", "INCREMENTS", "EMPTY_RETURNS", "AOR");

    // method is used by pitclipse
    assertThat(Mutator.allMutatorIds()).containsAll(incompleteSample);
  }

  @Test
  public void allGroupContainsAllKeys() {
    assertThat(parseStrings("ALL")).containsExactlyElementsOf(Mutator.all());
  }

  @Test
  public void overlappingGroupsDoNotCauseDuplicates() {
    assertThat(parseStrings("DEFAULTS", "DEFAULTS", "INCREMENTS", "INCREMENTS"))
            .hasSameSizeAs(parseStrings("DEFAULTS", "INCREMENTS"));
  }

  @Test
  public void parsesExclusions() {
    assertThat(parseStrings("DEFAULTS", "-INCREMENTS"))
            .noneMatch(m -> m.getName().equals("INCREMENTS"));
  }

  @Test
  public void excludesGroups() {
    assertThat(parseStrings("DEFAULTS", "STRONGER", "-ALL")).isEmpty();
  }

  private Collection<MethodMutatorFactory> parseStrings(final String... s) {
    return Mutator.fromStrings(asList(s));
  }

  private void assertProvides(String name) {
    assertThatCode(() -> parseStrings(name)).doesNotThrowAnyException();
  }

  private void assertGroupHasSize(String name, int expected) {
    assertThatCode(() -> parseStrings(name)).doesNotThrowAnyException();
    assertThat(parseStrings(name)).hasSize(expected);
  }

}
