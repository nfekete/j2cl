package com.google.j2cl.transpiler.integration.jsinteroptests;

import jsinterop.annotations.JsType;

public class JsTypeBridgeTest {
  @JsType
  private interface JsListInterface {
    void add(Object o);
  }

  private interface Collection {
    void add(Object o);
  }

  private static class CollectionBase implements Collection {
    Object x;

    public void add(Object o) {
      x = o + "CollectionBase";
    }
  }

  private interface List extends Collection, JsListInterface {
    void add(Object o);
  }

  private static class FooImpl extends CollectionBase implements Collection {
    @Override
    public void add(Object o) {
      super.add(o);
      x = x.toString() + "FooImpl";
    }
  }

  private static class ListImpl extends CollectionBase implements List {
    @Override
    public void add(Object o) {
      x = o + "ListImpl";
    }
  }

  public void testBridges() {
    ListImpl listWithExport = new ListImpl(); // Exports .add().
    FooImpl listNoExport = new FooImpl(); // Does not export .add().

    // Use a loose type reference to force polymorphic dispatch.
    Collection collectionWithExport = listWithExport;
    // Calls through a bridge method.
    collectionWithExport.add("Loose");
    assertEquals("LooseListImpl", listWithExport.x);

    // Use a loose type reference to force polymorphic dispatch.
    Collection collectionNoExport = listNoExport;
    collectionNoExport.add("Loose");
    assertEquals("LooseCollectionBaseFooImpl", listNoExport.x);

    // Calls directly.
    listNoExport.add("Tight");
    assertEquals("TightCollectionBaseFooImpl", listNoExport.x);

    listWithExport.add("Tight");
    assertEquals("TightListImpl", listWithExport.x);
  }

  private void assertEquals(Object expect, Object actual) {
    assert expect.equals(actual);
  }
}