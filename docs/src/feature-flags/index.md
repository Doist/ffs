# Feature flags

Feature flags can be used to enable or disable functionality remotely without changing code. They decouple deployment from delivery, allowing teams to iterate faster and deploy more frequently, both [key indicators](https://cloud.google.com/blog/products/devops-sre/using-the-four-keys-to-measure-your-devops-performance) of a high-performing team. Read more about the general technique in [Pete Hodgson's article](https://martinfowler.com/articles/feature-toggles.html).

To provide some examples, feature flags can be:

- Manually enabled or disabled
- Scheduled to be enabled or disabled at a specific date
- Enabled for specific sessions, users, countries, time zones, system loads, etc
- Gradually enabled over time for a group of users

And depending on the metadata you provide to it via the SDK, you can do much more. [Rules are powerful and flexible.](anatomy-of-rules.md)
