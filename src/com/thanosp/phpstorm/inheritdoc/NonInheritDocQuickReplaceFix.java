package com.thanosp.phpstorm.inheritdoc;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;

public class NonInheritDocQuickReplaceFix implements LocalQuickFix {
    static final NonInheritDocQuickReplaceFix INSTANCE = new NonInheritDocQuickReplaceFix();
    @NotNull
    @Override
    public String getName() {
        return "Replace docblock with inheritDoc";
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

        InheritDocUtil.replaceDocblockForNamedElement(phpNamedElement);
    }
}
