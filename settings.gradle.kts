rootProject.name = "ffs"

include(":ffs-server")

include(":ffs-sdk-frontend")
include(":ffs-sdk-backend")

include(":ffs-dashboard")

include(":ffs-shared")
include(":ffs-shared:client")
include(":ffs-shared:endpoints")
include(":ffs-shared:env")
include(":ffs-shared:rule")
include(":ffs-shared:session-header")
include(":ffs-shared:sse")
include(":ffs-shared:validators")

enableFeaturePreview("VERSION_CATALOGS")
