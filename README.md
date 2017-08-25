Do app/build.gradle trzeba dodac:
buildConfigField "long", "TIMESTAMP", System.currentTimeMillis() + "L"
