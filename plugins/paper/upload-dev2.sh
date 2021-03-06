#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/target"

plugin="$(ls -r Monumenta_*.jar | grep -v sources | head -n 1)"
if [[ -z "$plugin" ]]; then
	exit 1
fi

echo "Plugin version: $plugin"

ssh -p 8822 epic@admin.playmonumenta.com "cd /home/epic/dev2_shard_plugins && rm -f Monumenta*.jar"
scp -P 8822 $plugin epic@admin.playmonumenta.com:/home/epic/dev2_shard_plugins/
