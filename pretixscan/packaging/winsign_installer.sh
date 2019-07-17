CERT=authenticode.spc
KEY=authenticode.pvk
signcode \
 -spc $CERT \
 -v $KEY \
 -a sha1 -$ commercial \
 -n "pretixscan" \
 -i https://pretix.eu/ \
 -t http://timestamp.verisign.com/scripts/timstamp.dll \
 -tr 10 \
 dist/pretixscan.exe
