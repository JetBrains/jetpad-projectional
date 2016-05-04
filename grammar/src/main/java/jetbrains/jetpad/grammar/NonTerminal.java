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
package jetbrains.jetpad.grammar;

import java.util.*;

/**
 * Non terminal symbol of a grammar.
 * Has a set of rules associated with it.
 */
public final class NonTerminal extends Symbol {
  private Set<Rule> myRules = new LinkedHashSet<>();

  NonTerminal(Grammar grammar, String name) {
    super(grammar, name);
  }

  void addRule(Rule rule) {
    myRules.add(rule);
  }

  public Set<Rule> getRules() {
    return Collections.unmodifiableSet(myRules);
  }

  public Rule getFirstRule() {
    return myRules.iterator().next();
  }

  public boolean isNullable() {
    return getGrammar().getGrammarData().isNullable(this);
  }

  public Set<Terminal> getFirst() {
    return getGrammar().getGrammarData().getFirst(this);
  }

  public Set<Terminal> getFollow() {
    return getGrammar().getGrammarData().getFollow(this);
  }
}