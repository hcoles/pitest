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
package org.pitest.dependency;

import java.util.Objects;

final class DependencyAccess {

  static class Member implements Comparable<Member> {
    private final String owner;
    private final String name;

    protected Member(final String owner, final String name) {
      this.owner = owner;
      this.name = name;
    }

    public String getOwner() {
      return this.owner;
    }

    public String getName() {
      return this.name;
    }

    @Override
    public int hashCode() {
      return Objects.hash(owner, name);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final Member other = (Member) obj;
      return Objects.equals(owner, other.owner)
              && Objects.equals(name, other.name);
    }

    @Override
    public int compareTo(final Member other) {
      return (other.name.compareTo(this.name) * 100)
          + (other.owner.compareTo(this.owner) * 1000);
    }

  }

  private final Member source;
  private final Member dest;

  protected DependencyAccess(final Member source, final Member dest) {
    this.source = source;
    this.dest = dest;
  }

  public Member getSource() {
    return this.source;
  }

  public Member getDest() {
    return this.dest;
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, dest);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final DependencyAccess other = (DependencyAccess) obj;
    return Objects.equals(source, other.source)
            && Objects.equals(dest, other.dest);
  }
}
