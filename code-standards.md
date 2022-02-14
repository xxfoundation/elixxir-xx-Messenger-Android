# The Official XX Network Kotlin Style Guide (in progress)

## Goals

Use of this style guide ensures:

* Maintainability
* Conciseness
* Readability
* Simplicity

## Inspiration

Most of this guide statements are drawn or adapted from one of the following sources:

[**Google Java Style Guide**](https://google.github.io/styleguide/javaguide.html) - Old school can still be contemporary

[**Android Kotlin Style Guide**](https://android.github.io/kotlin-guides/style.html) - Official Android  style guide

[**Kotlin Coding Conventions**](https://kotlinlang.org/docs/reference/coding-conventions.html) - Official Kotlin guidelines for coding

[**Android Contributors Style Guide**](https://source.android.com/source/code-style.html) - Contribution insights on open sourcing

[**Google Swift Guide**](https://google.github.io/swift/) - General Formatting and Rules that can be applied to Kotlin, as they share many syntax similarities


## Table of Contents

<details>
<summary markdown="span">
Click to expand
</summary>

- [Naming](#naming)
    + [Packages](#packages)
    + [Classes, Objects & Interfaces](#classes-objects--interfaces)
    + [Methods](#methods)
    + [Properties](#properties)
    + [Variables](#variables)
    + [Other](#other)
- [Resources Naming](#resources-naming)
    + [General Rule](#general-rule)
    + [Main Types](#main-types)
    + [Icons](#icons)
    + [Selector States](#selector-states)
    + [Strings](#strings)
    + [Dimensions](#dimensions)
    + [Layouts](#layouts)
- [Formatting](#formatting)
    + [Indentation](#indentation)
    + [Line Wrapping](#line-wrapping)
    + [Line Break](#line-break)
    + [Break At Operators](#break-at-operators)
    + [Assignment Operator](#assignment-operator)
    + [Method Calls in Chain](#method-calls-in-chain)
    + [Long Parameters Case](#long-parameters-case)
    + [Vertical Indentation](#vertical-indentation)
    + [Horizontal Indentation](#horizontal-indentation)
    + [Horizontal Operators Indentation](#horizontal-operators-indentation)
- [Modifiers](#modifiers)
	+ [Public Modifier](#public-modifier)
	+ [Other Visibility Modifiers](#other-visibility-modifiers)
- [Classes, Variables & Objects](#classes-variables--objects)
    + [Variable Declaration](#variable-declaration)
	+ [Classes](#classes)
	+ [Data Classes](#data-classes)
	+ [Enum Classes](#enum-classes)
	+ [Getters & Setters](#getters--setters)
    + [Companion Objects](#companion-objects)
- [Conditionals & Braces](#conditionals--braces)
    + [Braces](#braces)
    + [If Statements](#if-statements)
    + [When Statements](#when-statements)
- [Types](#types)
    + [Immutable Types](#immutable-types)
    + [Nullable Types](#nullable-types)
    + [Type Inference](#type-inference)
- [Misc](#misc)
	+ [Comments](#comments)
	+ [Language](#language)

</details>

## Naming

Kotlin is a JVM-compatible language like Java, and naming should follow Kotlin standards.

### Packages

- Package names are all __lower-case__
- Multiple words are discouraged
- If multiple words are needed, they can be used concatenated together, without hyphens or underscores

__BAD__ ⛔️:

```kotlin
io.Elixxir.some_widget
```

__GOOD__ ✅:

```kotlin
io.elixxir.somewidget
```

### Classes, Objects & Interfaces

- Written in __UpperCamelCase__

__BAD__ ⛔️:

 ```kotlin
open class myExampleClass { /*...*/ }
object emptyObject : myExampleClass() { /*...*/ }
interface myInterface { /*...*/}
```

__GOOD__ ✅:

 ```kotlin
open class MyExampleClass { /*...*/ }
object EmptyExampleClass : MyExampleClass() { /*...*/ }
interface MyInterface { /*...*/}
```

### Methods

- Written in __lowerCamelCase__, without hyphens or underscores

__BAD__ ⛔️:

 ```kotlin
fun ProcessRequests() { /*...*/ }
var finishedCount = 1
```

__GOOD__ ✅:

 ```kotlin
fun processRequests() { /*...*/ }
var finishedCount = 1
```

### Properties

- Generally, written in __lowerCamelCase__
- Properties should **not** be named with Hungarian notation, as it is as it is  [**not recommended**](http://jakewharton.com/just-say-no-to-hungarian-notation/)  by Google
- Constant values in the companion object should be written all __uppercase__ using underscores in cases with multiple words

Example property names

__BAD__ ⛔️:

```kotlin
class MyClass {
  var publicfield: Int = 0
  val Person = Person()
  private var mPrivateField: Int?
}
```

__GOOD__ ✅:

```kotlin
class MyClass {
  var publicField: Int = 0
  val person = Person()
  private var privateField: Int?
}
```

Example constant names

__BAD__ ⛔️:
```kotlin
companion object {
  const val theAnswer = 42
}
```
__GOOD__ ✅:
```kotlin
companion object {
  const val THE_ANSWER = 42
}
```

___Exception #1___
`val` properties with no custom `get` function that hold deeply immutable data) should use __uppercase underscore-separated names__.

__BAD__ ⛔️:
```kotlin
val keyName = "KeyValue" //Immutable
```
__GOOD__ ✅:
```kotlin
val KEY_NAME = "KeyValue" //Immutable
```

### Variables

- Written in __lowerCamelCase__.

__BAD__ ⛔️:
```kotlin
val HTTPSAddress = "https://xx.network"
```
__GOOD__ ✅:
```kotlin
val httpsAddress = "https://xx.network"
```
- Except for temporary looping variables, single character variables are not encouraged.

```kotlin
for (x in 0..10) { /*...*/ }
```

### Other

- Acronyms should be treated as words

| Good ✅          | Bad ⛔️           |
| --------------   | ---------------  |
| `XmlHttpRequest` | `XMLHTTPRequest` |
| `customerId`     | `customerID`     |
| `url`            | `URL`            |
| `id`             | `ID`             |

## Resources Naming  
  
### General Rule  
  
One drawable must have  **the same file name**  for all screen densities (mdpi, hdpi etc.) and the file must be added to the corresponding drawable density folder   
(drawable-mdpi, drawable-hdpi, drawable-xhdpi, drawable-xxhdpi).  
  
All resources names have the following convention **depending on the need**:  
`<LOCATION>_<TYPE>_<WHAT>_<STATE>_<COLOR>_<SIZE>`  
  
**`<LOCATION>`**   
- If the resources belongs to an specific screen only, it must start with its  name, e.g.: <code>**onboard**_feature_1.xml</code>  
  
**`<TYPE>`**  
  
- The resource type, as specified in the next topics, e.g.: <code>**btn**_gradient_big_corner.xml</code>  
- If not can specified, it can be described with a long form name, e.g.: <code>**shape**_circle_filled_white.xml</code>  
  
**`<WHAT>`**  
  
- Describes what that resource is representing, e.g.: <code>main_selector_**tab_indicator**.xml</code>  
  
**`<STATE>`**  
  
  - If the resource represents a state, it should be named accordingly,  e.g.: <code>btn_success_**pressed**.xml</code>  
   
 **`<COLOR>`**  
  - If the resource has more than one color, it should be specified,  e.g.: <code>selector_btn_**light**.xml</code> and <code>selector_btn_**dark**.xml</code>  
   
**`<SIZE>`**  
- Most used for drawables and dimensions, to represent the resource in different sizes, e.g.:  <code>ic_error_sign_red_**24dp**.xml</code> and <code>ic_error_sign_red_**32dp**.xml</code>   
  
### Main Types  
  
| Resource Type | Prefix | Example |  
|--------------| ------------------|-----------------------------|  
| Action bar   |  `ab_`  | `ab_stacked.png`          |  
| Button |  `btn_`  |  `btn_send_pressed.png`  |  
| Dialog       |  `dialog_`  | `dialog_top.png`          |  
| Divider |  `divider_`  |  `divider_horizontal.png`  |  
| Icon         |  `ic_`  | `ic_star.png`             |  
| Menu |  `menu_ `  |  `menu_submenu_bg.png`  |  
| Notification |  `notification_`  | `notification_bg.png`     |  
| Tabs |  `tab_`  |  `tab_pressed.png`  |  
| Selector     |  `selector_`  | `selector_btn_color.xml`  |  
| Shape |  `shape_`  |  `shape_rectangle_big.xml`  |  
  
### Icons  
  
| Resource Type | Prefix | Example |  
| --------------------------------| ---------------- | ---------------------------- |  
| Icons                           |  `ic_`  | `ic_star.xml`                |  
| Launcher icons |  `ic_launcher`  |  `ic_launcher_calendar.xml`  |  
| Menu icons and Action Bar icons |  `ic_menu`  | `ic_menu_archive.xml`        |  
| Status bar icons |  `ic_stat_notify`  |  `ic_stat_notify_msg.xml`  |  
| Tab icons                       |  `ic_tab`  | `ic_tab_recent.xml`          |  
| Dialog icons |  `ic_dialog`  |  `ic_dialog_info.xml`  |  
  
### Selector States  
  
| State | Suffix | Example |  
|--------------|-----------------|-----------------------------|  
| Normal       |  `_normal`  | `btn_order_normal.xml`    |  
| Pressed |  `_pressed`  |  `btn_order_pressed.xml`  |  
| Focused      |  `_focused`  | `btn_order_focused.xml`   |  
| Disabled |  `_disabled`  |  `btn_order_disabled.xml`  |  
| Selected     |  `_selected`  | `btn_order_selected.xml`  |  
  
### Strings  
  
String names should start with a prefix that identifies the section they belong to and its description:  
  
| Section | String name |  
| ----------------- | --------------------------------------|  
| Login                  |  `login_username_hint`  |  
| Registration |  `registration_help_text`  |  
| Main                   |  `main_title`  |  
| User Profile |  `user_profile_picture`  |  
  
If a string **is not associated** to an specific screen, you should follow the rules below:  
  
| Prefix | Description |  
| ----------------- | --------------------------------------|  
| `error_`             | An error message |  
|  `msg_`  | A regular information message |  
| `title_`             | A title, i.e. a dialog title |  
|  `action_`  | An action such as "Save" or "Create" |  
  
### Dimensions  
  
Apps should only define dimensions for values which are constantly reused.  
  
| Dimension Type | Dimension Name |  
| -----------------| -------------------------------------- |  
| typical spacing  |  `<dimen name="spacing_xsmall">8dp</dimen>`|  
| font |  `<dimen name="font_larger">22sp</dimen>`  |  
| padding          |  `<dimen name="padding_large">24dp</dimen>`|  

### Layouts  
  
Layout files should match the name of the Android locations that they are intended for, using that as a prefix:  
  
| Component | Class Name | Layout Name |  
| ---------------- | ---------------------- | ----------------------------- |  
| Activity         |  `LoginActivity`  | `activity_login.xml`          |  
| Fragment |  `SignUpFragment`  |  `fragment_sign_up.xml`  |  
| Dialog           |  `WebViewDialog`  | `dialog_web_view.xml`         |  
| Adapter |  `RecyclerView`  | --- |  
| Holder item      | --- | `item_contact.xml`            |  
| Other layout | --- |  `component_menu_bar.xml`  |  

## Formatting

Spacing is important for code readability

### Source File

All source files must be encoded as UTF-8.

### Indentation

- Indentation is using 4 spaces, never tabs
- If using tabs, consider using soft tabs (tabs converted to spaces) only. Tabs (hard tabs) are highly problematic since they correspond to different spaces depending on the text editor/IDE

__BAD ⛔️:__

```kotlin
val test: Int = 12
for (i in 10..42) {
  println(
    when {
      i < test -> -1
      i > test -> 1
      else -> 0
    }
  )
}
```

__GOOD ✅:__

```kotlin
val test: Int = 12
for (i in 10..42) {
    println(
        when {
            i < test -> -1
            i > test -> 1
            else -> 0
        }
    )
}
```

### Line Wrapping

This should follow [Google's Kotlin Style Guide](https://developer.android.com/kotlin/style-guide).

- One statement per line, no semicolon (`;`)
- Except for `package`, `import`, shell command lines in comments or long URLs, lines should be no longer than 100 characters long
- Indentation for line wraps should use 4 spaces as well

__BAD ⛔️:__

```kotlin
val foo: FunkyWidget =
        someReallyLongExpression("that would not fit in a single line")
```

__GOOD ✅:__

```kotlin
val foo: FunkyWidget =
    someReallyLongExpression("that would not fit in a single line")
```

### Line Break

Should follow some rules of [Google's Java Guide](https://google.github.io/styleguide/javaguide.html#s4.5.1-line-wrapping-where-to-break) in addition to [Google's Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) with some adaptations:

 #### Break at operators

- When a line is broken at a  _non-assignment_  operator the break comes  _before_  the symbol
    -  This also applies to the following "operator-like" symbols:
    -  The dot separator (`.`)
    -  The two colons of a member reference (`::`)

__BAD ⛔️:__

```kotlin
val longVariable = anotherVeryLongVariable + anotherLongerOne - thisRidiculousLongOne +
    theFinalOne
```

__GOOD ✅:__

```kotlin
val longVariable = anotherVeryLongVariable + anotherLongerOne
 - thisRidiculousLongOne + theFinalOne
```

#### ___Observation:___

According to [Kotlin Grammar](https://kotlinlang.org/docs/reference/grammar.html), as there is no symbol for line continuation in Kotlin, if the **first line of the statement is valid**, the **second line won't work**. To avoid that behavior, we can use parenthesis:

__BAD ⛔️:__

```kotlin
val text = "This is" + "a valid" + "statement"
    + "this " + "line " + "won\'t" + "work" // syntax error
```

__GOOD ✅:__
```kotlin
val text = ("This is" + "a valid" + "statement"
    + "this " + "line " + "will" + "work") // no syntax error
```

 #### Assignment Operator

An __exception__ to the previous rule is the assignment operator `=`, where the line break should happen **after** the operator.

- When a line is broken at an  _assignment_  operator the break typically comes  _after_  the symbol.
- A method or constructor name stays attached to the open parenthesis `(()` that follows it.
- A comma (`,`) stays attached to the token that precedes it.
- A lambda arrow (`->`) stays attached to the argument list that precedes it.

__BAD ⛔️:__

```kotlin
val foo: FunkyWidget =
        someReallyLongExpression("that would not fit in a single line")
```

__GOOD ✅:__

```kotlin
val foo: FunkyWidget =
    someReallyLongExpression("that would not fit in a single line")
```

 #### Method Calls in Chain

When multiple calls are chained in the same line,  the `Assignment Operator Rule` is applied.

__BAD ⛔️:__

```kotlin
Picasso.with(context).load("https://elixxir.io/_nuxt/img/phone.d5cc20a.png").into(imageView)
```

__GOOD ✅:__

```kotlin
Picasso.with(context)
    .load("https://elixxir.io/_nuxt/img/phone.d5cc20a.png")
    .into(imageView)
```

### Long parameters case

When a method has many parameters or its parameters are very long, we also apply  `Assignment Operator Rule`

__BAD ⛔️:__

```kotlin
loadImage(context, "https://elixxir.io/_nuxt/img/phone.d5cc20a.png", profilePictureImageView, clickListener, "XX Messenger for Smartphones")
```

__GOOD ✅:__

```kotlin
loadImage(
    context,
    "https://elixxir.io/_nuxt/img/phone.d5cc20a.png",
    profilePictureImageView,
    clickListener,
    "XX Messenger for Smartphones"
)

//or

loadImage(context,"https://elixxir.io/_nuxt/img/phone.d5cc20a.png",
    profilePictureImageView, clickListener,"XX Messenger for Smartphones"
)
```

### Vertical Indentation

Single line spacing between method. If a method has many "sections" that often means it should be split into several methods.

### Horizontal Indentation

In general, you should add a single space separation in the following cases:

- `if`, `for`, or `catch` before an open parenthesis  (`(`) that follows it on the same line
- Before any open curly brace (`{`)
- For any reserved word such as `else` or `catch`, separating it from a closing curly brace (`}`)  that precedes it

__BAD ⛔️:__

```kotlin
if(list.isEmpty()){
    doSomething()
}else{
    doSomethingElse()
}
```

__GOOD ✅:__

```kotlin
if (list.isEmpty()) {
    doSomething()
} else {
    doSomethingElse()
}
```
### Horizontal Operators Indentation

Operators other than two colons (`::`), dot (`.`) and range (`..`) should be separated on both sides:

__BAD ⛔️:__

```kotlin
val c = a+b //binary operator

list.map { item->item.toString() } //arrow operator

val toString =  Any  :: toString //two colons

it . toString() //dot

for  (i in  1  ..  10) print(i) //range operator
```

__GOOD ✅:__

```kotlin
val c = a + b //binary operator

list.map { item -> item.toString() } //arrow operator

val toString = Any::toString //two colons

it.toString() //dot

for (i in 1..10) print(i) //range operator
```

___Exceptions:___

 - Spacing must be added after a (`,`) or (`;`)
 - Spacing should be added before a colon (`:`) only if that is used for base `class` or `interface` declarations

__BAD ⛔️:__

```kotlin
class Foo: Bar

fun <T> max(a: T, b: T) where T: Comparable<T> // last T is not declaration
```

__GOOD ✅:__

```kotlin
class Foo : Bar

fun <T> max(a: T, b: T) where T : Comparable<T> // last T is not declaration
```

## Modifiers

### Public Modifier

Just like Java, the default modifier for Kotlin is `public` .  As that's redundant, it should be omitted.

__BAD ⛔️:__

```kotlin
 class Person(
    id: Int,
    name: String,
    surname: String
) : Human(id, name) { /*...*/ }
```

__GOOD ✅:__

```kotlin
class Person(
    id: Int,
    name: String,
    surname: String
) : Human(id, name) { /*...*/ }
```

### Other Visibility Modifiers

Modifiers should be explicitly used for classes, methods and member variables. According to Kotlin Coding Conventions, if the declaration has multiple modifiers, they should be put in the following order:

```kotlin
public / protected / private / internal
expect / actual
final / open / abstract / sealed / const
external
override
lateinit
tailrec
vararg
suspend
inner
enum / annotation
companion
inline
infix
operator
data
```

## Classes, Variables & Objects

### Variable Declaration

Shall have single declaration per line.

__BAD ⛔️:__

```kotlin
val username: String; val password: String
```

__GOOD ✅:__

```kotlin
val username: String
val password: String
```

### Classes

- A single class per source file
- Inner classes only when appropriate.

### Data Classes

For holding simple data,  `data classes` are preferred.

__BAD ⛔️:__

```kotlin
class Person(val name: String, val age: Int) {
  override fun toString() : String {
    return "Person(name = $name, age = $age)"
  }
}
```

__GOOD ✅:__

```kotlin
data class Person(
    val name: String = "",
    val age: Int = 0
)
```

### Enum Classes

Enum classes may be formatted as follows:

```kotlin
private enum class Color {
 RED, GREEN, BLUE, YELLOW
}

//or

private enum class Color {
   RED,
   GREEN,
   BLUE,
   YELLOW
}
```

### Getters & Setters

Unlike Java, getters and setters in Kotlin are auto-generated, and they should be only declared if needed [following Kotlin conventions](https://kotlinlang.org/docs/reference/properties.html):

```kotlin
class  Example {
   var name: String = "Elixxir"
}
```

### Companion Objects

Companion objects are similar to Java's `static` modifier, and it should only be used while declaring a function or a property to be tied to a class rather than to instances of it.

## Conditionals & Braces

### Braces

For curly braces, the opening braces will be placed in the same line that opens a scope block and the closing brace on a separate line aligned horizontally with the opening construct:

__BAD ⛔️:__

```kotlin
class MyExample
    {
        fun doSomething()
            {
                if (someTest)
                    {
                      /* some logic */
                    }
                else
                    {
                      /* some logic */
                    }
            }
    }
```

__GOOD ✅:__

```kotlin
class MyExample {
    fun doSomething() {
        if (someTest) {
            /* some logic */
        } else {
            /* some other logic */
        }
    }
}
```

### If Statements

If statements are preferable to be used for __binary conditions__, and must be always enclosed with braces for both readability and code correctness, as it makes it easier to avoid mistakes:

__BAD ⛔️:__

```kotlin
if (someCondition)
    doSomething()

if (someCondition)
    doSomething()
    doSomethingElse() //This is not part of the if scope
```

__GOOD ✅:__

```kotlin
if (someCondition) {
    doSomething()
}

//Or

if (someCondition) {
  doSomething()
  doSomethingElse() //This is part of the scope
}
```

 `if` braces can be omitted if the entire expression fits a single line:

__BAD ⛔️:__

```kotlin
var v = if (condition)
        a //that's not ok
    else
        b //that's not ok

if (condition)
    callSomeFunc() //bad
```

__GOOD ✅:__

```kotlin
var v = if (condition) a else b //that's ok

if (condition) {
    doSomething() //this is acceptable
}

if (condition) callSomeFunc() //this is also acceptable
```

### When Statements

- Preferably used when comparing one expression against many expressions.
- Separate cases using commas if they should be handled the same way.
- You must always include `else` case.

__BAD ⛔️:__

```kotlin
when (input) {
    'a' -> doSomething()
    'b' -> doSomething()
    'c' -> doSomethingElse()
}
```

__GOOD ✅:__

```kotlin
when (input) {
    'a', 'b' -> doSomethingForCaseOneOrTwo()
    'c' -> doSomethingForCaseThree()
    else -> println("No case was satisfied")
}
```

## Types

### Immutable Types

Only use `var` for values that will change over time. Otherwise, use `val`

### Type Inference

A local value initialized by an expression should preferably use type inference instead of declaring its type, where possible:

__BAD ⛔️:__

```kotlin
fun main() {
    val manufacturer: String = myCarObj.getManufacturer() //Type inference
    println("Car manufacturer: $manufacturer")
}
```

__GOOD ✅:__

```kotlin
fun main() {
    val manufacturer = myCarObj.getManufacturer() //Type inference
    println("Car manufacturer: $manufacturer")
}
```

### Nullable Types

As Kotlin compiler by default doesn’t allow any  `null` values at compile-time, only declare a type as nullable if required using `?` after type declaration.

__BAD ⛔️:__

```kotlin
var example: String = "Elixxir Style Guide"
str = null  // compilation error
```

__GOOD ✅:__

```kotlin
var example: String? = "Elixxir Style Guide"
str = null  // no error
```

Use implicitly unwrapped types (`!!`) only if the variable will be initialized before use, or the safe operator (`?`) if it is not certain the variable will be initialized:

__BAD ⛔️:__

```kotlin
var item: Example? = null
/* other calls */
item.setMsg("example")
item.msg
```

__GOOD ✅:__

```kotlin
var item: Example? = null
/* other calls */
item = Example()
/* some more calls */
item?.setMsg("example") //If unsure the value is initialized
item!!.msg              //If sure the value is initialized
```

## Misc

### Comments

In most cases, code should be self explanatory. In exceptionally clever or complicated instances, comments can be used for ease of readability and understanding. Comments must be kept up-to-date or deleted.

*Exception: This does not apply to those comments used to generate documentation.*

### Language

Use `en-US` english spelling. 🇺🇸

__BAD ⛔️:__

```kotlin
val centreAlignment = 24f
```

__GOOD ✅:__

```kotlin
val centerAlignment = 24f
```

## Copyright Statement

The following copyright statement should be included at the top of every source file:

``Copyright here``

## Credits

This style guide is a collaborative effort from:

- [Dimas](mailto:dimas@elixxir.io)