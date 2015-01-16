package com.thanosp.phpstorm.inheritdoc;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to check elements for inheritdoc and
 * register the problem to be highlighted along with a fix
 */
public class InheritDocInspection extends PhpInspection {
    public boolean CHECK_CONSTANT = false;
    public boolean CHECK_FIELD = false;
    public boolean CHECK_CLASS_CONSTANT = false;

    public boolean CHECK_FUNCTION = true;
    public boolean CHECK_CLASS = true;
    public boolean CHECK_METHOD = true;

    private void checkElement(@NotNull PhpNamedElement namedElement, @NotNull ProblemsHolder holder) {
        if (namedElement.getDocComment() == null) {
            return;
        }

        if (! namedElement.getDocComment().hasInheritDocTag()) {
            return;
        }

        ASTNode nameNode = namedElement.getNameNode();
        if (nameNode == null) {
            return;
        }

        LocalQuickFix[] fixes = new LocalQuickFix[]{
                InheritDocQuickReplaceFix.INSTANCE,
                InheritDocQuickRemoveFix.INSTANCE
        };
        holder.registerProblem(
            nameNode.getPsi(),
            "Uses inheritDoc which is only readable by documentation tools and is obsolete unless combined with new text",
            fixes[0],
            fixes[1]
        );
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpConstant(Constant constant) {
                if (InheritDocInspection.this.CHECK_CONSTANT) {
                    InheritDocInspection.this.checkElement(constant, problemsHolder);
                }
            }

            public void visitPhpFunction(Function function) {
                if (InheritDocInspection.this.CHECK_FUNCTION) {
                    InheritDocInspection.this.checkElement(function, problemsHolder);
                }
            }

            public void visitPhpClass(PhpClass clazz) {
                if (InheritDocInspection.this.CHECK_CLASS) {
                    InheritDocInspection.this.checkElement(clazz, problemsHolder);
                }
            }

            public void visitPhpMethod(Method method) {
                if (InheritDocInspection.this.CHECK_METHOD) {
                    InheritDocInspection.this.checkElement(method, problemsHolder);
                }
            }

            public void visitPhpField(Field field) {
                if (field.isConstant() && InheritDocInspection.this.CHECK_CLASS_CONSTANT) {
                    InheritDocInspection.this.checkElement(field, problemsHolder);
                } else if(InheritDocInspection.this.CHECK_FIELD) {
                    InheritDocInspection.this.checkElement(field, problemsHolder);
                }
            }
        };
    }
}
