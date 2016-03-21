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

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.cell.Cell;

import java.util.List;

public interface CompletionContext {
  CompletionContext EMPTY = new EmptyCompletionContext();

  int getTargetIndex();

  List<Token> getPrefix();

  List<Cell> getViews();

  List<Token> getTokens();

  List<Object> getObjects();

  Mapper<?, ?> getContextMapper();
  Object getTarget();
}