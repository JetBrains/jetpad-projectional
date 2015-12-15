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
package jetbrains.jetpad.cell.text;

import com.google.common.base.Predicate;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.BaseCompletionController;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionTestCase;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ValidTextCompletionTest extends CompletionTestCase {
  private TextCell text = new TextCell();
  private boolean lowPriorityCompleted;
  private boolean rtCompleted;

  @Before
  public void init() {
    myCellContainer.root.children().add(text);

    text.text().set("");
    text.focusable().set(true);

    text.addTrait(new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return
          TextEditing.validTextEditing(
            new Predicate<String>() {
              @Override
              public boolean apply(String input) {
                return "".equals(input) || "u".equals(input) || "qaz".equals(input);
              }
            });
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              class LowPriorityCompletionItem extends SimpleCompletionItem {
                LowPriorityCompletionItem(String matchingText) {
                  super(matchingText);
                }

                @Override
                public boolean isLowMatchPriority() {
                  return true;
                }

                @Override
                public Runnable complete(String text) {
                  lowPriorityCompleted = true;
                  return Runnables.EMPTY;
                }
              }

              class ActuallySetTextToCompletionItem extends SetTextToCompletionItem {
                public ActuallySetTextToCompletionItem(String match, String completion) {
                  super(match, completion);
                }

                @Override
                public Runnable complete(String text) {
                  ValidTextCompletionTest.this.text.text().set(myCompletion);
                  return super.complete(text);
                }
              }

              List<CompletionItem> result = new ArrayList<>();
              result.addAll(createCompletion("a", "c", "ae", "zz", "d", "u", "q").get(cp));
              result.add(new LowPriorityCompletionItem("d"));
              result.add(new LowPriorityCompletionItem("xx"));
              result.add(new LowPriorityCompletionItem("qq"));
              result.add(new LowPriorityCompletionItem("v"));
              result.add(new LowPriorityCompletionItem("va"));
              result.add(new SetTextToCompletionItem("var"));
              result.add(new SetTextToCompletionItem("foobar"));
              result.add(new ActuallySetTextToCompletionItem("foo", "qaz"));
              return result;
            }
          };
        }

        if (spec == Completion.RIGHT_TRANSFORM) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              List<CompletionItem> result = new ArrayList<>();
              result.add(new SimpleCompletionItem("z") {
                @Override
                public Runnable complete(String text) {
                  rtCompleted = true;
                  return Runnables.EMPTY;
                }
              });
              return result;
            }
          };
        }

        if (spec == TextEditing.RT_ON_END) {
          return true;
        }

        return super.get(cell, spec);
      }
    });

    myCellContainer.focusedCell.set(text);
  }

  @Test
  public void simpleCompletion() {
    type('c');
    assertCompleted("c");
  }

  @Test
  public void asyncCompletion() {
    text.addTrait(createAsyncCompletionTrait("fff", "yyyy"));

    complete();
    type("fff");

    enter();
    assertCompleted("fff");
  }

  @Test
  public void completionConflict() {
    type('a');
    assertNotCompleted();
  }

  @Test
  public void completionConflictResolutionWithEnter() {
    type('a');
    enter();

    assertCompleted("a");
  }

  @Test
  public void completeImmediatelyInCaseOfSingleCase() {
    type("z");
    complete();

    assertCompleted("zz");
  }

  @Test
  public void completionPopup() {
    complete();
    enter();

    assertCompleted("a");
  }

  @Test
  public void completionPopupNavigationDown() {
    complete();
    down();
    enter();

    assertCompleted("ae");
  }

  @Test
  public void completionPopupNavigationUp() {
    complete();
    down();
    down();
    up();
    enter();

    assertCompleted("ae");
  }

  @Test
  public void escapeDismissesCompletion() {
    complete();
    assertCompletionActive();
    escape();
    assertCompletionInactive();
  }

  @Test
  public void focusLossDismissesCompletion() {
    complete();
    assertCompletionActive();
    myCellContainer.focusedCell.set(null);
    assertCompletionInactive();
  }

  @Test
  public void focusLostThenGainedKeepsValidCompletionState() {
    focusLossDismissesCompletion();
    text.focus();
    complete();
    assertCompletionActive();
  }

  @Test
  public void itemRemoveLeadsToCompletionDismiss() {
    complete();
    assertCompletionActive();
    text.removeFromParent();
    assertCompletionInactive();
  }

  @Test
  public void completionShouldntBeShownTwice() {
    complete();

    Cell popup = text.bottomPopup().get();

    complete();

    assertSame(popup, text.bottomPopup().get());
  }

  @Test
  public void nonEagerCompletionDoesntComplete() {
    type("a");

    assertNotCompleted();
  }

  @Test
  public void eagerCompletionCompletes() {
    text.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextEditing.EAGER_COMPLETION) {
          return true;
        }

        return super.get(cell, spec);
      }
    });

    type("a");

    assertCompleted("a");
  }

  @Test
  public void lowPriorityIsBeatenByHighPriority() {
    type("d");

    assertFalse(lowPriorityCompleted);
    assertCompleted("d");
  }


  @Test
  public void lowPriorityWorksIfThereNoHighPriority() {
    type("xx");

    assertTrue(lowPriorityCompleted);
    assertNotCompleted();
  }

  @Test
  public void lowPriorityPrefixAndStrictNormalPriorityDontConflict() {
    type("q");

    assertFalse(lowPriorityCompleted);
    assertCompleted("q");
  }


  @Test
  public void rightTransformOnEnd() {
    type("xx");

    complete();
    enter();

    assertTrue(rtCompleted);
  }

  @Test
  public void cancellationOfRtOnEnd() {
    type("xx");

    Cell focused = myCellContainer.focusedCell.get();

    complete();
    escape();

    assertFocused(focused);
  }

  @Test
  public void completeOnSpaceOnEnd() {
    // Tests that the following line does not throw
    type("foo ");

    assertCompleted("qaz");
  }

  @Test
  public void lowPriorityIsntCompletedIf() {
    type("var");

    assertFalse(lowPriorityCompleted);
    assertCompleted("var");
  }

  private void assertCompletionActive() {
    assertHasBottomPopup(text);
    assertTrue(BaseCompletionController.isCompletionActive(text));
  }

  private void assertCompletionInactive() {
    assertNoBottomPopup(text);
    assertFalse(BaseCompletionController.isCompletionActive(text));
  }
}