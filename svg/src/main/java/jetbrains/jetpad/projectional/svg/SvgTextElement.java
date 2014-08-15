/*
 * Copyright 2012-2014 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.collections.list.ObservableList;

public class SvgTextElement extends SvgStylableElement {
  public static final SvgPropertySpec<Double> X = new SvgPropertySpec<>("x", 0.0);
  public static final SvgPropertySpec<Double> Y = new SvgPropertySpec<>("y", 0.0);

  public void addTextNode(String text) {
    ObservableList<SvgNode> children = children();
    SvgTextNode textNode = new SvgTextNode(text);
    children.add(textNode);
  }
}