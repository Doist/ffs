# FFS

Feature flags solution that is fast, lean, and open-source.

## Documentation

Full documentation available at https://doist.github.io/ffs/.

## Project

<img src="docs/src/assets/icon-192.png" alt="FFS logo" align="right" width="192" height="192">

FFS is built on Kotlin Multiplatform and made up of 4 major components:

- A [dashboard](ffs-dashboard) to create and manage feature flags.
- SDKs to consume feature flags:
  - For [frontend](ffs-sdk-frontend) or other unsafe environments, relying on the server for evaluation results.
  - For [backend](ffs-sdk-backend) or other safe environments, evaluating feature flags locally.
- A [server](ffs-server) to provide the API for the dashboard and SDKs.

See each README for project-specific information.

## License

Released under the [MIT License](https://opensource.org/licenses/MIT).
