!include "MUI2.nsh"

 Name "pretixdesk"
 OutFile "dist/pretixdesk.exe"

 InstallDir "$PROGRAMFILES64\pretixdesk"

 InstallDirRegKey HKCU "Software\pretixdesk" ""

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

 Section "pretixdesk" SecBase
   SetOutPath "$INSTDIR"
   CreateDirectory "$INSTDIR"

   File /r "build\launch4j\*"
   File /r "jre"

   CreateDirectory "$SMPROGRAMS\pretixdesk"
   CreateShortCut "$SMPROGRAMS\pretixdesk\pretixdesk.lnk" "$INSTDIR\pretixdesk.exe"
   CreateShortCut "$SMPROGRAMS\pretixdesk\Uninstall.lnk" "$INSTDIR\uninstall.exe"

   ;Store installation folder
   WriteRegStr HKCU "Software\pretixdesk" "" $INSTDIR

   DetailPrint "Register pretixdesk URI Handler"
   DeleteRegKey HKCR "pretixdesk"
   WriteRegStr HKCR "pretixdesk" "" "URL: pretixdesk"
   WriteRegStr HKCR "pretixdesk" "URL Protocol" "pretixdesk setup URLs"
   WriteRegStr HKCR "pretixdesk\DefaultIcon" "" "$INSTDIR\pretixdesk.exe"
   WriteRegStr HKCR "pretixdesk\shell" "" ""
   WriteRegStr HKCR "pretixdesk\shell\Open" "" ""
   WriteRegStr HKCR "pretixdesk\shell\Open\command" "" "$INSTDIR\pretixdesk.exe %1"

   ;Create uninstaller
   WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixdesk" "DisplayName" "pretixdesk"
   WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixdesk" "UninstallString" "$INSTDIR\uninstall.exe"

   WriteUninstaller "$INSTDIR\uninstall.exe"
 SectionEnd

 Section "Desktop Shortcut"
    CreateShortCut "$DESKTOP\pretixdesk.lnk" "$INSTDIR\pretixdesk.exe" ""
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
   RMDIR  /r "$INSTDIR\pretixdesk"

   Delete "$SMPROGRAMS\pretixdesk\pretixdesk.lnk"
   Delete "$SMPROGRAMS\pretixdesk\Uninstall.lnk"
   RMDIR "$SMPROGRAMS\pretixdesk"
   Delete "$DESKTOP\pretixdesk.lnk"

   RMDir "$INSTDIR"

   DeleteRegKey /ifempty HKCU "Software\pretixdesk"
   DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pretixdesk"
   DeleteRegKey HKCR "pretixdesk"
 SectionEnd