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
package jetbrains.jetpad.cell.indent;

import com.google.common.base.Strings;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.model.composite.Composites;

public final class IndentUtil {

  public static void iterateLeaves(Cell cell, Handler<Cell> handler) {
    for (Cell child : cell.children()) {
      if (!Composites.isVisible(child)) continue;
      if (child instanceof IndentCell) {
        iterateLeaves(child, handler);
      } else {
        handler.handle(child);
      }
    }
  }

  public static String getIndentText(int size, int numSpaces) {
    String oneIndent = Strings.repeat(" ", numSpaces);
    return Strings.repeat(oneIndent, size);
  }

  private IndentUtil() {
  }

}
