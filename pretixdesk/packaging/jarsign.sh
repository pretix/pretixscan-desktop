jarsigner -keystore codesign.p12 -storetype pkcs12 -sigfile SIG -signedjar dist/pretixdesk.jar \
          build/libs/pretixdesk.jar "raphael michel’s comodo ca limited id"
