#!/usr/bin/env bash
set -euo pipefail
mkdir -p device-checks
trap 'adb logcat -d > device-checks/logcat.txt' EXIT
adb install -r dist/Mustang-Player.apk
adb install -r dist/player-tests.apk
adb shell pm grant com.dogra.mustang android.permission.POST_NOTIFICATIONS
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
adb logcat -c
adb shell am instrument -w com.dogra.mustang.test/android.test.InstrumentationTestRunner | tee device-checks/tests.txt
grep -Eq 'OK \([0-9]+ tests?\)' device-checks/tests.txt
adb shell am start -W -n com.dogra.mustang/.MainActivity
adb shell uiautomator dump /sdcard/mustang.xml
adb pull /sdcard/mustang.xml device-checks/home.xml
adb exec-out screencap -p > device-checks/home.png
adb logcat -d > device-checks/logcat.txt
if grep -q 'FATAL EXCEPTION' device-checks/logcat.txt; then exit 1; fi
