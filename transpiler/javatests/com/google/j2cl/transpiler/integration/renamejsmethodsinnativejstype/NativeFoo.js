goog.module('com.acme.MyFoo');

class MyFoo {
  constructor() {
    this.x = 40;
    this.y = 2;
  }

  mysum() { return this.x + this.y; }
};

exports = MyFoo;
