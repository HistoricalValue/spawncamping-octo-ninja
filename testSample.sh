#!/bin/sh

rm -v build/classes/sample/Sample.class && \
	cp -av sootOutput/sample/Sample.class build/classes/sample/	&& \
	java -cp build/classes/ sample.Sample
