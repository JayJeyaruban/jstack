package jstack.checkpoint

typealias Key = List<KeyPart>
typealias KeyPart = String

fun Key.append(part: KeyPart) = toMutableList().apply { add(part) }.toList()
