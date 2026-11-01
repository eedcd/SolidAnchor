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
package net.solidanchor.features.creativetab.tabs

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.solidanchor.api.core.HttpClient
import net.solidanchor.api.core.HttpMethod
import net.solidanchor.api.core.bodyAsJson
import net.solidanchor.api.core.ioScope
import net.solidanchor.api.core.retrying
import net.solidanchor.features.creativetab.CustomCreativeModeTab
import net.solidanchor.utils.text.asPlainText
import net.solidanchor.utils.client.logger
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Style
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

data class Head(val name: String, val uuid: UUID, val value: String) {

    fun asItemStack(): ItemStack {
        val builder = DataComponentPatch.builder()

        builder.set(DataComponents.CUSTOM_NAME, name.asPlainText(CUSTOM_NAME_STYLE))
        builder.set(
            DataComponents.LORE,
            ItemLore(
                listOf("UUID: $uuid".asPlainText(UUID_STYLE))
            )
        )

        val profile = GameProfile(
            uuid,
            name,
            PropertyMap(
                ImmutableMultimap.of("textures", Property("textures", value))
            ),
        )

        builder.set(
            DataComponents.PROFILE,
            ResolvableProfile.createResolved(profile),
        )

        return ItemStack(BuiltInRegistries.ITEM.wrapAsHolder(Items.PLAYER_HEAD), 1, builder.build())
    }


    companion object {
        @JvmStatic
        private val CUSTOM_NAME_STYLE = Style.EMPTY.applyFormats(ChatFormatting.GOLD, ChatFormatting.BOLD)

        @JvmStatic
        private val UUID_STYLE = Style.EMPTY.applyFormat(ChatFormatting.GRAY)
    }
}

class HeadsCreativeModeTab : CustomCreativeModeTab(
    "Heads",
    icon = { Items.SKELETON_SKULL.defaultInstance },
    items = { items ->
        heads.getNow()?.let { heads ->
            items.acceptAll(heads.distinctBy { it.name }.map(Head::asItemStack))
        }
    }
) {
    companion object {
        /**
         * The API endpoint to fetch heads from which is owned by CCBlueX
         * and therefore can reliably depend on.
         */
        const val HEAD_DB_API = "https://headdb.org/api/category/all"

        val heads = ioScope.retrying(
            interval = 1.seconds,
            name = "player-heads",
            maxRetries = 3,
        ) {
            val heads: Map<String, Head> = HttpClient.request(HEAD_DB_API, HttpMethod.GET).bodyAsJson()

            heads.values.also {
                logger.info("Successfully loaded ${it.size} heads from HeadDB")
            }
        }
    }
}
