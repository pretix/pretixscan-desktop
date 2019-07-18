!include "MUI2.nsh"

 Name "pretixSCAN"
 OutFile "dist/pretixSCAN.exe"

 InstallDir "$PROGRAMFILES64\pretixSCAN"

 InstallDirRegKey HKCU "Software\pretixSCAN" ""

 RequestExecutionLevel admin

 !define MUI_ABORTWARNING
 !define MUI_ICON "packaging\icons\windows\pretixSCAN.ico"
 !define MUI_WELCOMEFINISHPAGE_BITMAP "..\img\installer.bmp"
 !define MUI_UNWELCOMEFINISHPAGE_BITMAP "..\img\installer.bmp"

 !insertmacro MUI_PAGE_WELCOME
 !insertmacro MUI_PAGE_LICENSE "..\LICENSE"
 !insertmacro MUI_PAGE_COMPONENTS
 !insertmacro MUI_PAGE_DIRECTORY
 !insertmacro MUI_PAGE_INSTFILES
 !insertmacro MUI_PAGE_FINISH

 !insertmacro MUI_UNPAGE_WELCOME
 !insertmacro MUI_UNPAGE_CONFIRM
 !insertmacro MUI_UNPAGE_INSTFILES
 !insertmacro MUI_UNPAGE_FINISH


 !insertmacro MUI_LANGUAGE "English"

 Section "pretixSCAN" SecBase
   SetOutPath "$INSTDIR"
   CreateDirectory "$INSTDIR"

   File /r "build\launch4j\*"
   File /r /x deb.tmp "jre"

   CreateDirectory "$SMPROGRAMS\pretixSCAN"
   CreateShortCut "$SMPROGRAMS\pretixSCAN\pretixSCAN.lnk" "$INSTDIR\pretixSCAN.exe"
   CreateShortCut "$SMPROGRAMS\pretixSCAN\Uninstall.lnk" "$INSTDIR\uninstall.exe"

   ;Store installation folder
   WriteRegStr HKCU "Software\pretixSCAN" "" $INSTDIR

   ;Create uninstaller
   WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixSCAN" "DisplayName" "pretixSCAN"
   WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixSCAN" "UninstallString" "$INSTDIR\uninstall.exe"

   WriteUninstaller "$INSTDIR\uninstall.exe"
 SectionEnd

 Section "Desktop Shortcut"
    CreateShortCut "$DESKTOP\pretixSCAN.lnk" "$INSTDIR\pretixSCAN.exe" ""
 SectionEnd

 ;Language strings
 LangString DESC_SecBase ${LANG_ENGLISH} "pretix Check-in application"

 ;Assign language strings to sections
 !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
 !insertmacro MUI_DESCRIPTION_TEXT ${SecBase} $(DESC_SecBase)
 !insertmacro MUI_FUNCTION_DESCRIPTION_END

 Section "Uninstall"
   Delete "$INSTDIR\*.jar"
   Delete "$INSTDIR\*.exe"
   RMDIR  /r "$INSTDIR\icons"
   RMDIR  /r "$INSTDIR\pretixSCAN"

   Delete "$SMPROGRAMS\pretixSCAN\pretixSCAN.lnk"
   Delete "$SMPROGRAMS\pretixSCAN\Uninstall.lnk"
   RMDIR "$SMPROGRAMS\pretixSCAN"
   Delete "$DESKTOP\pretixSCAN.lnk"

   RMDir /r "$INSTDIR"

   DeleteRegKey /ifempty HKCU "Software\pretixSCAN"
   DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixSCAN"
   DeleteRegKey HKCR "pretixSCAN"
 SectionEnd
