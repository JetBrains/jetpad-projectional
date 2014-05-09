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
package jetbrains.jetpad.cell.view;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.toView.CellToView;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.view.*;

public class CellView extends HorizontalView {
  public static final ViewPropertySpec<Cell> CELL = new ViewPropertySpec<>("cell", ViewPropertyKind.RELAYOUT, null);

  public final Property<Cell> cell = getProp(CELL);
  public final CellContainer container = new CellContainer();

  private GroupView myPopupView = new GroupView();

  public CellView() {
    cell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Cell> event) {
        container.root.children().clear();
        if (event.getNewValue() != null) {
          container.root.children().add(event.getNewValue());
        }
      }
    });

    CellToView.map(container, this, this, myPopupView);
  }

  @Override
  protected void onAttach() {
    super.onAttach();

    container().decorationRoot().children().add(myPopupView);
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    Composites.<View>removeFromParent(myPopupView);
  }
}