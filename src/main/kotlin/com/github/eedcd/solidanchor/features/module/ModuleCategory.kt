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
package net.solidanchor.features.module

import net.solidanchor.config.OptionalInclusion
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.features.addon.AddonApi
import net.minecraft.resources.Identifier

/**
 * @param icon an SVG or PNG shown by the ClickGUI, `namespace:path` for `resources/<namespace>/<path>` in the
 * add-on's jar. Not Minecraft's `assets/`: those are visible to anything inspecting the loaded resource packs.
 * The built-in categories have their icons in the theme instead.
 */
@AddonApi
class ModuleCategory @JvmOverloads constructor(
    override val tag: String,
    val inclusionGroup: OptionalInclusion? = null,
    val icon: Identifier? = null,
) : Tagged {

    constructor(tag: String, icon: Identifier) : this(tag, null, icon)

    @Deprecated(
        message = "For script compatibility only. Use choiceName instead",
        replaceWith = ReplaceWith("choiceName"),
        level = DeprecationLevel.ERROR
    )
    val readableName: String
        get() = tag

}
