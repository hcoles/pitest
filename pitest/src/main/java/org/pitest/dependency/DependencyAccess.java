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

public class DependencyAccess {

  public enum AccessType {
    METHOD, FIELD;
  }

  public static class Member implements Comparable<Member> {
    private final String owner;
    private final String name;
    private final String desc;

    public Member(final String owner, final String name, final String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }

    public String getOwner() {
      return this.owner;
    }

    public String getName() {
      return this.name;
    }

    public String getDesc() {
      return this.desc;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((this.desc == null) ? 0 : this.desc.hashCode());
      result = prime * result
          + ((this.name == null) ? 0 : this.name.hashCode());
      result = prime * result
          + ((this.owner == null) ? 0 : this.owner.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final Member other = (Member) obj;
      if (this.desc == null) {
        if (other.desc != null) {
          return false;
        }
      } else if (!this.desc.equals(other.desc)) {
        return false;
      }
      if (this.name == null) {
        if (other.name != null) {
          return false;
        }
      } else if (!this.name.equals(other.name)) {
        return false;
      }
      if (this.owner == null) {
        if (other.owner != null) {
          return false;
        }
      } else if (!this.owner.equals(other.owner)) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "Member [desc=" + this.desc + ", name=" + this.name + ", owner="
          + this.owner + "]";
    }

    public int compareTo(final Member other) {
      return other.desc.compareTo(this.desc)
          + (other.name.compareTo(this.name) * 100)
          + (other.owner.compareTo(this.owner) * 1000);
    }

  }

  private final AccessType type;
  private final Member     source;
  private final Member     dest;

  public DependencyAccess(final AccessType type, final Member source,
      final Member dest) {
    this.type = type;
    this.source = source;
    this.dest = dest;
  }

  public AccessType getType() {
    return this.type;
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
    result = prime * result + ((this.dest == null) ? 0 : this.dest.hashCode());
    result = prime * result
        + ((this.source == null) ? 0 : this.source.hashCode());
    result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DependencyAccess other = (DependencyAccess) obj;
    if (this.dest == null) {
      if (other.dest != null) {
        return false;
      }
    } else if (!this.dest.equals(other.dest)) {
      return false;
    }
    if (this.source == null) {
      if (other.source != null) {
        return false;
      }
    } else if (!this.source.equals(other.source)) {
      return false;
    }
    if (this.type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!this.type.equals(other.type)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DependencyAccess [source=" + this.source + ", dest=" + this.dest
        + ", type=" + this.type + "]";
  }

  public boolean pointsTo(final DependencyAccess a) {
    return this.getDest().equals(a.getSource());
  }

}
