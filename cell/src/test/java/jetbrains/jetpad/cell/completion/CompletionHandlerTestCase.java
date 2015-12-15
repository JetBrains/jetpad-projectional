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
package jetbrains.jetpad.cell.completion;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.completion.CompletionController;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class CompletionHandlerTestCase extends CompletionTestCase {
  protected abstract Cell getView();

  protected CompletionController getController() {
    return getView().get(Completion.COMPLETION_CONTROLLER);
  }

  @Test
  public void completionHandlerAvailable() {
    assertNotNull(getController());
    assertFalse(getController().isActive());
  }

  @Test
  public void completionHandlerReturnCurrentValueAfterComplete() {
    complete();

    assertTrue(getController().isActive());
  }

  @Test
  public void completionCanBeActivatedWithCompletionHandler() {
    getController().activate();

    assertTrue(getController().isActive());
  }

  @Test
  public void completionCanBeDeactivatedWithCompletionHandler() {
    assertSame(myCellContainer.focusedCell.get(), getView());
    complete();

    getController().deactivate();
    assertFalse(getController().isActive());
    assertSame(myCellContainer.focusedCell.get(), getView());
  }
}