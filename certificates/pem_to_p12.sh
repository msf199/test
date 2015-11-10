#!/bin/sh
#http://zewaren.net/site/?q=node/131
openssl pkcs12 -export -inkey upfit.key -in upfit.pem -out final-prod.p12
