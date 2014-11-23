spawncamping-octo-ninja
=======================

Transparent proxy objects in java (proof of concept)

This is an unmaintained, useless, sample project which serves mostly as
proof of concept.

The idea is that it loads java bytecode, which is then rewritten so that
accesses to specific types of objects (say type T) are checked at runtime
whether they are actually objects of type T or some predefined type proxy
type, ProxyT. If they are found to be a proxy object, then each method
call or field access operation is wrapped by a simple "getObject()",
"commitObject()" transaction.
