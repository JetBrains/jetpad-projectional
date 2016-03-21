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
package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.hybrid.testapp.model.*;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;

class ExprMapperFactory implements MapperFactory<Object,Cell> {
  @Override
  public Mapper<? extends ExprNode, ? extends Cell> createMapper(Object source) {
    if (source instanceof PosValueExpr) {
      return new PosValueExprMapper((PosValueExpr) source);
    }
    if (source instanceof ValueExpr) {
      return new ValueExprMapper((ValueExpr) source);
    }
    if (source instanceof AsyncValueExpr) {
      return new AsyncValueExprMapper((AsyncValueExpr) source);
    }
    if (source instanceof ComplexValueExpr) {
      return new ComplexValueExprMapper((ComplexValueExpr) source);
    }
    if (source instanceof StringExpr) {
      return new StringExprMapper((StringExpr) source);
    }
    throw new IllegalArgumentException("Unknown source: " + source);
  }
}