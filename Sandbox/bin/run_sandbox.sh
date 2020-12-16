#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
MICRONAUT_SERVER_PORT=8412 java -jar ${DIR}/../build/libs/Sandbox-0.1-all.jar