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
package jetbrains.jetpad.projectional.generic;


import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.mapper.Mapper;

/**
 * Provider of completion for particular role
 *
 * @param <ContextT> - context type of this role
 * @param <TargetT> - the type of a value which we set, typically it's a source node of a mapper which contains
 *   typically it's a type of source node of the mapper which contains {@link jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer}
 */
public interface RoleCompletion<ContextT, TargetT> {
  /**
   * @param mapper - either mapper for the current value or if there's no one the mapper which contains
   *               {@link jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer}
   */
  CompletionSupplier createRoleCompletion(
    Mapper<?, ?> mapper,
    ContextT contextNode,
    Role<TargetT> target);
}