package com.google.j2cl.transpiler.readable.nativejstypes;

import jsinterop.annotations.JsType;

/**
 * Native JsType without "namespace" or "name".
 */
@JsType(isNative = true)
public class Bar {
  public int x;
  public int y;

  public Bar(int x, int y) {};

  public native int product();
}
