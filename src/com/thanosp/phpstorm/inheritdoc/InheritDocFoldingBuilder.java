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
import com.jetbrains.php.lang.psi.elements.PhpElementWithModifier;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

public class InheritDocFoldingBuilder extends FoldingBuilderEx {
    private static final int INHERITANCE_RECURSION_LIMIT = 10;

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement psiElement, @NotNull Document document, boolean b) {
        // only do this for php files
        if (!(psiElement instanceof PhpFile)) {
            return new FoldingDescriptor[0];
        }
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        Collection<PhpDocCommentImpl> filePhpDocs = PsiTreeUtil.findChildrenOfType(psiElement, PhpDocCommentImpl.class);

        filePhpDocs.forEach(phpDoc -> attachFolding(descriptors, phpDoc));

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private void attachFolding(List<FoldingDescriptor> descriptors, PhpDocCommentImpl phpDocComment) {
        final String comment = this.calculateInheritDocValue(phpDocComment);

        // don't attach if there was no valid doc value calculated
        if (null == comment) {
            return;
        }

        TextRange range = getInheritDocTextRange(phpDocComment);
        descriptors.add(new FoldingDescriptor(phpDocComment.getNode(), range) {
            @NotNull
            public String getPlaceholderText() {
                return comment;
            }
        });
    }

    private String calculateInheritDocValue(PhpDocCommentImpl phpDocComment) {
        return calculateInheritDocValue(phpDocComment, 0);
    }

    /**
     * Calculates the range to replace the inheritdoc in the given comment
     * @param phpDocComment The text range for accurate folding of the inheritdoc
     * @return TextRange
     */
    @NotNull
    private TextRange getInheritDocTextRange(PhpDocCommentImpl phpDocComment) {
        int docPosition = phpDocComment.getTextRange().getStartOffset();
        String normalizedDocBlock = phpDocComment.getText().toLowerCase();
        // inheritdoc might be wrapped in curly brackets
        int positionInDocBlock = normalizedDocBlock.contains("{@inheritdoc}") ?
                normalizedDocBlock.indexOf("{@inheritdoc}") : normalizedDocBlock.indexOf("@inheritdoc");
        int position = docPosition + positionInDocBlock;

        int inheritDocLength = normalizedDocBlock.contains("{@inheritdoc}") ? 13 : 11;
        return new TextRange(position, position + inheritDocLength);
    }

    /**
     * Calculates and returns the inheritdoc final value for the given element or returns null if there's nothing to do
     * @param psiElement The element to compute the placeholder for
     * @param level      The level of
     * @return String|null
     */
    private String calculateInheritDocValue(PhpDocCommentImpl psiElement, int level) {
        if (level > INHERITANCE_RECURSION_LIMIT) {
            return null;
        }
        PsiElement phpNamedElement = psiElement.getOwner();

        if (! psiElement.hasInheritDocTag()) {
            return null;
        }
        final ArrayList<PhpElementWithModifier> results = new ArrayList<>(1);

        if (phpNamedElement instanceof Method) {
            PhpClassHierarchyUtils.processSuperMembers(
                    (PhpClassMember) phpNamedElement,
                    (method, subClass, baseClass) -> {
                        results.add(method);
                        return true;
                    }
            );
        } else if(phpNamedElement instanceof PhpClass) {
            results.addAll(PhpClassHierarchyUtils.getImmediateParents((PhpClass)phpNamedElement));
        }

        if (results.isEmpty()) {
            return null;
        }

        String currentDocBlock = psiElement.getText();

        for (Object result : results) {
            PhpNamedElement superMember = (PhpNamedElement) result;

            if (superMember.isValid() && superMember.getDocComment() != null) {
                // calculate the parent doc value
                String parentComment = superMember.getDocComment().hasInheritDocTag() ?
                        this.calculateInheritDocValue((PhpDocCommentImpl) superMember.getDocComment(), level + 1) :
                        superMember.getDocComment().getText();
                if (parentComment == null) {
                    return null;
                }
                String newComment = currentDocBlock.replaceFirst(
                        "(?i)\\{?@inheritdoc\\}?",
                        Matcher.quoteReplacement(parentComment)
                );
                // remove starting doc tags
                newComment = newComment.replaceAll("/\\*+", "");
                // remove ending doc tags
                newComment = newComment.replaceAll("\\*/", "");
                // normalize whitespace/asterisks for readability
                newComment = newComment.replaceAll("[\\s\\*]{3,}", " * ");
                // remove leading and trailing whitespace and asterisks
                newComment = newComment.replaceAll("^[\\s\\*]+", "");
                newComment = newComment.replaceAll("[\\s\\*]+$", "");

                return newComment;
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
        return this.calculateInheritDocValue((PhpDocCommentImpl) astNode.getPsi());
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return true;
    }
}
