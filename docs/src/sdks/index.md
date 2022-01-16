# SDKs

FFS provides two kind of SDKs:

- **Frontend SDKs** rely on the server to evaluate feature flags and synchronize the results. They are meant for unsafe environments, like frontends.
- **Backend SDKs** rely on the server to provide feature flag data, and evaluate them locally. They are meant for safe environments, like backends.

The API is essentially the same for both. The only difference is how they work under the hood.

## Workflow

The workflow can be as simple as:

1. Initialize FFS.
2. Check if flags are enabled or not.

You can also:

- Specify metadata to be used in rule evaluation.
- Connect to a different URL (e.g., your own instance).
- Enable or disable live updates.
- Run a callback when FFS initializes with fresh data.
- Grab all flagg evaluations at once.
- Shutdown explicitly, to free up resources, or stop live updates.

## Availability

The API is consistent across all SDKs, with frontend and backend variants available for each. All features available everywhere.

## Testing

TODO: after implementing, describe how to leverage the test library
