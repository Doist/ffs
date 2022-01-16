# API

Semantic REST API for FFS.

## Design

The design follows [shallow nesting](https://guides.rubyonrails.org/routing.html#shallow-nesting), with each route having the minimal amount of information to uniquely identify the resource. It strikes a balance between descriptive routes and deep nesting.

For example, to create a project within an organization:

```
POST /organizations/42/projects 
```

To update it, the project id suffices:

```
PUT /projects/123
```

Otherwise, it's a regular REST API, attempting to use verbs and paths meaningfully.

## Response codes

Response codes are used consistently, e.g.:

- `200` when obtaining a resource.
- `201` when creating a resource, with the resource path included in the `Location` header.
- `204` when updating or deleting a resource.
- `400` when parameters are missing or invalid.
- `401` when a request lacks authentication.
- `403` when a request is authenticated, but not authorized.

## Versioning

The API supports versioning in the first segment of the path. If omitted, it defaults to the latest version.

## Authentication

There are two primary modes of authentication:

- Via **user session**, with varying permissions per organization.
- Via **token**, which encodes access and permission to a specific project.

The former is applicable to users using the dashboard. The latter is most relevant for the SDK.

## Streaming

There are endpoints which support [server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events), for clients that request streaming.

## Formats

The API supports JSON. It could support CBOR, but the benefits are questionable. The streaming APIs are locked into one specific format, and JavaScript clients don't benefit from better performance even though they see a penalty in bundle size.
