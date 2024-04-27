#!/usr/bin/env bash
set -eu

./sendEmail.pl -f jpsember@gmail.com      \
            -t jpsember@gmail.com    \
            -s smtp.gmail.com:587  \
            -u "Test email"        \
            -m "Hi buddy, this is a test email."
