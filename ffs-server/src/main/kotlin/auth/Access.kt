package doist.ffs.auth

data class Access(val resource: Resource, val permissions: Collection<Permission>)
