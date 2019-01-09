Do app/build.gradle trzeba dodac:
buildConfigField "long", "TIMESTAMP", System.currentTimeMillis() + "L"

klucz do keystore: abcdabcd, klucz do podpisu: key0, haslo do key0: abcdabcd
