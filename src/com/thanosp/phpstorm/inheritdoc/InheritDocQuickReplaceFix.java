package com.thanosp.phpstorm.inheritdoc;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;

public class InheritDocQuickReplaceFix implements LocalQuickFix {
    public static final InheritDocQuickReplaceFix INSTANCE = new InheritDocQuickReplaceFix();
    @NotNull
    @Override
    public String getName() {
        return "1. Replace inheritDoc with the inherited docblock";
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

        boolean replace = true;
        InheritDocUtil.fixInheritDocForNamedElement(phpNamedElement, replace);
    }
}
