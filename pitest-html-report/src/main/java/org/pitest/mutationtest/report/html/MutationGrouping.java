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
package org.pitest.mutationtest.report.html;

import java.util.List;

import org.pitest.mutationtest.MutationResult;

public class MutationGrouping {

  private final int                  id;
  private final String               title;
  private final List<MutationResult> mutations;

  public MutationGrouping(final int id, final String title,
      final List<MutationResult> mutations) {
    this.title = title;
    this.mutations = mutations;
    this.id = id;
  }

  public String getTitle() {
    return this.title;
  }

  public List<MutationResult> getMutations() {
    return this.mutations;
  }

  public int getId() {
    return this.id;
  }

}
