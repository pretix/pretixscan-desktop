jarsigner -keystore codesign.p12 -storetype pkcs12 -sigfile SIG -signedjar dist/pretixscan.jar \
          build/libs/pretixscan.jar "raphael michel’s comodo ca limited id"
