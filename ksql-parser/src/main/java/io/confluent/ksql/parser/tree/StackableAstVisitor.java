/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.parser.tree;

import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.Optional;

public class StackableAstVisitor<R, C>
    extends AstVisitor<R, StackableAstVisitor.StackableAstVisitorContext<C>> {

  public R process(final Node node, final StackableAstVisitorContext<C> context) {
    context.push(node);
    try {
      return node.accept(this, context);
    } finally {
      context.pop();
    }
  }

  public static class StackableAstVisitorContext<C> {

    private final LinkedList<Node> stack = new LinkedList<>();
    private final C context;

    public StackableAstVisitorContext(final C context) {
      this.context = requireNonNull(context, "context is null");
    }

    public C getContext() {
      return context;
    }

    private void pop() {
      stack.pop();
    }

    void push(final Node node) {
      stack.push(node);
    }

    public Optional<Node> getPreviousNode() {
      if (stack.size() > 1) {
        return Optional.of(stack.get(1));
      }
      return Optional.empty();
    }
  }
}
