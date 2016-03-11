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
package jetbrains.jetpad.hybrid;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.completion.BaseCompletionParameters;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.*;
import jetbrains.jetpad.hybrid.util.HybridWrapperRole;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.composite.Composites;
import org.junit.Test;

import static org.junit.Assert.*;

public class HybridEditorEditingTest extends BaseHybridEditorEditingTest<ExprContainerMapper> {
  @Override
  protected ExprContainerMapper createMapper() {
    return new ExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr, ?> getSync(ExprContainerMapper mapper) {
    return mapper.hybridSync;
  }

  @Test
  public void simpleTyping() {
    type("id");

    assertTrue(container.expr.get() instanceof IdExpr);
    assertEquals(1, targetCell.children().size());
  }

  @Test
  public void errorState() {
    type("+");

    assertNull(container.expr.get());
    assertTokens(Tokens.PLUS);
  }

  @Test
  public void plusExpr() {
    type("id+id");

    assertTrue(container.expr.get() instanceof PlusExpr);
    assertEquals(5, targetCell.children().size());
  }

  @Test
  public void intermediateErrorState() {
    type("id+");

    assertTrue(container.expr.get() instanceof IdExpr);
    assertTokens(Tokens.ID, Tokens.PLUS);
  }

  @Test
  public void replacePlusWith() {
    setTokens(Tokens.PLUS);
    select(0, true);

    complete();
    type("i");
    enter();

    assertTrue(container.expr.get() instanceof IdExpr);
  }

  @Test
  public void leftTransformTyping() {
    setTokens(Tokens.PLUS, Tokens.ID);
    select(0, true);

    type("id");

    assertTrue(container.expr.get() instanceof PlusExpr);
  }

  @Test
  public void deleteToEmpty() {
    container.expr.set(new IdExpr());
    setTokens(Tokens.PLUS);
    select(0, true);

    press(Key.DELETE, ModifierKey.CONTROL);

    assertNull(container.expr.get());
    assertTrue(sync.placeholder().focused().get());
  }

  @Test
  public void backspaceToEmpty() {
    type("id");

    press(Key.BACKSPACE);
    press(Key.BACKSPACE);

    assertNull(container.expr.get());
    assertTrue(sync.placeholder().focused().get());
  }

  @Test
  public void deleteEmptyToken() {
    setTokens(Tokens.MUL, Tokens.ID);
    select(1, false);

    backspace();
    backspace();
    backspace();

    assertTokens(Tokens.MUL);
    assertSelectedEnd(0);
  }

  @Test
  public void deleteLast() {
    setTokens(Tokens.MUL);
    select(0, false);

    press(Key.DELETE, ModifierKey.CONTROL);

    assertTokens();
  }

  @Test
  public void backspaceInFirstPositionDoesNothing() {
    setTokens(Tokens.PLUS);
    select(0, true);

    backspace();

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void deleteInlastPositionDoesNothing() {
    setTokens(Tokens.PLUS);
    select(0, false);

    del();

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void tokenMergeWithDel() {
    setTokens(Tokens.PLUS, Tokens.PLUS);
    select(0, false);

    del();

    assertTokens(Tokens.INCREMENT);
  }

  @Test
  public void tokenMergeWithBackspace() {
    setTokens(Tokens.PLUS, Tokens.PLUS);
    select(1, true);

    backspace();

    assertTokens(Tokens.INCREMENT);
    TextCell text = (TextCell) myCellContainer.focusedCell.get();
    assertEquals(1, (int) text.caretPosition().get());
  }

  @Test
  public void changedTokenWithBackspace() {
    setTokens(Tokens.INCREMENT);
    select(0, false);

    backspace();

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void tokenMergeLeadingToErrorToken() {
    setTokens(Tokens.PLUS, Tokens.MUL);
    select(0, false);

    del();

    assertTokens(new ErrorToken("+*"));
  }

  @Test
  public void unselectableTokenNavigation() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(2, true);

    left();

    assertSelected(0);
  }

  @Test
  public void deleteDotWithBackspace() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(2, true);

    backspace();

    assertTokens(Tokens.ID, Tokens.ID);
  }

  @Test
  public void deleteDotWithDel() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(0, false);

    del();

    assertTokens(Tokens.ID, Tokens.ID);
  }

  @Test
  public void replaceVarWithId() {
    setTokens(new IdentifierToken("i"));
    select(0, false);

    type("d");

    assertTrue(container.expr.get() instanceof IdExpr);
  }


  @Test
  public void spaceLeadsToErrorToken() {
    setTokens(Tokens.PLUS);
    select(0, false);

    type(' ');

    assertTokens(Tokens.PLUS, new ErrorToken(""));
  }

  @Test
  public void backspaceAfterNoSpaceToRightEnablesAutoDelete() {
    setTokens(Tokens.LP, Tokens.ID);
    select(1, false);

    backspace();
    backspace();

    assertTokens(Tokens.LP);
  }

  @Test
  public void delBeforeNoSpaceToLeftEnablesAutoDeletion() {
    setTokens(Tokens.ID, Tokens.RP);
    select(0, true);

    del();
    del();

    assertTokens(Tokens.RP);
  }

  @Test
  public void noExtraPositionInParens() {
    setTokens(Tokens.LP, new IntValueToken(2), Tokens.RP);
    select(1, false);

    backspace();

    assertSelectedEnd(0);
    assertTokens(Tokens.LP, Tokens.RP);
  }

  @Test
  public void backspaceInSeqOfRP() {
    setTokens(Tokens.RP, Tokens.RP);
    select(1, false);

    backspace();

    assertSelectedEnd(0);
    assertTokens(Tokens.RP);
  }

  @Test
  public void delInSeqOnLP() {
    setTokens(Tokens.LP, Tokens.LP);
    select(0, true);

    del();

    assertSelected(0);
    assertTokens(Tokens.LP);
  }

  @Test
  public void splitTokenWithSpace() {
    setTokens(Tokens.INCREMENT);
    select(0, 1);

    type(' ');

    assertTokens(Tokens.PLUS, Tokens.PLUS);
  }

  @Test
  public void tokenSplitWithAnotherToken() {
    type("239");
    left();
    type("+");

    assertTokens(new IntValueToken(23), Tokens.PLUS, new IntValueToken(9));
    assertTrue(container.expr.get() instanceof PlusExpr);
  }

  @Test
  public void errorTokenMerge() {
    setTokens(new IntValueToken(44));
    select(0, true);
    right();

    type("  ");
    left();
    del();

    assertTokens(new IntValueToken(4), new IntValueToken(4));
  }

  @Test
  public void menuActivationDuringTyping() {
    setTokens(new IdentifierToken("i"));
    select(0, false);
    backspace();

    complete();
    type("+");

    assertTrue(isCompletionActive());
  }

  @Test
  public void menuDeactivationDuringTyping() {
    setTokens(Tokens.PLUS);
    select(0, false);
    complete();

    type("+");

    assertFalse(isCompletionActive());
  }

  @Test
  public void endRtWithActivatedMenu() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    type("+");

    assertTokens(Tokens.DOT, Tokens.PLUS);
    assertTrue(isCompletionActive());
  }

  @Test
  public void endRtCompletionException() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    complete();

    assertTrue(isCompletionActive());
  }

  @Test
  public void endRtWhichLeadsToFocusAssertion() {
    setTokens(Tokens.ID, Tokens.DOT);
    select(1, false);

    complete();
    down();
    down();
    down();
    enter();

    assertTokens(Tokens.ID, Tokens.DOT, Tokens.MUL);
  }

  @Test
  public void endRtWithNoActivatedMenu() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    type(")");

    assertTokens(Tokens.DOT, Tokens.RP);
    assertFalse(isCompletionActive());
  }


  @Test
  public void endRTWithAsyncCompletion() {
    setTokens(Tokens.DOT);
    select(0, false);

    complete();
    type("async");
    enter();

    assertFalse(isCompletionActive());

    Token token = sync.tokens().get(1);
    assertTrue(token instanceof ValueToken && ((ValueToken) token).value() instanceof AsyncValueExpr);
  }


  @Test
  public void valueTokenParse() {
    type("value");

    assertTrue(sync.valid().get());
    assertTrue(container.expr.get() instanceof ValueExpr);
  }

  @Test
  public void valueTokenCompletion() {
    setTokens(new ValueToken(new ValueExpr(), new ValueExprCloner()));
    select(0, true);

    complete();
    type('+');
    enter();

    assertTokens(Tokens.PLUS);
  }

  @Test
  public void valueTokenLeftPrefix() {
    setTokens(new ValueToken(new ComplexValueExpr(), new ComplexValueCloner()));
    select(0, false);

    type("!");

    assertTrue(sync.tokens().size() == 2);
    assertTrue(sync.tokens().get(0) instanceof ValueToken);
    assertTrue(sync.tokens().get(1).equals(Tokens.FACTORIAL));
  }

  @Test
  public void valueTokenTransform() {
    type("value+value");

    assertTrue(sync.valid().get());
    assertTrue(container.expr.get() instanceof PlusExpr);
  }

  @Test
  public void complexValueTokenTransform() {
    setTokens(createComplexToken());
    select(0, false);

    type("+id");

    assertTrue(sync.valid().get());
    assertTrue(container.expr.get() instanceof PlusExpr);
    assertTrue(((PlusExpr) container.expr.get()).right.get() instanceof IdExpr);
  }

  @Test
  public void onCreateCalledForComplexToken() {
    type("aaaa");

    ComplexValueExpr expr = (ComplexValueExpr) ((ValueToken) sync.tokens().get(0)).value();
    Mapper<?, ? extends Cell> exprMapper = (Mapper<?, ? extends Cell>) mapper.getDescendantMapper(expr);
    HorizontalCell view = (HorizontalCell) exprMapper.getTarget();

    assertFocused(view.children().get(1));
  }

  @Test
  public void replaceValueTokens() {
    type("aaaa");

    select(0, true);
    complete();
    type("aaaa");

    assertEquals(1, sync.tokens().size());
  }

  @Test
  public void valueTokenDelete() {
    setTokens(new ValueToken(new ValueExpr(), new ValueExprCloner()), Tokens.RP);
    select(0, true);

    press(Key.BACKSPACE);

    assertTokens(Tokens.RP);
  }

  @Test
  public void tokenUpdateAfterReparse() {
    setTokens(new IdentifierToken("x"), Tokens.LP);

    select(1, false);
    type(")");

    assertTokens(new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP);
  }

  @Test
  public void tokenUpdateAfterReparseAndSplit() {
    setTokens(new IdentifierToken("id*"), new IdentifierToken("x"), Tokens.LP, Tokens.RP);

    select(0, false);
    left();
    type(" ");

    assertTokens(Tokens.ID, Tokens.MUL, new IdentifierToken("x"), Tokens.LP_CALL, Tokens.RP);
  }

  @Test
  public void tokenFocusAfterReparseAndSplitGivesDifferentTokens() {
    setTokens(new IdentifierToken("func("), Tokens.RP);

    select(0, false);
    left();
    type(" ");

    Cell focusedCell = sync.target().getContainer().focusedCell.get();
    assertTrue(sync.getSource(focusedCell) instanceof CallExpr);
  }

  @Test
  public void selectUpFromBottomInCorrectOrder() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(2, false);

    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 3);
  }

  @Test
  public void selectUpFromComplexToken() {
    setTokens(createComplexToken());

    select(0, true);

    assertNoSelection();

    press(KeyStrokeSpecs.SELECT_UP);

    assertSelected(0);
  }

  @Test
  public void selectDownFromComplexToken() {
    setTokens(createComplexToken());

    select(0, true);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelected(0);

    press(KeyStrokeSpecs.SELECT_DOWN);

    assertNoSelection();
  }


  @Test
  public void selectionDeleteWithDelDeleteAll() {
    setTokens(Tokens.LP, Tokens.RP);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);
    del();

    assertTokens();
  }

  @Test
  public void selectionDeleteWithDelInTheMiddle() {
    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    del();

    assertTokens(Tokens.ID, Tokens.ID);
    assertSelected(0);
  }

  @Test
  public void selectionDeleteWithDelAtTheEnd() {
    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    select(1, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    del();

    assertTokens(Tokens.ID, Tokens.ID);
    assertSelected(1);
  }

  @Test
  public void selectionDeleteWithTyping() {
    setTokens(Tokens.ID, Tokens.ID);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    type("+");

    assertTokens(Tokens.PLUS);
    assertSelected(0);
  }

  @Test
  public void selectUpWithoutSelection() {
    setTokens(Tokens.ID, Tokens.PLUS, Tokens.ID);
    select(0, true);

    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 1);
  }

  @Test
  public void selectUpWithSelectionNoParse() {
    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    select(0, true);

    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 3);
  }

  @Test
  public void selectUpDoesntConsumeInEmpty() {
    setTokens();
    sync.placeholder().focus();

    KeyEvent event = press(KeyStrokeSpecs.SELECT_UP);
    assertFalse(event.isConsumed());
  }

  @Test
  public void selectUpSelctionWithParse() {
    setTokens(Tokens.ID, Tokens.INCREMENT, Tokens.INCREMENT);

    select(0, true);

    press(KeyStrokeSpecs.SELECT_UP);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 2);
  }

  @Test
  public void selectUpInsideOfComplexValueToken() {
    ComplexValueExpr complexExpr = new ComplexValueExpr();
    setTokens(new ValueToken(complexExpr, new ComplexValueCloner()));

    Cell first = Composites.firstFocusable(sync.tokenCells().get(0));
    first.focus();

    press(KeyStrokeSpecs.SELECT_UP);

    assertSelection(0, 1);
  }

  @Test
  public void selectDownClearsSelection() {
    setTokens(Tokens.ID, Tokens.INCREMENT, Tokens.INCREMENT);

    select(0, true);
    sync.select(Range.closed(0, 1));

    press(KeyStrokeSpecs.SELECT_DOWN);

    assertNoSelection();
    assertSelected(0);
  }

  @Test
  public void selectDownNoParse() {
    setTokens(Tokens.ID, Tokens.ID);
    select(0, true);

    sync.select(Range.closed(0, 2));

    press(KeyStrokeSpecs.SELECT_DOWN);

    assertNoSelection();
    assertSelected(0);
  }

  @Test
  public void selectDownDownWithParse() {
    setTokens(Tokens.ID, Tokens.INCREMENT, Tokens.INCREMENT);

    select(0, true);
    sync.select(Range.closed(0, 3));

    press(KeyStrokeSpecs.SELECT_DOWN);

    assertSelection(0, 2);
    assertSelected(0);
  }

  @Test
  public void clearAll() {
    setTokens(Tokens.ID, Tokens.ID);

    select(0, true);

    press(Key.RIGHT, ModifierKey.SHIFT);
    press(Key.RIGHT, ModifierKey.SHIFT);

    del();

    assertTokens();

    assertFocused(sync.placeholder());
  }

  @Test
  public void clearFirstPart() {
    setTokens(Tokens.ID, Tokens.ID);

    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    del();

    Cell tokenCell = targetCell.children().get(0);
    assertFocused(tokenCell);
    assertTrue(Positions.isHomePosition(tokenCell));
    assertTokens(Tokens.ID);
  }

  @Test
  public void clearLastPart() {
    setTokens(Tokens.ID, Tokens.ID);

    select(1, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    del();

    Cell tokenCell = targetCell.children().get(0);
    assertFocused(tokenCell);
    assertTrue(Positions.isEndPosition(tokenCell));
    assertTokens(Tokens.ID);
  }

  @Test
  public void statePersistence() {
    CellStateHandler handler = targetCell.get(CellStateHandler.PROPERTY);

    setTokens(Tokens.ID, Tokens.ID, Tokens.ID);
    CellState state = handler.saveState(targetCell);

    select(1, true);
    type(" id");

    handler.restoreState(targetCell, state);

    assertTokens(Tokens.ID, Tokens.ID, Tokens.ID);
  }

  @Test
  public void valueTokenStatePersistence() {
    CellStateHandler handler = targetCell.get(CellStateHandler.PROPERTY);

    ValueExpr valExpr = new ValueExpr();
    setTokens(new ValueToken(valExpr, new ValueExprCloner()), Tokens.ID);

    CellState state = handler.saveState(targetCell);
    valExpr.val.set("z");

    handler.restoreState(targetCell, state);

    ValueToken newVal = (ValueToken) sync.tokens().get(0);
    assertNull(((ValueExpr) newVal.value()).val.get());
  }

  @Test
  public void unselectableSelection() {
    setTokens(Tokens.ID, Tokens.DOT, Tokens.ID);
    select(0, true);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    assertEquals(Range.closed(0, 2), sync.selection());
  }

  @Test
  public void caretPositionSavingOnCompletion() {
    setTokens(Tokens.PLUS);
    select(0, true);
    type("+");

    assertTokens(Tokens.INCREMENT);
    assertSelected(0);
    assertEquals(1, (int) ((TextCell) sync.tokenCells().get(0)).caretPosition().get());
  }

  @Test
  public void selectionIndex() {
    setTokens(Tokens.PLUS, Tokens.PLUS, Tokens.PLUS);
    select(1, false);

    Cell focusedCell = sync.target().getContainer().focusedCell.get();
    assertSame(sync.tokenCells().get(1), focusedCell);
  }

  @Test
  public void valueTokenShouldBeFromModel() {
    ValueExpr expr = new ValueExpr();
    container.expr.set(expr);

    assertNotNull(mapper.getDescendantMapper(expr));
  }

  @Test
  public void valueTokensShouldBeFromModelOnReparse() {
    setTokens(new ValueToken(new ValueExpr(), new ValueExprCloner()));

    Expr expr = container.expr.get();
    assertNotNull(mapper.getDescendantMapper(expr));
  }

  @Test
  public void toRightSimplePair() {
    setTokens(Tokens.LP, Tokens.RP);

    assertSame(tokenCell(1), sync.getPair((TextTokenCell) tokenCell(0)));
  }

  @Test
  public void toLeftSimplePair() {
    setTokens(Tokens.LP, Tokens.RP);

    assertSame(tokenCell(0), sync.getPair((TextTokenCell) tokenCell(1)));
  }

  @Test
  public void pairingIndexOutOfBoundsException() {
    setTokens(Tokens.LP, Tokens.RP);
    select(0, false);
    type("239");

    assertTokens(Tokens.LP, new IntValueToken(239), Tokens.RP);
  }

  @Test
  public void nestedPairing() {
    setTokens(Tokens.LP, Tokens.LP, Tokens.RP, Tokens.RP);

    assertSame(tokenCell(0), sync.getPair((TextTokenCell) tokenCell(3)));
  }

  @Test
  public void noPair() {
    setTokens(Tokens.LP);

    assertNull(sync.getPair((TextTokenCell) tokenCell(0)));
  }

  @Test
  public void additionalAsyncCompletion() {
    setTokens();
    select(0, true);

    complete();
    type("async");
    enter();

    assertSelected(0);

    Token token = sync.tokens().get(0);
    assertTrue(token instanceof ValueToken && ((ValueToken) token).value() instanceof AsyncValueExpr);
  }

  @Test
  public void typingWithCompletionMenuInPlaceholder() {
    setTokens();
    select(0, true);

    complete();
    type("+");

    assertTrue(isCompletionActive());
  }

  @Test
  public void dynamicSpec() {
    setTokens(Tokens.ID, Tokens.PLUS, Tokens.ID);
    assertTrue(container.expr.get() instanceof PlusExpr);

    mapper.hybridSyncSpec.set(new ExprHybridEditorSpec(Tokens.MUL, Tokens.PLUS));
    assertTrue(container.expr.get() instanceof MulExpr);
  }

  @Test
  public void emptyTokenCausesError() {
    setTokens(new IdentifierToken("a"), Tokens.PLUS, new IdentifierToken("b"));

    select(0, false);
    backspace();

    assertTrue(MessageController.hasError(sync.target()));
  }

  @Test
  public void hideTokensInMenu() {
    sync.setHideTokensInMenu(true);
    complete();
    type("aaa");

    assertEquals(0, sync.tokens().size());
  }

  @Test
  public void hideTokensInMenuForHybridWrapperRole() {
    HybridWrapperRole<Object, Expr, Expr> hybridWrapperRole = new HybridWrapperRole<>(mapper.hybridSyncSpec.get(), null, null, true);
    CompletionSupplier roleCompletion = hybridWrapperRole.createRoleCompletion(mapper, null, null);
    CompletionParameters completionParameters = new BaseCompletionParameters() {
      @Override
      public boolean isMenu() {
        return true;
      }
    };
    Iterable<CompletionItem> completionItems = roleCompletion.get(completionParameters);
    assertTrue(FluentIterable.from(completionItems).isEmpty());
  }
}
