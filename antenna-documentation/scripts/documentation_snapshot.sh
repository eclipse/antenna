#!/usr/bin/env bash -e
# Copyright (c) Bosch Software Innovations GmbH 2019.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

BASEDIR=$(readlink -f "$0")
BASEDIR=$(dirname "$BASEDIR")

CURRENT_BRANCH=$(git branch | grep \* | cut -d ' ' -f2)

createCurrentSnapshotDocumentation() {
  PROJECT_VERSION="$(mvn -q\
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)"

  bash "${BASEDIR}"/createOrUpdateVersionDocumentationFolder.sh "${PROJECT_VERSION}"
  git checkout ${CURRENT_BRANCH}
}

pushChanges() {
  echo $(git push "https://${GITHUB_CREDENTIALS_USR}:${GITHUB_CREDENTIALS_PSW}@github.com/bsinno/antenna.git" gh-pages)
}

createCurrentSnapshotDocumentation
push_message=$(pushChanges)
if [[ ${push_message} == *"Everything up-to-date"* ]]; then
    exit 1
fi
