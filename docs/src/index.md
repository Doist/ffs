---
hide:
- navigation
---

# FFS

Feature flags that are fast ⚡, lean 🤏, and open-source 🌍.

## Project

FFS is built on Kotlin Multiplatform and made up of 4 major components:

- A [dashboard](dashboard) to create and manage feature flags.
- [SDKs](sdks) to consume feature flags:
    - For frontends or other unsafe environments, relying on the server for evaluation results.
    - For backends or other safe environments, evaluating feature flags locally.
- A [server](server) to provide the [API](api) for the dashboard and SDKs.

## Getting started

Getting started is as simple as:

1. Head over to https://ffs.delivery (or your own instance).
2. Create an [organization](dashboard/organizations-and-projects.md).
3. Generate an [API token](dashboard/api-tokens.md).
4. Set up an [SDK](sdks/index.md).
5. Create [feature flags](dashboard/feature-flags.md).

The remaining docs go over components, workflows, and advanced options.

## Why

Because no other solutions nail this specific intersection of goals.

⚡ Updates using [server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events). Changes are propagated instantly to clients.  
⚡ Evaluates over 40,000 [rules](https://github.com/Doist/ffs/blob/main/ffs-server/src/benchmark/kotlin/rule/RuleEvalBenchmark.kt) per second. On a Raspberry Pi 4 Model B. Per core.  
⚡ Dedicated frontend library for JavaScript. No compromises where bundle size matters.

🤏 Limited in feature set and code surface. It does few things, and it does them right.  
🤏 No bloat. Complimentary functions should be handled by complimentary tools.  
🤏 Easy to grasp, maintain, and improve across all libraries and server.

🌍 Open-source first and foremost. No special versions or closed add-ons for enterprise.  
🌎 Easy to set up and deploy. We can run it for you, but out of convenience, not complexity.  
🌏 All contributions are welcome, and we're happy to help along the way.

## Technology

[Kotlin](https://kotlinlang.org/) is the backbone of FFS. It uses [Kotlin/JS](https://kotlinlang.org/docs/js-overview.html) in the dashboard, to [Kotlin/Multiplatform](https://kotlinlang.org/docs/multiplatform.html) in the SDKs, to [Kotlin/JVM](https://kotlinlang.org/docs/server-overview.html) in the server.

While this optimizes development time, a larger benefit is correctness. Sharing code and tests across projects and platforms helps ensure consistent behavior, and that any singular fix or improvement immediately benefits multiple components.

Cross-platform has a bad rap, but Kotlin inverts the usual paradigm. By compiling down to and deeply integrating with each platform it supports, instead of trying to replace it, Kotlin provides the benefits of code sharing without most interoperability or performance challenges.
