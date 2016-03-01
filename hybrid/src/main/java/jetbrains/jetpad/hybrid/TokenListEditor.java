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

import com.google.common.base.Objects;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.hybrid.parser.ParsingContext;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.prettyprint.ParseNode;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TokenListEditor<SourceT> {
  private Property<Boolean> myValid = new ValueProperty<>(true);
  private ParseNode myParseNode;
  private ReadableProperty<HybridEditorSpec<SourceT>> mySpec;
  private boolean myAutoReprint;
  private boolean mySyncing;
  private List<Token> myPrintedTokens;
  private boolean myRestoringState;
  private Registration myChangeReg = Registration.EMPTY;

  final ObservableList<Token> tokens = new ObservableArrayList<>();
  final Property<SourceT> value = new ValueProperty<>();
  final ReadableProperty<Boolean> valid = myValid;

  TokenListEditor(HybridEditorSpec<SourceT> spec, boolean autoReprint, boolean autoReparse) {
    this(Properties.constant(spec), autoReprint, autoReparse);
  }

  TokenListEditor(ReadableProperty<HybridEditorSpec<SourceT>> spec, boolean autoReprint, boolean autoReparse) {
    mySpec = spec;
    myAutoReprint = autoReprint;

    if (autoReparse) {
      tokens.addHandler(new EventHandler<CollectionItemEvent<? extends Token>>() {
        @Override
        public void onEvent(CollectionItemEvent<? extends Token> event) {
          sync(new Runnable() {
            @Override
            public void run() {
              reparse();
            }
          });
        }
      });
    }
    if (myAutoReprint) {
      value.addHandler(new EventHandler<PropertyChangeEvent<SourceT>>() {
        @Override
        public void onEvent(PropertyChangeEvent<SourceT> event) {
          sync(new Runnable() {
            @Override
            public void run() {
              reprintToTokens();
            }
          });
        }
      });
    }
    if (autoReparse) {
      spec.addHandler(new EventHandler<PropertyChangeEvent<HybridEditorSpec<SourceT>>>() {
        @Override
        public void onEvent(PropertyChangeEvent<HybridEditorSpec<SourceT>> event) {
          sync(new Runnable() {
            @Override
            public void run() {
              reparse();
            }
          });
          updateToPrintedTokens();
        }
      });
    }
  }

  ParseNode parseNode() {
    return myParseNode;
  }

  List<Object> objects() {
    if (myParseNode == null) return Collections.emptyList();
    List<Object> result = new ArrayList<>();
    toObjects(myParseNode, result);
    return result;
  }

  private void toObjects(ParseNode node, List<Object> result) {
    for (ParseNode child : node.children()) {
      if (child.value() instanceof Token) {
        result.add(node.value());
      } else {
        toObjects(child, result);
      }
    }
  }

  private void sync(Runnable r) {
    if (mySyncing) return;
    mySyncing = true;
    try {
      r.run();
    } finally {
      mySyncing = false;
    }
  }

  void reparse() {
    if (myRestoringState) return;

    if (tokens.size() == 0) {
      value.set(null);
      myValid.set(true);
      myParseNode = null;
      myPrintedTokens = new ArrayList<>();
      myChangeReg.remove();
      myChangeReg = Registration.EMPTY;
    } else {
      List<Token> toParse = new ArrayList<>();
      for (Token t : tokens) {
        toParse.add(t.copy());
      }

      SourceT result = mySpec.get().getParser().parse(new ParsingContext(toParse));
      if (result != null) {
        value.set(result);
        myValid.set(true);
        reprint();
        if (myPrintedTokens.size() != tokens.size()) {
          throw new IllegalStateException();
        }
      } else {
        myValid.set(false);
        myParseNode = null;
        myPrintedTokens = null;
      }
    }
  }

  void reprintToTokens() {
    PrettyPrinterContext<? super SourceT> ctx = reprint();
    myValid.set(true);
    tokens.clear();
    tokens.addAll(ctx.tokens());
  }

  private PrettyPrinterContext<? super SourceT> reprint() {
    PrettyPrinter<? super SourceT> printer = mySpec.get().getPrettyPrinter();
    PrettyPrinterContext<? super SourceT> ctx = new PrettyPrinterContext<>(printer);
    ctx.print(value.get());
    myParseNode = ctx.result();
    myPrintedTokens = ctx.tokens();

    myChangeReg.remove();
    myChangeReg = ctx.changeSource().addHandler(new EventHandler<Object>() {
      @Override
      public void onEvent(Object event) {
        sync(new Runnable() {
          @Override
          public void run() {
            if (myAutoReprint) {
              reprintToTokens();
            }
          }
        });
      }
    });
    return ctx;
  }

  void updateToPrintedTokens() {
    if (myPrintedTokens == null) return;

    sync(new Runnable() {
      @Override
      public void run() {
        List<Token> newTokens = myPrintedTokens;
        for (int i = 0; i < newTokens.size(); i++) {
          Token newToken = newTokens.get(i);
          Token token = tokens.get(i);
          if (!Objects.equal(newToken, token)) {
            tokens.set(i, newToken);
          }
        }
      }
    });

  }

  void restoreState(List<Token> state) {
    if (myRestoringState) {
      throw new IllegalStateException();
    }
    myRestoringState = true;
    try {
      if (state != null) {
        tokens.clear();
        tokens.addAll(state);
      } else if (!myValid.get()) {
        reprintToTokens();
      }
    } finally {
      myRestoringState = false;
    }
    if (state != null) {
      reparse();
    }
  }

  void dispose() {
    myChangeReg.remove();
    myChangeReg = Registration.EMPTY;
  }
}