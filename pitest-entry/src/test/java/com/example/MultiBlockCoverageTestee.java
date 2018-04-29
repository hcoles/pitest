package com.example;

public class MultiBlockCoverageTestee {

  public static int blocks1(final int i) {
    return i;
  }

  // public void blocks2(int i) {
  // switch(i) {
  // case 0:
  // System.out.println();
  // }
  // }

  public static int blocks3(int i) {
    if (i > 2) {
      return i;
    }

    return 1;
  }

  public static int blocks4(int i) {
    switch (i) {
    case 1:
      return 1;
    default:
      return 5;
    }
  }

  public static int blocks5(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    default:
      return 5;
    }
  }

  public static int blocks6(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    default:
      return 5;
    }
  }

  public static int blocks7(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    default:
      return 50;
    }
  }

  public static int blocks8(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    default:
      return 50;
    }
  }

  public static int blocks9(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    case 6:
      return 7;
    default:
      return 50;
    }
  }

  public static int blocks10(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    case 6:
      return 7;
    case 7:
      return 8;
    default:
      return 50;
    }
  }

  public static int blocks11(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    case 6:
      return 7;
    case 7:
      return 8;
    case 8:
      return 9;
    default:
      return 50;
    }
  }

  public static int blocks12(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    case 6:
      return 7;
    case 7:
      return 8;
    case 8:
      return 9;
    case 9:
      return 10;
    default:
      return 50;
    }
  }

  public static int blocks13(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    case 6:
      return 7;
    case 7:
      return 8;
    case 8:
      return 9;
    case 9:
      return 10;
    case 10:
      return 11;
    default:
      return 50;
    }
  }

  public static int blocks14(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    case 6:
      return 7;
    case 7:
      return 8;
    case 8:
      return 9;
    case 9:
      return 10;
    case 10:
      return 11;
    case 11:
      return 12;
    default:
      return 50;
    }
  }

  public static int blocks15(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    case 6:
      return 7;
    case 7:
      return 8;
    case 8:
      return 9;
    case 9:
      return 10;
    case 10:
      return 11;
    case 11:
      return 12;
    case 12:
      return 13;
    default:
      return 50;
    }
  }

  public static int blocksMany(int i) {
    switch (i) {
    case 1:
      return 1;
    case 2:
      return 3;
    case 3:
      return 4;
    case 4:
      return 5;
    case 5:
      return 6;
    case 6:
      return 7;
    case 7:
      return 8;
    case 8:
      return 9;
    case 9:
      return 10;
    case 10:
      return 11;
    case 11:
      return 12;
    case 12:
      return 13;
    case 13:
      return 14;
    case 14:
      return 15;
    case 15:
      return 16;
    case 16:
      return 17;
    case 17:
      return 18;
    case 18:
      return 19;
    case 19:
      return 20;
    case 20:
      return 21;
    default:
      return 50;
    }
  }

}
