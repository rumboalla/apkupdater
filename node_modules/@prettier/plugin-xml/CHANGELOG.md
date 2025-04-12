# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.4.1] - 2024-03-30

### Changed

- Fix the npm publish.

## [3.4.0] - 2024-03-29

### Added

- Export plugin type.

### Changed

- Fix printing around reference nodes.
- Trim only XML whitespace, not JS whitespace.

## [3.3.1] - 2024-02-10

### Changed

- Allow elements to be marked as whitespace ignored even when they have reference nodes.

## [3.3.0] - 2024-02-09

### Added

- Support for `formatWithCursor`.

### Changed

- Always keep whitespace around in `xsl:text` tags.

## [3.2.2] - 2023-10-27

### Changed

- Fixed a bug with the `xmlSortAttributesByKey` option when `xmlns` attributes are present.

## [3.2.1] - 2023-09-11

### Added

- Updated the documentation to reflect that you need to pass the plugin path now.

### Changed

- Error messages thrown by the plugin are now much closer to the error messages thrown by prettier.
- Fixed a bug where self-closing tags that looked like embeds would throw an error.

## [3.2.0] - 2023-08-08

### Added

- Respect `xml:space="preserve"` as an override to the `xmlWhitespaceSensitivity` option.

## [3.1.1] - 2023-07-14

### Changed

- Required `prettier` as a peer dependency instead of a runtime dependency.

## [3.1.0] - 2023-07-07

### Added

- the `xmlSortAttributesByKey: true | false` option has been added. See the README.
- The `xmlQuoteAttributes: "preserve" | "single" | "double"` option has been added. See the README.

## [3.0.0] - 2023-07-06

### Changed

- See alpha release notes.

## [3.0.0-alpha.0] - 2023-06-02

### Added

- The `xmlWhitespaceSensitivity: "preserve"` option has been added. See the README.

### Changed

- Fixed the behavior of `bracketSameLine` when the attributes on the parent element broke into multiple lines.
- **BREAKING** Require prettier v3.
- **BREAKING** Migrate to ESM modules.

## [2.2.0] - 2022-05-12

### Added

- Better error messages in the case of a syntax error.

## [2.1.0] - 2022-04-16

### Added

- Support for the `singleAttributePerLine` option.

## [2.0.1] - 2022-03-22

### Added

- Better idempotency when printing long strings of text content within elements with ignored whitespace.

## [2.0.0] - 2022-03-22

### Changed

- Require prettier `2.4.0` for the `bracketSameLine` option.

## [1.2.0] - 2021-12-23

### Added

- Support formatting `.xsl` files.

## [1.1.0] - 2021-09-26

### Added

- Bring back the `xmlSelfClosingSpace` option.

## [1.0.2] - 2021-07-17

### Changed

- Removed duplicated inner comments when `xmlWhitespaceSensitivity` is set to `"strict"`.

## [1.0.1] - 2021-07-14

### Changed

- Fix the export to work in non-TypeScript environments.

## [1.0.0] - 2021-07-14

### Added

- Support for the `bracketSameLine` option to mirror the core option.

### Removed

- The `xmlSelfClosingSpace` option is now removed to make it easier to maintain.

## [0.13.1] - 2021-03-03

### Changed

- Fixed a bug with newlines when there is empty content.

## [0.13.0] - 2021-01-22

### Added

- Maintain newlines if there are some in the original source.

## [0.12.0] - 2020-08-31

### Added

- Allow embedded parsers to handle content if element tags contain only text content and the tag name matches the name of an existing parser. For example:

```xml
<style type="text/css">
.box {
  height: 100px;
  width: 100px;
}
</style>
```

- Additionally support `.inx` files.

## [0.11.0] - 2020-08-14

### Changed

- Support for a whole wide variety of file types, as per linguist.

## [0.10.0] - 2020-07-24

### Changed

- Some better support for indenting mixed content when whitespace is set to ignore.

## [0.9.0] - 2020-07-21

### Added

- Ignored print ranges using the special `<!-- prettier-ignore-start -->` and `<!-- prettier-ignore-end -->` comments. For example, you can now do:

```xml
<foo>
  <!-- prettier-ignore-start -->
    < this-content-will-not-be-formatted />
  <!-- prettier-ignore-end -->
</foo>
```

and it will maintain your formatting.

## [0.8.0] - 2020-07-03

### Added

- Support `.wsdl` files.

## [0.7.2] - 2020-02-12

### Changed

- Bump dependency on `@xml-tools/parser` to `v1.0.2`.

## [0.7.1] - 2020-02-10

### Changed

- Require `prettier/doc` instead of `prettier` to load less code in standalone mode.

## [0.7.0] - 2020-01-29

### Added

- Handle processing instructions inside elements.
- Properly handle mult-line CData tags.

## [0.6.0] - 2020-01-27

### Added

- The `xmlWhitespaceSensitivity` option, with current valid values of `"strict"` and `"ignore"`. `"strict"` behavior maintains the current behavior, while `"ignore"` allows the plugin more freedom in where to place nodes.

## [0.5.0] - 2020-01-21

### Added

- Support for DOCTYPE nodes.

## [0.4.0] - 2020-01-19

### Added

- A dependency on the `@xml-tools/parser` package to handle parsing.
- We now register as supporting `.svg` and `.xsd` files.
- The `xmlSelfClosingSpace` option for specifying whether or not to add spaces before self-closing element tags.

## [0.3.0] - 2019-11-14

### Added

- Support for cdata tags.
- Support for the `locStart` and `locEnd` functions by tracking node metadata in the new parser.
- Support for comment nodes.
- Support for `<?xml ... ?>` and `<?xml-model ... ?>` tags.

### Changed

- Dropped the dependency on `fast-xml-parser` in favor of writing our own for better control over comments and node location.

## [0.2.0] - 2019-11-12

### Changed

- Renamed package to `@prettier/plugin-xml`.

## [0.1.0] - 2019-11-12

### Added

- Initial release 🎉

[unreleased]: https://github.com/prettier/plugin-xml/compare/v3.3.1...HEAD
[3.3.1]: https://github.com/prettier/plugin-xml/compare/v3.3.0...v3.3.1
[3.3.0]: https://github.com/prettier/plugin-xml/compare/v3.2.2...v3.3.0
[3.2.2]: https://github.com/prettier/plugin-xml/compare/v3.2.1...v3.2.2
[3.2.1]: https://github.com/prettier/plugin-xml/compare/v3.2.0...v3.2.1
[3.2.0]: https://github.com/prettier/plugin-xml/compare/v3.1.1...v3.2.0
[3.1.1]: https://github.com/prettier/plugin-xml/compare/v3.1.0...v3.1.1
[3.1.0]: https://github.com/prettier/plugin-xml/compare/v3.0.0...v3.1.0
[3.0.0]: https://github.com/prettier/plugin-xml/compare/v3.0.0-alpha.0...v3.0.0
[3.0.0-alpha.0]: https://github.com/prettier/plugin-xml/compare/v2.2.0...v3.0.0-alpha.0
[2.2.0]: https://github.com/prettier/plugin-xml/compare/v2.1.0...v2.2.0
[2.1.0]: https://github.com/prettier/plugin-xml/compare/v2.0.1...v2.1.0
[2.0.1]: https://github.com/prettier/plugin-xml/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/prettier/plugin-xml/compare/v1.2.0...v2.0.0
[1.2.0]: https://github.com/prettier/plugin-xml/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/prettier/plugin-xml/compare/v1.0.2...v1.1.0
[1.0.2]: https://github.com/prettier/plugin-xml/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/prettier/plugin-xml/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/prettier/plugin-xml/compare/v0.13.1...v1.0.0
[0.13.1]: https://github.com/prettier/plugin-xml/compare/v0.13.0...v0.13.1
[0.13.0]: https://github.com/prettier/plugin-xml/compare/v0.12.0...v0.13.0
[0.12.0]: https://github.com/prettier/plugin-xml/compare/v0.11.0...v0.12.0
[0.11.0]: https://github.com/prettier/plugin-xml/compare/v0.10.0...v0.11.0
[0.10.0]: https://github.com/prettier/plugin-xml/compare/v0.9.0...v0.10.0
[0.9.0]: https://github.com/prettier/plugin-xml/compare/v0.8.0...v0.9.0
[0.8.0]: https://github.com/prettier/plugin-xml/compare/v0.7.2...v0.8.0
[0.7.2]: https://github.com/prettier/plugin-xml/compare/v0.7.1...v0.7.2
[0.7.1]: https://github.com/prettier/plugin-xml/compare/v0.7.0...v0.7.1
[0.7.0]: https://github.com/prettier/plugin-xml/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/prettier/plugin-xml/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/prettier/plugin-xml/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/prettier/plugin-xml/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/prettier/plugin-xml/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/prettier/plugin-xml/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/prettier/plugin-xml/compare/289f2a...v0.1.0
