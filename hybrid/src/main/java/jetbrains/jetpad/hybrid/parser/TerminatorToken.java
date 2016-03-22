/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.hybrid.parser;

public final class TerminatorToken extends SimpleToken {

  public TerminatorToken(String prefix) {
    this(prefix, "");
  }

  public TerminatorToken(String prefix, String name) {
    super(prefix + name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TerminatorToken that = (TerminatorToken) o;

    return text().equals(that.text());

  }

  @Override
  public int hashCode() {
    return text().hashCode();
  }

}
