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

import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.values.Color;

class TextCellToEditorAdapter extends TextEditorCell {
  TextCellToEditorAdapter(TextCell source) {
   super(source);
  }

  @Override
  public TextCell getCell() {
    return (TextCell) super.getCell();
  }

  @Override
  public Property<String> text() {
    return getCell().text();
  }

  @Override
  public Property<Color> textColor() {
    return getCell().textColor();
  }

  @Override
  public Property<Boolean> selectionVisible() {
    return getCell().selectionVisible();
  }

  @Override
  public Property<Integer> selectionStart() {
    return getCell().selectionStart();
  }

  @Override
  public Property<Boolean> caretVisible() {
    return getCell().caretVisible();
  }

  @Override
  public Property<Integer> caretPosition() {
    return getCell().caretPosition();
  }

  @Override
  public void scrollToCaret() {
    getCell().scrollToCaret();
  }

  @Override
  public int getCaretAt(int x) {
    return getCell().getCaretAt(x);
  }

  @Override
  public int getCaretOffset(int caret) {
    return getCell().getCaretOffset(caret);
  }
}
