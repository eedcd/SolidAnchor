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

package net.solidanchor.config.types

import net.solidanchor.config.ConfigSystem
import net.solidanchor.config.ConfigSystem.configs
import net.solidanchor.config.ConfigSystem.rootFolder
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.features.addon.AddonApi
import java.io.File

@AddonApi
open class Config(name: String, value: MutableCollection<Value<*>> = mutableListOf()) : ValueGroup(name, value) {

    /** Writes the file now; the client does so on exit anyway. */
    fun saveToDisk() = ConfigSystem.store(this)

    /** Re-reads the file, dropping unsaved changes. */
    fun loadFromDisk() = ConfigSystem.load(this)

    val jsonFile: File
        get() {
            require(this in configs) { "${this.name} is not registered" }
            return File(rootFolder, "${this.loweredName}.json")
        }

    /**
     * Serialization finishes here before [jsonFile] is replaced. The replacement is atomic
     * when supported by the file system; otherwise it falls back to a replacing move.
     */
    val jsonTmpFile: File
        get() {
            require(this in configs) { "${this.name} is not registered" }
            return File(rootFolder, "${this.loweredName}.json.tmp")
        }

}
