!include "MUI2.nsh"

 Name "pretixSCAN"
 OutFile "dist/pretixscan.exe"

 InstallDir "$PROGRAMFILES64\pretixscan"

 InstallDirRegKey HKCU "Software\pretixscan" ""

 RequestExecutionLevel admin

 !define MUI_ABORTWARNING
 !define MUI_ICON "..\img\icon_on_shape.ico"
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

 Section "pretixscan" SecBase
   SetOutPath "$INSTDIR"
   CreateDirectory "$INSTDIR"

   File /r "build\launch4j\*"
   File /r /x deb.tmp "jre"

   CreateDirectory "$SMPROGRAMS\pretixscan"
   CreateShortCut "$SMPROGRAMS\pretixscan\pretixsSCAN.lnk" "$INSTDIR\pretixscan.exe"
   CreateShortCut "$SMPROGRAMS\pretixscan\Uninstall.lnk" "$INSTDIR\uninstall.exe"

   ;Store installation folder
   WriteRegStr HKCU "Software\pretixscan" "" $INSTDIR

   ;Create uninstaller
   WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixscan" "DisplayName" "pretixSCAN"
   WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixscan" "UninstallString" "$INSTDIR\uninstall.exe"

   WriteUninstaller "$INSTDIR\uninstall.exe"
 SectionEnd

 Section "Desktop Shortcut"
    CreateShortCut "$DESKTOP\pretixSCAN.lnk" "$INSTDIR\pretixscan.exe" ""
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
   RMDIR  /r "$INSTDIR\pretixscan"
   RMDIR  /r "$INSTDIR\lib"
   RMDIR  /r "$INSTDIR\jre"

   Delete "$SMPROGRAMS\pretixscan\pretixSCAN.lnk"
   Delete "$SMPROGRAMS\pretixscan\Uninstall.lnk"
   RMDIR "$SMPROGRAMS\pretixscan"
   Delete "$DESKTOP\pretixSCAN.lnk"

   RMDir "$INSTDIR"

   DeleteRegKey /ifempty HKCU "Software\pretixscan"
   DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixscan"
   DeleteRegKey HKCR "pretixscan"
 SectionEnd
