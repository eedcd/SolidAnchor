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
package net.solidanchor.interfaces;

import net.solidanchor.features.module.modules.render.ModuleXRay;
import org.jspecify.annotations.Nullable;

/**
 * Per-block cache of {@link ModuleXRay#renderActive()} on Sodium's block render context.
 *
 * <p>Sodium reuses one context per builder thread and keeps the state of the block being rendered on it, so the
 * module state is resolved once per block and every face decision of that block reads a plain field instead of
 * walking the thread's scoped bindings. Reads outside such a block — and any path that never resolves the cache —
 * fall back to {@link ModuleXRay#renderActive()}.
 */
public interface AbstractBlockRenderContextAddition {

    boolean solidAnchor$xrayActive();

    /**
     * Cached value for the block being rendered, or {@code null} to leave the block scope.
     */
    void solidAnchor$setXRayActive(@Nullable Boolean active);

}
