package com.thanosp.phpstorm.inheritdoc;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to check elements for inheritdoc and
 * register the problem to be highlighted along with a fix
 */
public class MissingInheritDocInspection extends PhpInspection {
    private boolean CHECK_METHOD = true;

    private void checkElement(@NotNull PhpNamedElement namedElement, @NotNull ProblemsHolder holder) {
        PhpDocComment docComment = namedElement.getDocComment();

        ASTNode nameNode = namedElement.getNameNode();
        if (nameNode == null) {
            return;
        }

        // no valid parent with docblock
        if (!InheritDocUtil.namedElementHasParentWithDoc(namedElement)) {
            return;
        }

        // already has inheritdoc
        if (docComment != null && docComment.hasInheritDocTag()) {
            return;
        }

        boolean replaceDocBlock = docComment != null && !docComment.hasInheritDocTag();
        LocalQuickFix[] fixes = new LocalQuickFix[] {
            replaceDocBlock ? NonInheritDocQuickReplaceFix.INSTANCE : MissingInheritDocQuickAddFix.INSTANCE
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
            public void visitPhpMethod(Method method) {
                if (MissingInheritDocInspection.this.CHECK_METHOD) {
                    MissingInheritDocInspection.this.checkElement(method, problemsHolder);
                }
            }
        };
    }
}
