
log -p v -t "Root File"  "Root File Started"

#!/system/bin/sh

# Mount system writable
/system/bin/mount -o rw,remount /system

#log -p v -t "Root File"  "Root File Operation 1"

# Delete OLD binary and superuser app
#/system/bin/rm /system/bin/su
/system/bin/rm /system/xbin/su
#log -p v -t "Root File"  "Root File Operation 2"
#/system/bin/rm /system/app/Superuser.apk

# Copy files to system
/system/bin/cp /sdcard/Download/su /system/xbin/su
/system/bin/cp /sdcard/Download/daemonsu /system/xbin/daemonsu
/system/bin/cp /sdcard/Download/busybox /system/xbin/busybox
#/system/bin/cp /sdcard/Download/SuperUser.apk /system/app/SuperUser.apk
/system/bin/cp /sdcard/Download/install-recovery.sh /system/etc/install-recovery.sh

#log -p v -t "Root File"  "Root File Operation 3"

# Install Busybox
/system/bin/chown 0.1000 /system/xbin/busybox
/system/bin/chmod 0755 /system/xbin/busybox
/system/xbin/busybox --install -s /system/xbin

#log -p v -t "Root File"  "Root File Operation 4"

# Set permission
/system/bin/chown 0.0 /system/app/SuperUser.apk
/system/bin/chmod 0644 /system/app/SuperUser.apk

/system/bin/chown 0.0 /system/etc/install-recovery.sh
/system/bin/chmod 6755 /system/etc/install-recovery.sh

/system/bin/chown 0.0 /system/xbin/su
/system/bin/chmod 6755 /system/xbin/su

/system/bin/chown 0.0 /system/xbin/daemonsu
/system/bin/chmod 6755 /system/xbin/daemonsu

#log -p v -t "Root File"  "Root File Operation 5"

# Totally clean up VRoot
#/system/bin/rm -r /dev/com.mgyun.shua.su.daemon
#/system/bin/rm -r /dev/com.mgyun.shua.su

# Disable KNOX
#/system/bin/pm disable com.sec.knox.seandroid

log -p v -t "Root File"  "Root File Ended"

# Reboot
# adb reboot
