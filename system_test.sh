#!/bin/bash

OUTDIR=$1
ROOT=$(cd $(dirname $0); pwd)
CLASSPATH=$ROOT/lib/*:$ROOT/lib/jetty/*
MAINCLASS=org.projectbuendia.server.Server

if [ -z "$OUTDIR" ]; then
  echo "Usage: $0 <output-directory>"
  echo "    Builds and runs the system tests in <output-directory>."
  exit 1
fi

# Quickly check the Python file for syntax errors before doing a lot of work.
PYTHONPATH=$ROOT/tests python -c 'import system_test' || exit 1

# Compile the server.
$ROOT/build.sh $OUTDIR || exit 1
cd $OUTDIR

# Start the server.
echo -n "Starting server..."
java -cp .:$CLASSPATH $MAINCLASS >server.log 2>&1 &
pid=$!

# Wait for the server to be ready.
for attempt in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20; do
  sleep 0.2
  if curl -s http://localhost:8080/ >/dev/null; then up=yes; break; fi
  echo -n "."
done
if [ -n "$up" ]; then
  echo "server is up."
else
  echo "did not come up in 10 seconds.  Check $OUTDIR/server.log for clues."
  exit 1
fi

# Run the tests.
echo
echo -n "Running tests: "
python $ROOT/tests/system_test.py && passed=yes

# Shut down the server.
echo -n "Stopping server..."
for attempt in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20; do
  kill $pid
  sleep 0.2
  if ! ps -p $pid >/dev/null; then stopped=yes; break; fi
done
if [ -n "$stopped" ]; then
  echo "stopped."
else
  kill -9 $pid
  echo "killed."
fi

# Report the results.
if [ -n "$passed" ]; then
  echo
  echo -e '\x1b[32m \\o/ PASSED! \x1b[0m'
  echo
else
  echo
  echo -n -e '\x1b[31m >_< FAILED. \x1b[0m'" See output above or $OUTDIR/server.log for clues."
  echo
fi
