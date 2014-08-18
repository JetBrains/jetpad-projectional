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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.property.Property;

import java.util.HashMap;
import java.util.Map;

public class SvgLineElement extends SvgStylableElement {
  protected static Map<String, SvgAttrSpec<?>> myAttrInfo = new HashMap<>(SvgStylableElement.myAttrInfo);

  public static final SvgAttrSpec<Double> X1 = SvgAttrSpec.initAttrSpec("x1", myAttrInfo);
  public static final SvgAttrSpec<Double> Y1 = SvgAttrSpec.initAttrSpec("y1", myAttrInfo);
  public static final SvgAttrSpec<Double> X2 = SvgAttrSpec.initAttrSpec("x2", myAttrInfo);
  public static final SvgAttrSpec<Double> Y2 = SvgAttrSpec.initAttrSpec("y2", myAttrInfo);

  @Override
  protected Map<String, SvgAttrSpec<?>> getAttrInfo() {
    return myAttrInfo;
  }

  public Property<Double> getX1() {
    return getProp(X1);
  }

  public Property<Double> getY1() {
    return getProp(Y1);
  }

  public Property<Double> getX2() {
    return getProp(X2);
  }

  public Property<Double> getY2() {
    return getProp(Y2);
  }
}
