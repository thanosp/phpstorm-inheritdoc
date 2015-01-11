package com.thanosp.phpstorm.inheritdoc;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;

public class InheritDocIntention extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(Project project, Editor editor, PsiElement psiElement) throws IncorrectOperationException {

        final PhpNamedElement phpNamedElement = PsiTreeUtil.getParentOfType(psiElement, PhpNamedElement.class);
        InheritDocUtil.fixInheritDocForNamedElement(phpNamedElement);
    }

    @Override
    public boolean isAvailable(Project project, Editor editor, PsiElement psiElement) {
        PhpNamedElement phpNamedElement = PsiTreeUtil.getParentOfType(psiElement, PhpNamedElement.class);
        if (null == phpNamedElement || phpNamedElement.getDocComment() == null) {
            return false;
        }

        return phpNamedElement.getDocComment().hasInheritDocTag();
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "PHPDoc";
    }
}
