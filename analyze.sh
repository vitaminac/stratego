#!/usr/bin/env bash
./gradlew sonarqube \
  -Dsonar.projectKey=vitaminac_stratego \
  -Dsonar.organization=vitaminac-github \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=89be7544bf7650efa0e33509b7bd2648fd69b59d