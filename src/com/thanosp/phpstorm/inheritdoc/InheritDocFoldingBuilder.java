package com.thanosp.phpstorm.inheritdoc;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpClassHierarchyUtils;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InheritDocFoldingBuilder extends FoldingBuilderEx {
    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement psiElement, @NotNull Document document, boolean b) {
        if (!(psiElement instanceof PhpFile)) {
            return new FoldingDescriptor[0];
        }
        ArrayList descriptors = new ArrayList();
        Collection phpDocs = PsiTreeUtil.findChildrenOfType(psiElement, PhpDocCommentImpl.class);

        for (Object phpDoc1 : phpDocs) {
            PhpDocCommentImpl phpDoc = (PhpDocCommentImpl) phpDoc1;
            this.attachBlockShortcuts(descriptors, phpDoc);
        }

        return (FoldingDescriptor[])descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private void attachBlockShortcuts(List<FoldingDescriptor> descriptors, PhpDocCommentImpl phpDocComment) {
        final String comment = this.getPlaceholderTextForCommentImpl(phpDocComment);

        if (null == comment) {
            return;
        }

        int docPosition = phpDocComment.getTextRange().getStartOffset();
        int positionInDocBlock = phpDocComment.getText().toLowerCase().indexOf("{@inheritdoc}");
        int position = docPosition + positionInDocBlock;

        TextRange range = new TextRange(position, position + 13);

        descriptors.add(new FoldingDescriptor(phpDocComment.getNode(), range) {
            @Nullable
            public String getPlaceholderText() {
                return comment;
            }
        });
    }

    private String getPlaceholderTextForCommentImpl(PhpDocCommentImpl psiElement) {
        PsiElement phpNamedElement = psiElement.getOwner();

        if (! psiElement.hasInheritDocTag()) {
            return null;
        }
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

        if (results.isEmpty()) {
            return null;
        }

        for (Object result : results) {

            PhpNamedElement superMember = (PhpNamedElement) result;

            String commentString;
            if (superMember.isValid() && superMember.getDocComment() != null) {
                commentString = superMember.getDocComment().getText().replaceAll("\\s+", " ");
                commentString = commentString.replaceAll("/\\*+", "");
                commentString = commentString.replaceAll("\\*/", "");
                commentString = commentString.replaceAll("^\\s\\*?", "");
                commentString = commentString.replaceAll("\\s+\\*[\\s\\*]?\\s", " * ");
                commentString = commentString.trim();
                return commentString;
            }

        }

        // no valid supermember found
        return null;
    }


    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode astNode) {
        if (! (astNode.getPsi() instanceof PhpDocCommentImpl)) {
            return "...";
        }
        return this.getPlaceholderTextForCommentImpl((PhpDocCommentImpl) astNode.getPsi());
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return true;
    }
}
