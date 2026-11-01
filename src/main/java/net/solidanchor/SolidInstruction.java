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
package net.solidanchor;

import net.solidanchor.utils.client.GitInfo;
import org.jspecify.annotations.NullMarked;

import javax.swing.*;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class is for manual open of the jar file.
 * It should not use any other external libraries such as Kotlin stdlib, JavaFX or Minecraft
 * because they are not included in the jar.
 */
@NullMarked
public final class SolidInstruction {

  private SolidInstruction() {}

  private static Stream<ImageIcon> loadIcons() {
    return Stream.of(
            "/resources/solidanchor/icon_64x64.png",
            "/resources/solidanchor/icon_32x32.png",
            "/resources/solidanchor/icon_16x16.png"
        ).map(SolidInstruction.class::getResource)
        .filter(Objects::nonNull)
        .map(ImageIcon::new);
  }

  public static void main(String[] args) {
    var buttons = new String[]{"Close"};

    JOptionPane.showOptionDialog(
        null,
        """
            Welcome to %s!

            This file is a Fabric mod, you should use it with a launcher and Fabric.
            """.formatted(SolidAnchor.CLIENT_NAME),
        SolidAnchor.CLIENT_NAME + " by " + SolidAnchor.CLIENT_AUTHOR + " (" + GitInfo.version() + ")",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        loadIcons().findFirst().orElse(null),
        buttons,
        buttons[0]
    );

    System.exit(0);
  }

}
