/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.format;

import lombok.RequiredArgsConstructor;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.marker.ImplicitReturn;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.Space;

@RequiredArgsConstructor
public class MinimumViableSpacingVisitor<P> extends JavaIsoVisitor<P> {
    @Nullable
    private final Tree stopAfter;

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, P p) {
        J.ClassDeclaration c = super.visitClassDeclaration(classDecl, p);

        boolean first = c.getLeadingAnnotations().isEmpty();
        if (!c.getModifiers().isEmpty()) {
            if (!first && Space.firstPrefix(c.getModifiers()).getWhitespace().isEmpty()) {
                c = c.withModifiers(Space.formatFirstPrefix(c.getModifiers(),
                        c.getModifiers().iterator().next().getPrefix().withWhitespace(" ")));
            }
            if (c.getModifiers().size() > 1) {
                c = c.withModifiers(ListUtils.map(c.getModifiers(), (index, modifier) -> {
                    if (index > 0 && modifier.getPrefix().getWhitespace().isEmpty()) {
                        return modifier.withPrefix(modifier.getPrefix().withWhitespace(" "));
                    }
                    return modifier;
                }));
            }
            first = false;
        }

        if (c.getPadding().getKind().getPrefix().isEmpty()) {
            if (!first) {
                c = c.getPadding().withKind(c.getPadding().getKind().withPrefix(
                        c.getPadding().getKind().getPrefix().withWhitespace(" ")));
            }
            first = false;
        }

        if (!first && c.getName().getPrefix().getWhitespace().isEmpty()) {
            c = c.withName(c.getName().withPrefix(c.getName().getPrefix().withWhitespace(" ")));
        }

        J.ClassDeclaration.Padding padding = c.getPadding();
        JContainer<J.TypeParameter> typeParameters = padding.getTypeParameters();
        if (typeParameters != null && !typeParameters.getElements().isEmpty()) {
            if (!first && !typeParameters.getBefore().getWhitespace().isEmpty()) {
                c = padding.withTypeParameters(typeParameters.withBefore(typeParameters.getBefore().withWhitespace(" ")));
            }
        }

        if (c.getPadding().getExtends() != null) {
            Space before = c.getPadding().getExtends().getBefore();
            if (before.getWhitespace().isEmpty()) {
                c = c.getPadding().withExtends(c.getPadding().getExtends().withBefore(before.withWhitespace(" ")));
            }
        }

        if (c.getPadding().getImplements() != null) {
            Space before = c.getPadding().getImplements().getBefore();
            if (before.getWhitespace().isEmpty()) {
                c = c.getPadding().withImplements(c.getPadding().getImplements().withBefore(before.withWhitespace(" ")));
                c = c.withImplements(ListUtils.mapFirst(c.getImplements(), anImplements -> {
                    if (anImplements.getPrefix().getWhitespace().isEmpty()) {
                        return anImplements.withPrefix(anImplements.getPrefix().withWhitespace(" "));
                    }
                    return anImplements;
                }));
            }
        }

        return c;
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, P p) {
        J.MethodDeclaration m = super.visitMethodDeclaration(method, p);

        boolean first = m.getLeadingAnnotations().isEmpty();
        if (!m.getModifiers().isEmpty()) {
            if (!first && Space.firstPrefix(m.getModifiers()).getWhitespace().isEmpty()) {
                m = m.withModifiers(Space.formatFirstPrefix(m.getModifiers(),
                        m.getModifiers().iterator().next().getPrefix().withWhitespace(" ")));
            }
            if (m.getModifiers().size() > 1) {
                m = m.withModifiers(ListUtils.map(m.getModifiers(), (index, modifier) -> {
                    if (index > 0 && modifier.getPrefix().getWhitespace().isEmpty()) {
                        return modifier.withPrefix(modifier.getPrefix().withWhitespace(" "));
                    }
                    return modifier;
                }));
            }
            first = false;
        }

        J.TypeParameters typeParameters = m.getAnnotations().getTypeParameters();
        if (typeParameters != null && !typeParameters.getTypeParameters().isEmpty()) {
            if (!first && typeParameters.getPrefix().getWhitespace().isEmpty()) {
                m = m.getAnnotations().withTypeParameters(
                        typeParameters.withPrefix(
                                typeParameters.getPrefix().withWhitespace(" ")
                        )
                );
            }
            first = false;
        }

        if (m.getReturnTypeExpression() != null && m.getReturnTypeExpression().getPrefix().getWhitespace().isEmpty()) {
            if (!first) {
                m = m.withReturnTypeExpression(m.getReturnTypeExpression()
                        .withPrefix(m.getReturnTypeExpression().getPrefix().withWhitespace(" ")));
            }
            first = false;
        }
        if (!first) {
            m = m.withName(m.getName().withPrefix(m.getName().getPrefix().withWhitespace(" ")));
        }

        if (m.getPadding().getThrows() != null) {
            Space before = m.getPadding().getThrows().getBefore();
            if (before.getWhitespace().isEmpty()) {
                m = m.getPadding().withThrows(m.getPadding().getThrows().withBefore(before.withWhitespace(" ")));
            }
        }

        return m;
    }

    @Override
    public J.Return visitReturn(J.Return return_, P p) {
        J.Return r = super.visitReturn(return_, p);
        if (r.getExpression() != null && r.getExpression().getPrefix().getWhitespace().isEmpty() &&
            !return_.getMarkers().findFirst(ImplicitReturn.class).isPresent()) {
            r = r.withExpression(r.getExpression().withPrefix(r.getExpression().getPrefix().withWhitespace(" ")));
        }
        return r;
    }

    @Override
    public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, P p) {
        J.VariableDeclarations v = super.visitVariableDeclarations(multiVariable, p);

        boolean first = v.getLeadingAnnotations().isEmpty();

        /*
         * We need at least one space between multiple modifiers, otherwise we could get a run-on like "publicstaticfinal".
         * Note, this is applicable anywhere that modifiers can exist, such as class declarations, etc.
         */
        if (!v.getModifiers().isEmpty()) {
            boolean needFirstSpace = !first;
            v = v.withModifiers(
                    ListUtils.map(v.getModifiers(), (index, modifier) -> {
                        if (index != 0 || needFirstSpace) {
                            if (modifier.getPrefix().getWhitespace().isEmpty()) {
                                modifier = modifier.withPrefix(modifier.getPrefix().withWhitespace(" "));
                            }
                        }
                        return modifier;
                    })
            );
            first = false;
        }

        if (!first && v.getTypeExpression() != null) {
            if (v.getTypeExpression().getPrefix().getWhitespace().isEmpty()) {
                v = v.withTypeExpression(v.getTypeExpression().withPrefix(v.getTypeExpression().getPrefix().withWhitespace(" ")));
            }
        }

        J firstEnclosing = getCursor().getParentOrThrow().firstEnclosing(J.class);
        if (!(firstEnclosing instanceof J.Lambda)) {
            if (Space.firstPrefix(v.getVariables()).getWhitespace().isEmpty()) {
                v = v.withVariables(Space.formatFirstPrefix(v.getVariables(),
                        v.getVariables().iterator().next().getPrefix().withWhitespace(" ")));
            }
        }

        return v;
    }

    @Nullable
    @Override
    public J postVisit(J tree, P p) {
        if (stopAfter != null && stopAfter.isScope(tree)) {
            getCursor().putMessageOnFirstEnclosing(JavaSourceFile.class, "stop", true);
        }
        return super.postVisit(tree, p);
    }

    @Nullable
    @Override
    public J visit(@Nullable Tree tree, P p) {
        if (getCursor().getNearestMessage("stop") != null) {
            return (J) tree;
        }
        return super.visit(tree, p);
    }
}
