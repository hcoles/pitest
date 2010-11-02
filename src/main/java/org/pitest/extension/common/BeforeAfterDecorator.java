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

package org.pitest.extension.common;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.teststeps.CallStep;

public class BeforeAfterDecorator extends TestUnitDecorator {

  private final List<CallStep> before = new LinkedList<CallStep>();
  private final List<CallStep> after  = new LinkedList<CallStep>();

  public BeforeAfterDecorator(final TestUnit child,
      final Collection<CallStep> before, final Collection<CallStep> after) {
    super(child);
    this.before.addAll(before);
    this.after.addAll(after);
  }

  public void execute(final ClassLoader loader, final ResultCollector rc) {
    final SideEffect1<CallStep> e = new SideEffect1<CallStep>() {
      public void apply(final CallStep a) {
        a.execute(loader, child().description(), null);
      }

    };
    FCollection.forEach(this.before, e);
    this.child().execute(loader, rc);
    FCollection.forEach(this.after, e);

  }

  public Option<TestUnit> filter(final TestFilter filter) {
    final Option<TestUnit> modifiedChild = this.child().filter(filter);
    if (modifiedChild.hasSome()) {
      return Option.<TestUnit> someOrNone(new BeforeAfterDecorator(
          modifiedChild.value(), this.before, this.after));
    } else {
      return Option.none();
    }

  }

}
