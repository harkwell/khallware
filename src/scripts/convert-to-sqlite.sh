#!/bin/bash

[ $1 ] || { echo "$0: <mysql script>"; exit 1; }

OUTFILE=${1}ite
cp $1 $OUTFILE


sed -i -e 's#^SET#-- SET#g' $OUTFILE
sed -i -e 's#^DROP#-- DROP#g' $OUTFILE
sed -i -e 's#[^_]id INT.*$#\tid INTEGER PRIMARY KEY AUTOINCREMENT,#' $OUTFILE
sed -i -e 's#PRIMARY KEY (id).*$##g' $OUTFILE
[ $2 ] && sed -i -e '14s#^.*$#\tdisabled BIT(1) NOT NULL DEFAULT 0#' $OUTFILE
sed -i -e 's#OR REPLACE ##g' $OUTFILE
sed -i -e 's#^ALTER.*AUTO_INCR.*$##g' $OUTFILE
sed -i -e "s#NOW()#datetime('now')#g" $OUTFILE
