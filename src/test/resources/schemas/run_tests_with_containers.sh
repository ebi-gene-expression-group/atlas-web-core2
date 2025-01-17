#!/usr/bin/env bash
# Above ^^ will test that bash is installed

dbConnection='postgresql://scxa:postgresPass@postgres/scxa-test'
docker stop postgres && docker rm postgres
docker network rm mynet
docker network create mynet
docker run --name postgres --net mynet -e POSTGRES_PASSWORD=postgresPass -e POSTGRES_USER=scxa -e POSTGRES_DB=scxa-test -p 5432:5432 -d postgres:10.3-alpine

docker run --net mynet -i \
  -v $(pwd)/tests:/usr/local/tests \
  -v $(pwd)/flyway/scxa:/usr/local/flyway/scxa \
  -v $(pwd)/flyway/gxa:/usr/local/flyway/gxa \
  -e dbConnection=$dbConnection --entrypoint=/usr/local/tests/run_tests.sh \
  quay.io/ebigxa/atlas-schemas-base:1.0
