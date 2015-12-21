/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.cell.error;

import com.google.common.base.Function;
import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellContainerAdapter;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.CompositeRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ErrorController {
  private static final CellTraitPropertySpec<ErrorController> TRAIT = new CellTraitPropertySpec<>("errorController");
  static final CellPropertySpec<String> ERROR = new CellPropertySpec<>("error");
  static final CellPropertySpec<String> WARNING = new CellPropertySpec<>("warning");
  static final CellPropertySpec<String> BROKEN = new CellPropertySpec<>("broken");

  public static Registration install(CellContainer container,
                                     ErrorStyler defaultStyler,
                                     List<Function<Cell, ErrorStyler>> customStylers) {
    ErrorController controller = new ErrorController(container);
    ErrorDecorationTrait decorator = new ErrorDecorationTrait(new ErrorStyleController(defaultStyler, customStylers));
    return controller.install(decorator);
  }

  public static Registration install(CellContainer container) {
    return install(container, null, null);
  }

  public static void setBroken(Cell cell, String message) {
    set(cell, message, BROKEN);
  }

  public static void setError(Cell cell, String message) {
    set(cell, message, ERROR);
  }

  public static void setWarning(Cell cell, String message) {
    set(cell, message, WARNING);
  }

  static void set(Cell cell, String message, CellPropertySpec<String> prop) {
    if (canHaveErrors(cell)) {
      cell.set(prop, message);
    }
  }

  private static boolean canHaveErrors(Cell cell) {
    return !cell.get(Cell.POPUP);
  }

  public static ErrorController getController(Cell cell) {
    if (!cell.isAttached()) {
      throw new IllegalStateException();
    }
    return getController(cell.cellContainer().get());
  }

  public static ErrorController getController(CellContainer container) {
    return container.root.get(ErrorController.TRAIT);
  }

  public static boolean isBroken(Cell cell) {
    return cell.get(BROKEN) != null;
  }

  public static boolean hasWarning(Cell cell) {
    return cell.get(WARNING) != null;
  }

  public static boolean hasError(Cell cell) {
    return cell.get(ERROR) != null;
  }


  private CellContainer myContainer;
  private MyChildrenListener myChildrenListener;

  private ErrorController(CellContainer container) {
    if (container.root.get(TRAIT) != null) {
      throw new IllegalArgumentException("Error controller is already installed");
    }
    myContainer = container;
  }

  private CompositeRegistration install(ErrorDecorationTrait decorator) {
    CompositeRegistration result = new CompositeRegistration();

    result.add(myContainer.root.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TRAIT) {
          return ErrorController.this;
        }
        return super.get(cell, spec);
      }
    }));

    myChildrenListener = new MyChildrenListener(myContainer, decorator);
    result.add(myContainer.addListener(myChildrenListener));
    result.add(new Registration() {
      @Override
      protected void doRemove() {
        myChildrenListener.dispose();
      }
    });

    return result;
  }

  // for tests
  int getErrorDecoratedCellsCount() {
    return myChildrenListener.myRegistrations.size();
  }

  private static class MyChildrenListener extends CellContainerAdapter implements Disposable {
    private ErrorDecorationTrait myDecorator;
    private Map<Cell, Registration> myRegistrations;

    private Handler<Cell> myAttachHandler = new Handler<Cell>() {
      @Override
      public void handle(final Cell cell) {
        if (!canHaveErrors(cell)) {
          return;
        }
        if (myRegistrations != null && myRegistrations.containsKey(cell)) {
          throw new IllegalStateException();
        }
        final Registration decorationReg = cell.addTrait(myDecorator);
        if (myRegistrations == null) {
          myRegistrations = new HashMap<>();
        }
        myRegistrations.put(cell, new Registration() {
          @Override
          protected void doRemove() {
            decorationReg.remove();
            myDecorator.detach(cell);
          }
        });
      }
    };

    private Handler<Cell> myDetachHandler = new Handler<Cell>() {
      @Override
      public void handle(Cell cell) {
        if (myRegistrations == null) return;
        Registration registration = myRegistrations.remove(cell);
        if (registration != null) {
          registration.remove();
        }
        if (myRegistrations.isEmpty()) {
          myRegistrations = null;
        }
      }
    };

    MyChildrenListener(CellContainer container, ErrorDecorationTrait decorator) {
      myDecorator = decorator;
      visit(container.root, myAttachHandler);
    }

    @Override
    public void onChildAdded(Cell parent, CollectionItemEvent<? extends Cell> change) {
      visit(change.getNewItem(), myAttachHandler);
    }

    @Override
    public void onChildRemoved(Cell parent, CollectionItemEvent<? extends Cell> change) {
      visit(change.getOldItem(), myDetachHandler);
    }

    private void visit(Cell cell, Handler<Cell> handler) {
      handler.handle(cell);
      for (Cell child : cell.children()) {
        visit(child, handler);
      }
    }

    @Override
    public void dispose() {
      if (myRegistrations == null) return;
      for (Cell cell : myRegistrations.keySet()) {
        myRegistrations.get(cell).remove();
      }
      myRegistrations = null;
    }
  }
}
