#!/bin/sh

rm -v build/classes/sample/Sample.class && \
	cp -av sootOutput/sample/ build/classes/	&& \
	java -cp build/classes/ sample.Sample
