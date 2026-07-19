# HASAB Language Reference

HASAB is a statically-typed, compiled programming language with dual-script support (Latin + Amharic/Ethiopic). It compiles to Java source via a full pipeline: Lexing → Parsing → Semantic Analysis → Type Checking → Code Generation → JVM Bytecode.

---

## 1. Hello World

```hasab
fn main() {
    println("Hello, World!");
}
```

---

## 2. Types

### Primitive Types

| Type | Description | Java Mapping |
|------|-------------|--------------|
| `int` | 64-bit signed integer | `int` |
| `float` | 64-bit floating point | `double` |
| `string` | UTF-8 string | `String` |
| `bool` | Boolean (`true`/`false`) | `boolean` |
| `char` | Single character | `char` |
| `void` | No return value | `void` |

### Compound Types

| Syntax | Description | Java Mapping |
|--------|-------------|--------------|
| `[T]` | Array of T | `T[]` |
| `*T` | Pointer to T | `Object` |
| `T?` | Optional T | `Object` |
| `(T, U) -> R` | Function type | `Object` |

### User-Defined Types

See [Structs](#structs), [Enums](#enums), [Traits](#traits), [Type Aliases](#type-aliases).

---

## 3. Variables

```hasab
let x = 42;              // immutable binding
let mut y = 10;          // mutable binding
y += 5;                  // compound assignment
let name: string = "HASAB"; // explicit type annotation
```

Variables are immutable by default. Use `mut` to allow reassignment.

---

## 4. Operators

### Arithmetic
`+` `-` `*` `/` `%`

### Comparison
`==` `!=` `<` `>` `<=` `>=`

### Logical
`&&` `||` `!`

### Bitwise
`&` `|` `^` `~` `<<` `>>`

### Assignment
`=` `+=` `-=` `*=` `/=` `%=` `&=` `|=` `^=` `<<=` `>>=`

### Range
`..` (exclusive) `..=` (inclusive)

### Other
`.` (member access) `?.` (safe navigation) `!!` (null assertion) `[]` (index)

---

## 5. Control Flow

### If/Else
```hasab
if age >= 18 {
    println("adult");
} else {
    println("minor");
}
```

### While Loop
```hasab
let mut i = 0;
while i < 10 {
    println(i);
    i += 1;
}
```

### For Loop (iterate over arrays)
```hasab
let items = ["a", "b", "c"];
for (item: items) {
    println(item);
}
```

### Break and Continue
```hasab
while true {
    if done { break; }
    if skip { continue; }
}
```

### Return
```hasab
fn add(a: int, b: int) -> int {
    return a + b;
}
```

---

## 6. Functions

```hasab
fn greet(name: string) -> string {
    return "Hello, " + name + "!";
}

fn factorial(n: int) -> int {
    if n <= 1 { return 1; }
    return n * factorial(n - 1);
}

fn main() {
    println(greet("HASAB"));
    println(factorial(5));  // 120
}
```

Parameters are pass-by-value. Recursive functions are fully supported.

---

## 7. Structs

```hasab
struct Point {
    x: float,
    y: float,
}

struct Config {
    host: string,
    port: int,
    verbose: bool,
}

fn main() {
    let p = Point(3.0, 4.0);
    println(p.x);  // 3.0
    println(p.y);  // 4.0

    let mut c = Counter(0);
    c.value += 1;
}
```

Structs generate:
- A Java `static class` with public fields
- A no-arg constructor and a parameterized constructor
- Immutable fields are declared `final`

---

## 8. Enums

### Simple Enums
```hasab
enum Direction {
    North,
    South,
    East,
    West,
}

fn main() {
    let dir = Direction.North;
    println(dir);
}
```

### Data-Carrying Enums
```hasab
enum Result {
    Ok(int),
    Err(string),
}

fn main() {
    let success = Result.Ok(42);
    let failure = Result.Err("something went wrong");
}
```

Data-carrying enums generate Java classes with static factory methods.

---

## 9. Impl Blocks

```hasab
struct Point {
    x: float,
    y: float,
}

impl Point {
    fn new(px: float, py: float) -> Point {
        return Point(px, py);
    }

    fn distance(self, other: Point) -> float {
        let dx = self.x - other.x;
        let dy = self.y - other.y;
        return sqrt(dx * dx + dy * dy);
    }

    fn length(self) -> float {
        return sqrt(self.x * self.x + self.y * self.y);
    }
}

fn main() {
    let p1 = Point(3.0, 4.0);
    println(p1.length());       // 5.0
    let p2 = Point(0.0, 0.0);
    println(p1.distance(p2));   // 5.0
}
```

Impl methods are compiled as static methods with the struct type as the first parameter.

---

## 10. Traits

```hasab
trait Drawable {
    fn draw(self);
}

struct Circle {
    radius: float,
}
```

Traits define method signatures that structs and enums can implement.

---

## 11. Arrays

```hasab
// Array literal
let numbers = [10, 20, 30, 40, 50];

// Initialize array of size N
let arr = .[5];

// Index access (0-based)
println(numbers[0]);  // 10

// Modify mutable array
let mut values = [1, 2, 3];
values[0] = 100;

// Nested arrays (2D)
let matrix = [[1, 2], [3, 4]];
println(matrix[0][1]);  // 2

// Computed elements
let sums = [1 + 2, 3 * 4, 10 - 5];
```

---

## 12. Strings

```hasab
let s = "Hello, World!";
println(len(s));             // 13
println(upper(s));           // HELLO, WORLD!
println(lower(s));           // hello, world!
println(trim("  hi  "));     // hi
println(contains(s, "World")); // true
println(starts_with(s, "Hello")); // true
println(ends_with(s, "!"));       // true
println(reverse("abc"));    // cba
println(replace("aabb", "aa", "xx")); // xxbb
let parts = split("a,b,c", ","); // ["a", "b", "c"]
let sub = substring("hello", 1, 4); // "ell"
```

---

## 13. Type Aliases

```hasab
type UserId = int;
type Email = string;

fn get_name(uid: int) -> string {
    if uid == 1 { return "Alice"; }
    return "Unknown";
}
```

Type aliases create transparent names for existing types.

---

## 14. Built-in Functions

| Function | Signature | Description |
|----------|-----------|-------------|
| `println` | `(any) -> void` | Print with newline |
| `print` | `(any) -> void` | Print without newline |
| `len` | `(any) -> int` | Length of array or string |
| `abs` | `(int) -> int` | Absolute value |
| `sqrt` | `(float) -> float` | Square root |
| `pow` | `(float, float) -> float` | Exponentiation |
| `min` | `(int, int) -> int` | Minimum of two ints |
| `max` | `(int, int) -> int` | Maximum of two ints |
| `str` | `(any) -> string` | Convert to string |
| `now` | `() -> int` | Current timestamp (ms) |
| `to_int` | `(any) -> int` | Convert to integer |
| `to_float` | `(any) -> float` | Convert to float |
| `typeof` | `(any) -> string` | Type name as string |
| `assert` | `(bool) -> void` | Assert condition is true |
| `substring` | `(string, int, int) -> string` | Substring [start, end) |
| `contains` | `(string, string) -> bool` | Check substring |
| `trim` | `(string) -> string` | Trim whitespace |
| `upper` | `(string) -> string` | Uppercase |
| `lower` | `(string) -> string` | Lowercase |
| `reverse` | `(string) -> string` | Reverse string |
| `replace` | `(string, string, string) -> string` | Replace substring |
| `split` | `(string, string) -> [string]` | Split by delimiter |
| `starts_with` | `(string, string) -> bool` | Check prefix |
| `ends_with` | `(string, string) -> bool` | Check suffix |

---

## 15. Modules

```hasab
mod geometry {
    pub struct Point {
        x: float,
        y: float,
    }
}

use geometry::Point;
```

---

## 16. Null Safety

```hasab
let name: string? = nil;

// Safe navigation
let len_name = name?.length;  // nil if name is nil

// Null assertion (may throw at runtime)
let forced = name!!;
```

---

## 17. Diagnostic Codes

HASAB uses stable diagnostic codes for compiler messages:

### Semantic Diagnostics (HSB2xxx)

| Code | Name | Description |
|------|------|-------------|
| HSB2001 | UNDEFINED_VARIABLE | Variable not found |
| HSB2002 | DUPLICATE_DECLARATION | Name already declared in scope |
| HSB2003 | UNDEFINED_TYPE | Type not found |
| HSB2004 | UNDEFINED_FUNCTION | Function not found |
| HSB2005 | WRONG_ARGUMENT_COUNT | Wrong number of arguments |
| HSB2006 | TYPE_MISMATCH | Type incompatibility |
| HSB2007 | NOT_CALLABLE | Expression is not callable |
| HSB2008 | NOT_INDEXABLE | Cannot use [] on this type |
| HSB2009 | NO_SUCH_FIELD | Field does not exist on type |
| HSB2010 | CANNOT_ASSIGN | Cannot assign to expression |
| HSB2011 | MUTABILITY_VIOLATION | Cannot modify immutable variable |
| HSB2013 | UNUSED_VARIABLE | Variable declared but never used |
| HSB2015 | BREAK_OUTSIDE_LOOP | `break` outside of loop |
| HSB2016 | CONTINUE_OUTSIDE_LOOP | `continue` outside of loop |
| HSB2017 | RETURN_TYPE_MISMATCH | Return value type mismatch |
| HSB2021 | DUPLICATE_PARAMETER | Duplicate parameter name in function |
| HSB2022 | DUPLICATE_FIELD | Duplicate field name in struct |
| HSB2028 | SELF_OUTSIDE_IMPL | `self` used outside impl block |

### Type Diagnostics (HSB3xxx)

| Code | Name | Description |
|------|------|-------------|
| HSB3001 | TYPE_MISMATCH | Type incompatibility |
| HSB3005 | WRONG_ARGUMENT_COUNT | Wrong number of arguments |
| HSB3006 | ARGUMENT_TYPE_MISMATCH | Argument type mismatch |
| HSB3007 | RETURN_TYPE_MISMATCH | Return type mismatch |
| HSB3008 | NOT_CALLABLE | Expression is not callable |
| HSB3009 | NOT_INDEXABLE | Cannot index expression |
| HSB3010 | NO_SUCH_FIELD | No such field on type |
| HSB3013 | MUTABILITY_VIOLATION | Cannot modify immutable variable |
| HSB3016 | ARITH_TYPE_ERROR | Arithmetic type error |
| HSB3017 | COMPARE_TYPE_ERROR | Comparison type error |
| HSB3020 | CANNOT_INFER | Cannot infer type |

All diagnostics include bilingual messages (English and Amharic) and actionable suggestions where applicable.

---

## 18. Example: FizzBuzz

```hasab
fn main() {
    let mut i = 1;
    while i <= 30 {
        if i % 15 == 0 {
            println("FizzBuzz");
        } else {
            if i % 3 == 0 {
                println("Fizz");
            } else {
                if i % 5 == 0 {
                    println("Buzz");
                } else {
                    println(i);
                }
            }
        }
        i += 1;
    }
}
```

---

*HASAB Language Reference — Version 0.9*
