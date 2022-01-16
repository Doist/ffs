rootProject.name = "ffs"

include(":ffs-server")

include(":ffs-library-frontend")
include(":ffs-library-backend")

include(":ffs-dashboard")

include(":ffs-shared")
include(":ffs-shared:client")
include(":ffs-shared:env")
include(":ffs-shared:rule")
include(":ffs-shared:sse")
include(":ffs-shared:validators")

enableFeaturePreview("VERSION_CATALOGS")
