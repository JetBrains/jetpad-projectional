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
package jetbrains.jetpad.cell.completion;

import jetbrains.jetpad.cell.trait.BaseCellTraitOld;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.Cell;
import org.junit.Before;

public class NonTextCellCompletionHandlerTest extends CompletionHandlerTestCase {
  private HorizontalCell target = new HorizontalCell();

  @Before
  public void init() {
    target.addTrait(new BaseCellTraitOld() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return createCompletion("a", "b", "c");
        }
        return super.get(cell, spec);
      }
    });
    target.addTrait(CompletionSupport.trait());

    myCellContainer.root.children().add(target);
    target.focusable().set(true);
    target.focus();
  }

  @Override
  protected Cell getView() {
    return target;
  }
}