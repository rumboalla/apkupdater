### Parser Traits (mixins)

The Chevrotain Parser class is implemented as multiple classes mixed-in (combined) together
to provide the required functionality.

A mix-in approach has been chosen to:

1. Split up the large (~3,000 LOC) Parser Class into smaller files.
   - Similar to C# [Partial Classes](https://docs.microsoft.com/en-us/dotnet/csharp/programming-guide/classes-and-structs/partial-classes-and-methods)
2. Avoid performance regressions of the common "classic" Object oriented pattern of composition & delegation.
   - For example, consider: `this.LA()` vs `this.traitX.LA()`
   - Past attempts at extracting an API for Lexer Adaptors using composition have [shown](https://github.com/SAP/chevrotain/issues/528#issuecomment-313863665)
     significant performance regressions.

### Background

The mixin pattern used here is derived from the mixins pattern found in the [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/mixins.html).
There are a few Issues With The TypeScript Handbook Pattern:

- Duplicate declarations of the instance methods.
- Missing scenario of interaction between different mixins included by the same class.
  - This scenario is more similar to partial classes.
- Instance fields initialization would not be executed.
  - As instance fields are copied to the constructor by the TypeScript compiler
    and only one constructor would get invoked.

Therefor a slightly modified pattern has been used.

### The "Upgraded" Pattern

The building blocks are as follows:

- **Define the full combined type**
  - by using Intersection Types to define the complete Type (after all the mixins).
  - [Source Snippet](https://github.com/SAP/chevrotain/blob/8a1c3594165849c179f6c9fd67078ba96af0ea34/src/parse/parser/traits/parser_traits.ts#L20-L28)
- **Make every method aware of its full execution context**
  - By specifying the type of "this" context in methods as the "full combined type" Type
    to allow "interaction" between different mixed-ins of the same class.
  - [e.g calling a method from another trait](https://github.com/SAP/chevrotain/blob/8a1c3594165849c179f6c9fd67078ba96af0ea34/src/parse/parser/traits/recognizer_api.ts#L35-L41)
- **Define state initialization for each trait/mixin**?:
  - By defining init methods for each trait/mixin which would be called during the combined type initialization.
  - [A single trait's initializer definition](https://github.com/SAP/chevrotain/blob/8a1c3594165849c179f6c9fd67078ba96af0ea34/src/parse/parser/traits/lexer_adapter.ts#L17-L21)
- **Invoke the state initialization in the combined class**
  - By using a type assertion in the main class constructor to enable calling these init methods.
  - [Invoking the init methods](https://github.com/SAP/chevrotain/blob/8a1c3594165849c179f6c9fd67078ba96af0ea34/src/parse/parser/parser.ts#L225-L232)
- **Ensuring alignment with the public API**
  - By using a dummy assignment statement to leverge the TypeScript compiler to ensure the internal Parser implementation matches
    the exposed public API of Chevrotain.
  - [Dummy assignment](https://github.com/SAP/chevrotain/blob/8a1c3594165849c179f6c9fd67078ba96af0ea34/src/api.ts#L193-L197)
- **Enriching the combined class's prototype**
  - [Invoking ApplyMixings](https://github.com/SAP/chevrotain/blob/8a1c3594165849c179f6c9fd67078ba96af0ea34/src/parse/parser/parser.ts#L240-L249)
  - [Upgraded ApplyMixings with setter/getter handling](https://github.com/SAP/chevrotain/blob/8a1c3594165849c179f6c9fd67078ba96af0ea34/src/utils/utils.ts#L433-L460)

* Pros

  - Avoid duplication.
  - Allows splitting up large classes to multiple files if/when class composition is not appropriate.
    - a.k.a ["partial classes"](https://docs.microsoft.com/en-us/dotnet/csharp/programming-guide/classes-and-structs/partial-classes-and-methods).

* Cons
  - Breaks the semantics of TypeScript a bit, What the compiler knows about "SmartObject"
    is no longer the "full story".

### References

- [Wikipedia Article on Mixins](https://en.wikipedia.org/wiki/Mixin)
- [Trait Linearization in Scala](https://www.trivento.io/trait-linearization/)

### To Investigate

Would TypeScript 2.2 enabled a simpler & clearer pattern by using its support for ["mixin classes"](https://github.com/Microsoft/TypeScript/wiki/What%27s-new-in-TypeScript#support-for-mix-in-classes)?
