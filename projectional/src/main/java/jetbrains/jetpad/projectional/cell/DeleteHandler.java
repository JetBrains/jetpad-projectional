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
package jetbrains.jetpad.projectional.cell;

/**
 * Handler of abstract delete event for cells. These delete event might be triggered by
 * key events, mouse events, or in other ways.
 */
public interface DeleteHandler {
  DeleteHandler EMPTY = new DeleteHandler() {
    @Override
    public boolean canDelete() {
      return false;
    }

    @Override
    public Runnable delete() {
      throw new UnsupportedOperationException();
    }
  };

  boolean canDelete();
  Runnable delete();
}