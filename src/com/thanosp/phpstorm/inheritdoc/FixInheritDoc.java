package com.thanosp.phpstorm.inheritdoc;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpClassHierarchyUtils;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FixInheritDoc extends AnAction {
    public void actionPerformed(@NotNull AnActionEvent e)
    {
        final PhpNamedElement phpNamedElement = getParentPsiFromEvent(e);

        // no named parent or not doc block
        if (phpNamedElement == null || phpNamedElement.getDocComment() == null) {
            return;
        }

        // no inheritDoc
        if (! phpNamedElement.getDocComment().hasInheritDocTag()) {
            return;
        }

        // inheritDoc must be purged
        new WriteCommandAction.Simple(phpNamedElement.getProject(), phpNamedElement.getContainingFile()) {
            @Override
            protected void run() throws Throwable {

                final ArrayList results = new ArrayList(1);
                if(phpNamedElement instanceof Method) {
                    PhpClassHierarchyUtils.processSuperMembers((PhpClassMember) phpNamedElement, new PhpClassHierarchyUtils.HierarchyClassMemberProcessor() {
                        public boolean process(PhpClassMember method, PhpClass subClass, PhpClass baseClass) {
                            results.add(method);
                            return true;
                        }
                    });
                } else if(phpNamedElement instanceof PhpClass) {
                    results.addAll(PhpClassHierarchyUtils.getImmediateParents((PhpClass)phpNamedElement));
                }

                // delete the inheritdoc to start with. we'll replace or just leave blank
                phpNamedElement.getDocComment().delete();

                PhpNamedElement superMember = (PhpNamedElement) results.get(0);

                String commentString = "";
                if (results.size() == 1 && superMember.getDocComment() != null) {
                    commentString = superMember.getDocComment().getText();
                }

                // no parent comment. leave
                if (commentString.length() == 0) {
                    return;
                }

                PhpDocComment comment = PhpPsiElementFactory.createFromText(
                        phpNamedElement.getProject(),
                        PhpDocComment.class,
                        commentString
                );

                PhpCodeEditUtil.insertDocCommentBefore(phpNamedElement, comment);

            }
        }.execute();
    }

    @Override
    public void update(@NotNull AnActionEvent e)
    {
        PhpNamedElement phpNamedElement = this.getParentPsiFromEvent(e);

        // no named parent or not doc block
        if (phpNamedElement == null || phpNamedElement.getDocComment() == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

        // no inheritDoc
        if (! phpNamedElement.getDocComment().hasInheritDocTag()) {
            e.getPresentation().setEnabled(false);
            return;
        }

        e.getPresentation().setEnabled(true);
    }

    public PhpNamedElement getParentPsiFromEvent(@NotNull AnActionEvent e)
    {
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if (file == null || editor == null) {
            //e.getPresentation().setEnabled(false);
            return null;
        }
        PsiElement at = file.findElementAt(editor.getCaretModel().getOffset());

        return PsiTreeUtil.getParentOfType(at, PhpNamedElement.class);
    }
}
