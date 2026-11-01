<div align="center">
<p>
    <img width="200" src="src/main/resources/resources/solidanchor/icon_64x64.png">
</p>
</div>

SolidAnchor is a free and open-source mixin-based injection hacked client using the Fabric API for Minecraft.

## License

This project is subject to the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html). This
does only apply for source code located directly in this clean repository. During the development and compilation
process, additional source code may be used to which we have obtained no rights. Such code is not covered by the GPL
license.

For those who are unfamiliar with the license, here is a summary of its main points. This is by no means legal advice
nor legally binding.

*Actions that you are allowed to do:*

- Use
- Share
- Modify

*If you do decide to use ANY code from the source:*

- **You must disclose the source code of your modified work and the source code you took from this project. This means
  you are not allowed to use code from this project (even partially) in a closed-source (or even obfuscated)
  application.**
- **Your modified application must also be licensed under the GPL**

## Setting up a Workspace

SolidAnchor uses Gradle; to make sure that it is installed properly, you can
check [Gradle's website](https://gradle.org/install/).

1. Clone the repository.
2. CD into the local repository. (`cd SolidAnchor`)
3. Run `./gradlew genSources` for better development experience (Optional).
4. Open the folder as a Gradle project in your preferred IDE.
5. Run the client. (`./gradlew runClient`)

## Additional libraries

### Mixins

Mixins can be used to modify classes at runtime before they are loaded. SolidAnchor uses it to inject its code into the
Minecraft client. This way, none of Mojang's copyrighted code is shipped. If you want to learn more about it, check out
its [Documentation](https://docs.spongepowered.org/5.1.0/en/plugin/internals/mixins.html).

## Contributing

We appreciate contributions. So if you want to support us, feel free to make changes to SolidAnchor's source code.
