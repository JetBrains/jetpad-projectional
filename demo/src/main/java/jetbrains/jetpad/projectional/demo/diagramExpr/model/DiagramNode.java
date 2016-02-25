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
package jetbrains.jetpad.projectional.demo.diagramExpr.model;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.SimpleComposite;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.Expression;

public class DiagramNode extends SimpleComposite<SimpleDiagram, DiagramNode> {
  public final Property<Vector> location = new ValueProperty<>(Vector.ZERO);
  public final Property<Expression> expression = new ValueProperty<>();
  public final ObservableList<DiagramNodeConnection> connections = new ChildList<>(this);
}