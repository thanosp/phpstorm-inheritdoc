package com.thanosp.phpstorm.inheritdoc;

import com.intellij.codeInspection.LocalQuickFix;
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
public class MissingInheritDocInspection extends PhpInspection {
    public boolean CHECK_CONSTANT = false;
    public boolean CHECK_FIELD = false;
    public boolean CHECK_CLASS_CONSTANT = false;

    public boolean CHECK_FUNCTION = true;
    public boolean CHECK_CLASS = true;
    public boolean CHECK_METHOD = true;

    private void checkElement(@NotNull PhpNamedElement namedElement, @NotNull ProblemsHolder holder) {
        if (namedElement.getDocComment() != null) {
            return;
        }

        ASTNode nameNode = namedElement.getNameNode();
        if (nameNode == null) {
            return;
        }

        if (!InheritDocUtil.namedElementHasParentWithDoc(namedElement)) {
            return;
        }

        LocalQuickFix[] fixes = new LocalQuickFix[]{
                MissingInheritDocQuickAddFix.INSTANCE
        };
        holder.registerProblem(
            nameNode.getPsi(),
            "Has parent with docblock but does not use inheritDoc",
            fixes[0]
        );
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpConstant(Constant constant) {
                if (MissingInheritDocInspection.this.CHECK_CONSTANT) {
                    MissingInheritDocInspection.this.checkElement(constant, problemsHolder);
                }
            }

            public void visitPhpFunction(Function function) {
                if (MissingInheritDocInspection.this.CHECK_FUNCTION) {
                    MissingInheritDocInspection.this.checkElement(function, problemsHolder);
                }
            }

            public void visitPhpClass(PhpClass clazz) {
                if (MissingInheritDocInspection.this.CHECK_CLASS) {
                    MissingInheritDocInspection.this.checkElement(clazz, problemsHolder);
                }
            }

            public void visitPhpMethod(Method method) {
                if (MissingInheritDocInspection.this.CHECK_METHOD) {
                    MissingInheritDocInspection.this.checkElement(method, problemsHolder);
                }
            }

            public void visitPhpField(Field field) {
                if (field.isConstant() && MissingInheritDocInspection.this.CHECK_CLASS_CONSTANT) {
                    MissingInheritDocInspection.this.checkElement(field, problemsHolder);
                } else if(MissingInheritDocInspection.this.CHECK_FIELD) {
                    MissingInheritDocInspection.this.checkElement(field, problemsHolder);
                }
            }
        };
    }
}
