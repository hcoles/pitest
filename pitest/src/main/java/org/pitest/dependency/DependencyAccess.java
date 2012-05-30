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

class DependencyAccess {

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
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((owner == null) ? 0 : owner.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Member other = (Member) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (owner == null) {
        if (other.owner != null)
          return false;
      } else if (!owner.equals(other.owner))
        return false;
      return true;
    }

    public int compareTo(final Member other) {
      return (other.name.compareTo(this.name) * 100)
          + (other.owner.compareTo(this.owner) * 1000);
    }

  }

  private final Member     source;
  private final Member     dest;

  protected DependencyAccess(final Member source,
      final Member dest) {
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
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dest == null) ? 0 : dest.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    return result;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DependencyAccess other = (DependencyAccess) obj;
    if (dest == null) {
      if (other.dest != null)
        return false;
    } else if (!dest.equals(other.dest))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    return true;
  }

  

}
