include(":app", ":bypass")
include(":about", ":core", ":dribbble", ":designernews", ":search")
include(":test_shared")
project(":bypass").projectDir = File(rootDir, "third_party/bypass")
