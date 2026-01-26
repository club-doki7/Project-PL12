# Universe Design Choices in Dependent Type Theory

This document elaborates on different universe design choices in dependent type theory, discussing their advantages and shortcomings. This is relevant to the ongoing development of PL-12, which currently uses the McBride universe (`* : *`).

## Table of Contents

1. [Type-in-Type (`* : *`)](#type-in-type)
2. [Cumulative Universe Hierarchies](#cumulative-universe-hierarchies)
3. [Impredicativity](#impredicativity)
4. [Universe Polymorphism](#universe-polymorphism)
5. [Eliminators vs Dependent Pattern Matching](#eliminators-vs-dependent-pattern-matching)

---

## Type-in-Type

Type-in-type (also known as the McBride universe or `* : *`) is the simplest approach where there is a single universe `*` (or `Type`) that is its own type.

### Advantages

- **Simplicity**: No need to manage universe levels; the type system is straightforward to implement and understand.
- **Expressiveness**: Allows writing highly generic code without universe level annotations cluttering the syntax.
- **Practical programming**: For many practical programming tasks that don't require logical consistency, type-in-type works perfectly fine.
- **No universe level juggling**: Users don't need to worry about lifting types between universe levels.

### Shortcomings

- **Logical inconsistency**: Type-in-type leads to Girard's paradox, meaning every type is inhabited and the system cannot be used as a consistent logic.
- **No termination guarantee**: Without consistency, you lose strong normalization guarantees.
- **Cannot serve as a proof assistant**: If you intend to use the type system for theorem proving, inconsistency is a fatal flaw.
- **Potential for non-terminating type checking**: In pathological cases, type checking may not terminate.

### When to Use

Type-in-type is suitable when:
- The primary goal is practical programming rather than theorem proving
- Simplicity is valued over logical soundness
- The system is not intended as a foundation for mathematics

---

## Cumulative Universe Hierarchies

Cumulative universe hierarchies introduce a countable sequence of universes `Type₀ : Type₁ : Type₂ : ...` (or `Type(0) : Type(1) : Type(2) : ...`) where `Type(i) : Type(i+1)` and `Type(i)` is a subtype of `Type(i+1)` (cumulativity).

### Advantages

- **Logical consistency**: Avoids Girard's paradox by stratifying types into levels.
- **Strong normalization**: Well-founded universe hierarchy ensures termination.
- **Suitable for theorem proving**: Can be used as a foundation for constructive mathematics.
- **Cumulativity eases use**: Automatic subtyping between universe levels reduces annotation burden compared to non-cumulative systems.

### Shortcomings

- **Increased complexity**: Implementation becomes more complex with universe constraint solving.
- **Universe level annotations**: Users sometimes need to manage explicit universe levels.
- **Universe polymorphism often needed**: Without universe polymorphism, code duplication across universe levels becomes painful.
- **Subtyping complications**: Cumulativity introduces a form of subtyping, which complicates the metatheory and implementation.

### Variants

- **Non-cumulative hierarchies**: Used in systems like Agda (by default). Simpler metatheory but requires explicit lifting.
- **Cumulative hierarchies**: Used in Coq and Lean. More convenient but more complex.
- **Explicit vs implicit levels**: Some systems infer universe levels (Agda, Lean), others require more explicit annotation.

---

## Impredicativity

Impredicativity allows a type to be defined by quantifying over a collection that includes itself. The classic example is `Prop : Type` where `∀ (P : Prop), P` (or `forall (P : Prop), P`) is itself in `Prop`.

### Advantages

- **Compact encodings**: Enables Church-style encodings of data types without universe level explosion.
- **Second-order logic embedding**: Impredicative `Prop` naturally corresponds to second-order propositional logic.
- **Proof irrelevance**: Often combined with proof irrelevance for propositions, which can simplify reasoning.
- **Polymorphism without universe polymorphism**: Impredicative polymorphism can substitute for some uses of universe polymorphism.

### Shortcomings

- **Incompatibility with certain axioms**: Impredicativity is incompatible with some useful axioms:
  - Strong elimination from `Prop` into `Type` is inconsistent with certain classical principles
  - Combining impredicativity with large elimination and certain forms of proof-relevant elimination can lead to inconsistency
- **Complexity in implementation**: Unification and type inference become more difficult.
- **Restrictions on elimination**: Must carefully restrict what can be eliminated from impredicative propositions to avoid paradoxes (e.g., Coq's `Prop` restrictions).
- **Can break parametricity**: Impredicativity can interfere with free theorems and parametricity.

### Stratified Impredicativity

Some systems (like Coq) have:
- An impredicative `Prop` (for propositions)
- A predicative `Set`/`Type` hierarchy (for data)

This provides benefits of impredicativity for logical content while keeping data types predicative.

---

## Universe Polymorphism

Universe polymorphism allows definitions to be polymorphic over universe levels, abstracting over which universe level types live in.

### Advantages

- **Code reuse**: Write a definition once, use it at any universe level.
- **Avoids code duplication**: Without universe polymorphism, you might need separate versions of the same function for different universe levels.
- **Maintains consistency**: Unlike type-in-type, preserves logical consistency while providing flexibility.
- **Natural for library code**: Generic libraries benefit greatly from universe polymorphism.

### Shortcomings

- **Complexity**: Adds another layer of abstraction that users and implementers must understand.
- **Universe constraint solving**: Requires sophisticated constraint solving to infer universe level arguments.
- **Verbosity when explicit**: If levels must be written explicitly, code becomes cluttered.
- **Interaction with other features**: Universe polymorphism's interaction with features like cumulativity, impredicativity, and module systems can be subtle.

### Implementation Approaches

- **Implicit universe polymorphism** (Agda): Universe levels are inferred and generally invisible.
- **Explicit universe polymorphism** (Lean 4): Universe parameters are explicit at declaration sites but often inferred at use sites.
- **Template universe polymorphism** (Coq): Definitions are "template" and instantiated as needed.

---

## Eliminators vs Dependent Pattern Matching

This section discusses the choice between primitive eliminators and dependent pattern matching for working with inductive types.

### Eliminators

Eliminators (also called recursors or induction principles) are the "official" way to destruct inductive types in type theory.

#### Advantages

- **Foundational simplicity**: Eliminators have well-understood metatheory.
- **Guaranteed termination**: Structural recursion through eliminators ensures termination.
- **Uniformity**: Every inductive type gets a systematic eliminator.

#### Shortcomings

- **Verbose**: Using eliminators directly is extremely tedious for complex pattern matches.
- **Unreadable code**: Nested eliminator calls are hard to read and write.
- **Cognitive overhead**: Thinking in terms of eliminators is unnatural for most programmers.

### Dependent Pattern Matching

Dependent pattern matching allows writing function definitions by cases, similar to functional programming languages but with dependent types.

#### Advantages

- **Readability**: Pattern matching is intuitive and resembles standard functional programming.
- **Productivity**: Writing code with pattern matching is much faster.
- **Inaccessible patterns**: Can express that certain patterns are impossible (absurd patterns).
- **With-abstraction**: Systems like Agda provide `with` to refine patterns based on intermediate computations.

#### Shortcomings

- **Coverage checking**: Ensuring all cases are covered becomes complex with dependent types.
- **Termination checking**: Must verify that recursive calls are on structurally smaller arguments.
- **Elaboration complexity**: Translating pattern matching to eliminators (in systems that do this) is non-trivial.
- **Subject reduction issues**: Some forms of dependent pattern matching can break subject reduction (type preservation).

### Implementation Strategies

1. **Elaborate to eliminators**: Pattern matching is surface syntax that elaborates to eliminator calls (e.g., older Coq).
2. **First-class pattern matching**: Pattern matching is primitive, with its own typing rules (e.g., Agda).
3. **Equations-based**: Provide an intermediate language like Equations (Coq plugin) that gives better pattern matching on top of eliminators.

### Recommendations

For practical languages, dependent pattern matching is almost essential for usability. However:
- Consider providing both eliminators (for foundational work) and pattern matching (for practical use)
- Ensure the termination checker is robust
- If elaborating to eliminators, ensure the elaboration is reliable and produces efficient code

---

## Summary and Recommendations for PL-12

Given that PL-12 is described as a practical extension over LambdaPi, here are some considerations:

| Feature | Recommendation | Rationale |
|---------|----------------|-----------|
| Type-in-type | Consider moving away | Limits use as a proof assistant |
| Universe hierarchy | Worth considering | Provides consistency if needed |
| Universe polymorphism | Highly recommended | Essential for practical library code |
| Impredicative Prop | Optional | Useful for logical content, but adds complexity |
| Pattern matching | Essential | Eliminators alone are impractical |

The choice ultimately depends on the goals of PL-12:
- If purely for practical programming: Type-in-type is acceptable
- If theorem proving is a goal: Universe hierarchy is necessary
- Regardless: Dependent pattern matching greatly improves usability over raw eliminators

---

## References

- Girard, J.-Y. (1972). *Interprétation fonctionnelle et élimination des coupures de l'arithmétique d'ordre supérieur*. (Girard's paradox)
- Martin-Löf, P. (1984). *Intuitionistic Type Theory*. (Universe hierarchies)
- Coquand, T. (1986). *An Analysis of Girard's Paradox*. (Type-in-type inconsistency)
- Sozeau, M., & Tabareau, N. (2014). *Universe Polymorphism in Coq*. (Universe polymorphism implementation)
- Cockx, J., & Abel, A. (2018). *Elaborating dependent (co)pattern matching*. (Pattern matching elaboration)
- Coquand, T. (1992). *Pattern Matching with Dependent Types*. (Dependent pattern matching)
