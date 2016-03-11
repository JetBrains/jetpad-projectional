package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;

public interface SimpleHybridEditorSpec<SourceT> extends CompletionSpec {
  PrettyPrinter<? super SourceT> getPrettyPrinter();
  PairSpec getPairSpec();

  CompletionSupplier getAdditionalCompletion(CompletionContext ctx, Completer completer);
}
