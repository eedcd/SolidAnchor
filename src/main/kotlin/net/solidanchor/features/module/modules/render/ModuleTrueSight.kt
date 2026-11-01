/*
 * This file is part of SolidAnchor
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * SolidAnchor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SolidAnchor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SolidAnchor. If not, see <https://www.gnu.org/licenses/>.
 */
package net.solidanchor.features.module.modules.render

import net.solidanchor.config.types.list.Tagged
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.modules.render.esp.ModuleESP
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.utils.render.entity
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.LivingEntity

/**
 * TrueSight module
 *
 * Allows you to see invisible objects and entities.
 */
object ModuleTrueSight : ClientModule("TrueSight", ModuleCategories.RENDER) {
    private val sight by multiEnumChoice("Sight", Sight.entries)

    val barriers get() = Sight.BARRIERS in sight
    val lights get() = Sight.LIGHTS in sight
    val entities get() = Sight.ENTITIES in sight

    val entityColor by color("EntityColor", Color4b(255, 255, 255, 100))
    val entityFeatureLayerColor by color("EntityFeatureLayerColor", Color4b(255, 255, 255, 120))

    @JvmStatic
    @Suppress("ComplexCondition")
    fun canRenderEntities(state: LivingEntityRenderState): Boolean {
        val enabled = this.running && entities

        val entity = state.entity as? LivingEntity ?: return false

        return (enabled || ModuleESP.running && ModuleESP.requiresTrueSight(entity))
            && entity.isInvisible
    }

    private enum class Sight(
        override val tag: String
    ) : Tagged {
        BARRIERS("Barriers"),
        LIGHTS("Lights"),
        ENTITIES("Entities")
    }
}
