/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package suive.delta.service

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.DECLARATION
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.DELEGATION
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.FAKE_OVERRIDE
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.SYNTHESIZED
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithVisibility
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.idea.resolve.ResolutionFacade
import org.jetbrains.kotlin.idea.util.getResolutionScope
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.OverridingUtil
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.utils.getImplicitReceiversHierarchy
import java.util.LinkedHashSet

fun DeclarationDescriptorWithVisibility.isVisible(from: DeclarationDescriptor): Boolean {
    return isVisible(from, null)
}

fun DeclarationDescriptorWithVisibility.isVisible(
    context: PsiElement,
    receiverExpression: KtExpression?,
    bindingContext: BindingContext,
    resolutionFacade: ResolutionFacade
): Boolean {
    val resolutionScope = context.getResolutionScope(bindingContext, resolutionFacade)
    val from = resolutionScope.ownerDescriptor
    return isVisible(from, receiverExpression, bindingContext, resolutionScope)
}

private fun DeclarationDescriptorWithVisibility.isVisible(
    from: DeclarationDescriptor,
    receiverExpression: KtExpression?,
    bindingContext: BindingContext? = null,
    resolutionScope: LexicalScope? = null
): Boolean {
    if (DescriptorVisibilities.isVisibleWithAnyReceiver(this, from)) return true

    if (bindingContext == null || resolutionScope == null) return false

    // for extension it makes no sense to check explicit receiver because we need dispatch receiver which is implicit in this case
    if (receiverExpression != null && !isExtension) {
        val receiverType = bindingContext.getType(receiverExpression) ?: return false
        val explicitReceiver = ExpressionReceiver.create(receiverExpression, receiverType, bindingContext)
        return DescriptorVisibilities.isVisible(explicitReceiver, this, from)
    } else {
        return resolutionScope.getImplicitReceiversHierarchy().any {
            DescriptorVisibilities.isVisible(it.value, this, from)
        }
    }
}

fun DescriptorVisibility.toKeywordToken(): KtModifierKeywordToken = when (val normalized = normalize()) {
    DescriptorVisibilities.PUBLIC -> KtTokens.PUBLIC_KEYWORD
    DescriptorVisibilities.PROTECTED -> KtTokens.PROTECTED_KEYWORD
    DescriptorVisibilities.INTERNAL -> KtTokens.INTERNAL_KEYWORD
    else -> {
        if (DescriptorVisibilities.isPrivate(normalized)) {
            KtTokens.PRIVATE_KEYWORD
        } else {
            error("Unexpected visibility '$normalized'")
        }
    }
}

fun <D : CallableMemberDescriptor> D.getDirectlyOverriddenDeclarations(): Collection<D> {
    val result = LinkedHashSet<D>()
    for (overriddenDescriptor in overriddenDescriptors) {
        @Suppress("UNCHECKED_CAST")
        when (overriddenDescriptor.kind) {
            DECLARATION -> result.add(overriddenDescriptor as D)
            FAKE_OVERRIDE, DELEGATION -> result.addAll((overriddenDescriptor as D).getDirectlyOverriddenDeclarations())
            SYNTHESIZED -> {
                //do nothing
            }
            else -> throw AssertionError("Unexpected callable kind ${overriddenDescriptor.kind}: $overriddenDescriptor")
        }
    }
    return OverridingUtil.filterOutOverridden(result)
}

fun <D : CallableMemberDescriptor> D.getDeepestSuperDeclarations(withThis: Boolean = true): Collection<D> {
    val overriddenDeclarations = DescriptorUtils.getAllOverriddenDeclarations(this)
    if (overriddenDeclarations.isEmpty() && withThis) {
        return setOf(this)
    }

    return overriddenDeclarations.filterNot(DescriptorUtils::isOverride)
}

fun <T : DeclarationDescriptor> T.unwrapIfFakeOverride(): T {
    return if (this is CallableMemberDescriptor) DescriptorUtils.unwrapFakeOverride(this) else this
}
