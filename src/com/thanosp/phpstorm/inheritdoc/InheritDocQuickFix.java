package com.thanosp.phpstorm.inheritdoc;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;

public class InheritDocQuickFix implements LocalQuickFix {
    public static final InheritDocQuickFix INSTANCE = new InheritDocQuickFix();
    @NotNull
    @Override
    public String getName() {
        return "Replace inheritDoc with the inherited docblock text";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "PHPDoc";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor)
    {
        final PhpNamedElement phpNamedElement = PsiTreeUtil.getParentOfType(
                problemDescriptor.getPsiElement(),
                PhpNamedElement.class
        );

        InheritDocUtil.fixInheritDocForNamedElement(phpNamedElement);
    }
}
