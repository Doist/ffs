# Anatomy of rules

At the heart of FFS are rules. Each feature flag is backed by a rule. It boils down to _functions_ as commonly known in programming, or _formulas_ as commonly seen in spreadsheets. Functions accept data, and return a result. That is numeric, between `0` and `1`.

When it is `0`, the flag is disabled. When `1`, the flag is enabled. When in-between, the flag is partially enabled, meaning it is enabled for some users, proportionally to the value.

## Examples

To build intuition, let's look at some examples:

- `#!js 0`: the flag is disabled.
- `#!js false`: the flag is disabled (same as `0`).
- `#!js 1`: the flag is enabled.
- `#!js true`: the flag is enabled (same as `1`).
- `#!js 0.5`: the flag is enabled for 50% of users.

Let's take it up a notch, and look at more useful rules:

- `#!js gte(now(), datetime("2038-01-19T04:14:07+01:00"))`: enabled after January 19, 2038, at 03:14:07 UTC.
- `#!js matches("*@example.com", env["user.email"])`: enabled for emails ending in `@example.com`.
- `#!js contains(cidr("192.104.36.0/24"), ip(env["user.ip"]))"`: enabled for devices in the 192.104.36.0 – 192.104.36.255 range.
- `#!js map(datetime("2022-11-08"), datetime("2022-11-15"), 0, 1, now())`: gradual rollout between November 8 and November 16, 2022.
- `#!js not(isblank(env["user.email"]))`: enabled for logged-in users (assuming their email is set when logging in).
- `#!js pow(map(datetime("2021-11-08"), datetime("2021-11-15"), 0, 1, now()), 2)`: gradual rollout again, but exponential instead of linear over the week.

## Environment

All metadata supplied by the SDK can be accessed using `env["<key>"]`. You can send _anything_ that makes sense to your app, and then use it in rules.

!!! info

    Noticed `#!js env["user.email"]` in the examples above? It was supplied by the SDK.

## Results

Results are always numerical. `true` and `false` are mapped to `1` and `0`, respectively. Any result that's neither a number nor boolean is treated as `0`.

## Functions

| Name     | Type       | Syntax                                         | Description                                                                                                      |
|----------|------------|------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| isblank  | Info       | `isblank(value)`                               | Checks if the value is blank.                                                                                    |
| eq       | Operator   | `eq(a, b)`                                     | Checks if `a` is equal to `b`.                                                                                   |
| gt       | Operator   | `gt(a, b)`                                     | Checks if `a` is greater than `b`.                                                                               |
| gte      | Operator   | `gte(a, b)`                                    | Checks if `a` is greater than or equal to `b`.                                                                   |
| lt       | Operator   | `lt(a, b)`                                     | Checks if `a` is less than `b`.                                                                                  |
| lte      | Operator   | `lte(a, b)`                                    | Checks if `a` is less than or equal to `b`.                                                                      |
| now      | Date       | `now()`                                        | Returns the seconds since epoch the current date and time.                                                       |
| datetime | Date       | `datetime(value)`                              | Returns the seconds since epoch for date and time. `value` must follow ISO-8601's format date and time, or date. |
| ip       | IP Address | `ip(value)`                                    | Returns a numeric value for the IPv4 address.                                                                    |
| cidr     | IP Address | `cidr(value)`                                  | Returns a numeric range for an IPv4 address range. `value` must follow CIDR notation.                            |
| matches  | Lookup     | `matches(regex, value)`                        | Checks if `value` matches `regex`.                                                                               |
| contains | Lookup     | `contains(list, value)`                        | Checks if `list` contains `value`.                                                                               |
| contains | Lookup     | `contains([min:max], value)`                   | Checks if `value' is within `min` and `max` (end inclusive).                                                     |
| not      | Logical    | `not(value)`                                   | Negates the result of `value`.                                                                                   |
| and      | Logical    | `and(a, b)`                                    | Checks if both `a` and `b` are true.                                                                             |
| or       | Logical    | `or(a, b)`                                     | Checks if either `a` or `b` is true.                                                                             |
| if       | Logical    | `if(condition, a, b)`                          | If `condition` is true, returns `a`. Otherwise, returns `b`.                                                     |
| plus     | Operator   | `plus(a, b)`                                   | Adds `a` and `b`.                                                                                                |
| minus    | Operator   | `minus(a, b)`                                  | Subtracts `b` from `a`.                                                                                          |
| times    | Operator   | `times(a, b)`                                  | Multiplies `a` and `b`.                                                                                          |
| div      | Operator   | `div(a, b)`                                    | Divides `a` by `b`.                                                                                              |
| rem      | Operator   | `rem(a, b)`                                    | Returns the remainder of `a` divided by `b`.                                                                     |
| log      | Math       | `log(a, b)`                                    | Returns the logarithm of `a` to the base of `b`.                                                                 |
| ln       | Math       | `ln(value)`                                    | Returns the natural logarithm of `value`.                                                                        |
| pow      | Math       | `pow(a, b)`                                    | Returns `a` to the power of `b`.                                                                                 |
| exp      | Math       | `exp(value)`                                   | Returns `e` to the power of `value`.                                                                             |
| map      | Math       | `map(instart, inend, outstart, outend, value)` | Maps `value` over the range `[instart, inend]` and returns the result in the range `[outstart, outend]`.         |

