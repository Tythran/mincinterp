#!/bin/bash

javac *.java

for i in *.minc; do
  num=$(echo "$i" | grep -o -E '[0-9]+')
  if [ -f "$i" ]; then
    java Program "$i" > "testout$num.txt"
  else
    echo "$i not found!"
  fi
done

all_diffs_empty=true

for i in $(seq 1 8); do 
  if [ -f "testout$i.txt" ]; then
    if ! diff -cb "testout$i.txt" "testsolu$i.txt" > /dev/null; then
      all_diffs_empty=false
      diff -cb "testout$i.txt" "testsolu$i.txt"
    fi
  else
    echo "testout$i.txt not found!"
    all_diffs_empty=false
  fi
done

if $all_diffs_empty; then
  echo "All tests passed!"
else
  echo "Some tests failed."
fi