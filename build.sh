#!/usr/bin/env bash
git pull
docker build -t egreen/newsmaster_feeder:v1 .
docker stop fmms-service
docker rm fmms-service
docker run -itd --restart=always --name newsmaster-feed-service --link mongo:mongodb  egreen/newsmaster_feeder:v1
