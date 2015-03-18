package jetbrains.jetpad.hybrid.util;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.completion.*;
import jetbrains.jetpad.hybrid.BaseCompleter;
import jetbrains.jetpad.hybrid.CompletionContext;
import jetbrains.jetpad.hybrid.HybridPositionSpec;
import jetbrains.jetpad.hybrid.HybridSynchronizer;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.generic.RoleCompletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;

public class HybridWrapperRole<ContainerT, WrapperT, TargetT> implements RoleCompletion<ContainerT, WrapperT> {
  private HybridPositionSpec<TargetT> mySpec;
  private Supplier<WrapperT> myFactory;
  private Function<Mapper<?, ?>, HybridSynchronizer<TargetT>> mySyncProvider;


  public HybridWrapperRole(HybridPositionSpec<TargetT> spec, Supplier<WrapperT> targetFactory, Function<Mapper<?, ?>, HybridSynchronizer<TargetT>> syncProvider) {
    mySpec = spec;
    myFactory = targetFactory;
    mySyncProvider = syncProvider;
  }

  @Override
  public List<CompletionItem> createRoleCompletion(CompletionParameters ctx, final Mapper<?, ?> mapper, ContainerT contextNode, final Role<WrapperT> target) {
    List<CompletionItem> result = new ArrayList<>();

    final BaseCompleter completer = new BaseCompleter() {
      @Override
      public Runnable complete(int selectionIndex, Token... tokens) {
        WrapperT targetItem = myFactory.get();
        target.set(targetItem);
        Mapper<?, ?> targetItemMapper =  mapper.getDescendantMapper(targetItem);
        HybridSynchronizer<?> sync = mySyncProvider.apply(targetItemMapper);
        sync.setTokens(Arrays.asList(tokens));
        return sync.selectOnCreation(selectionIndex, LAST);

      }
    };

    for (CompletionItem ci : mySpec.getTokenCompletion(new Function<Token, Runnable>() {
      @Override
      public Runnable apply(Token input) {
        return completer.complete(input);
      }
    }).get(ctx)) {
      result.add(new WrapperCompletionItem(ci) {
        @Override
        public boolean isLowPriority() {
          return true;
        }
      });
    }

    if (ctx.isMenu()) {
      CompletionSupplier compl = mySpec.getAdditionalCompletion(new CompletionContext() {
        @Override
        public int getTargetIndex() {
          return 0;
        }

        @Override
        public List<Token> getPrefix() {
          return Collections.emptyList();
        }

        @Override
        public List<Cell> getViews() {
          return Collections.emptyList();
        }

        @Override
        public List<Token> getTokens() {
          return Collections.emptyList();
        }

        @Override
        public List<Object> getObjects() {
          return Collections.emptyList();
        }

        @Override
        public Mapper<?, ?> getContextMapper() {
          return mapper;
        }

        @Override
        public Object getTarget() {
          return target.get();
        }
      }, completer);
      result.addAll(compl.get(new BaseCompletionParameters()));
    }
    return result;
  }
}