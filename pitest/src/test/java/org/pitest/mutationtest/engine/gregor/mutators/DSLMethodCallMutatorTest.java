/*
 * Copyright 2010 Henry Coles
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

package org.pitest.mutationtest.engine.gregor.mutators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.FunctionalList;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutatorTest.HasConstructorCall;

public class DSLMethodCallMutatorTest extends MutatorTestBase {

	private static class HasVoidMethodCall implements Callable<String> {

		private String s = "";

		public void set(final int i) {
			this.s = this.s + i;
		}

		public String call() throws Exception {
			set(1);
			return this.s;
		}

	}

	static class HasIntMethodCall implements Callable<String> {

		private static int i = 0;

		public int set(final int newVal) {
			i = newVal;
			return i + 42;
		}

		@SuppressWarnings("finally")
		public String call() throws Exception {
			int c = 2;
			try {
				c = set(1);
			} finally {
				return "" + c;
			}
		}

	}

	static class HasDslMethodCall implements Callable<String> {

		private int i = 0;

		public HasDslMethodCall chain(final int newVal) {
			this.i += newVal;
			return this;
		}

		public void voidNonDsl(final int newVal) {
			this.i += newVal;
		}

		public int nonDsl(final int newVal) {
			this.i += newVal;
			return i;
		}

		public String call() throws Exception {
			HasDslMethodCall dsl = this;
			dsl.chain(1).nonDsl(3);
			return "" + dsl;
		}

		@Override
		public String toString() {
			return "HasDslMethodCall [i=" + i + "]";
		}

	}

	@Before
	public void setupEngineToRemoveVoidMethods() {
		createTesteeWith(mutateOnlyCallMethod(), DSLMethodCallMutator.DSL_METHOD_CALL_MUTATOR);
	}

	@Test
	public void shouldRemoveNonVoidMethods() throws Exception {
		assertTrue(findMutationsFor(HasIntMethodCall.class).isEmpty());
	}

	@Test
	public void shouldNotRemoveVoidMethodCalls() throws Exception {
		assertTrue(findMutationsFor(HasVoidMethodCall.class).isEmpty());
	}

	@Test
	public void shouldNotRemoveConstructorCalls() throws Exception {
		final FunctionalList<MutationDetails> actual = findMutationsFor(HasConstructorCall.class);
		assertFalse(actual.contains(descriptionContaining("Integer")));
	}

	@Test
	public void shouldRemoveDslMethods() throws Exception {
		FunctionalList<MutationDetails> methods = findMutationsFor(HasDslMethodCall.class);
		assertEquals(1, methods.size());

		final Mutant mutant = getFirstMutant(HasDslMethodCall.class);
		assertMutantCallableReturns(new HasDslMethodCall(), mutant, "HasDslMethodCall [i=3]");
	}

}
