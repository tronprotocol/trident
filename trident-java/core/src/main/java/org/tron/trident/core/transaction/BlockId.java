package org.tron.trident.core.transaction;

import org.tron.trident.core.utils.Sha256Hash;

import java.util.Arrays;

public class BlockId extends Sha256Hash {

  private long num;

  /**
   * Use {@link #wrap(byte[])} instead.
   */
  public BlockId(Sha256Hash hash, long num) {
    super(num, hash);
    this.num = num;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || (getClass() != o.getClass() && !(o instanceof Sha256Hash))) {
      return false;
    }
    return Arrays.equals(getBytes(), ((Sha256Hash) o).getBytes());
  }

  public String getString() {
    return "Num:" + num + ",ID:" + super.toString();
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public int compareTo(Sha256Hash other) {
    if (other.getClass().equals(BlockId.class)) {
      long otherNum = ((BlockId) other).getNum();
      return Long.compare(num, otherNum);
    }
    return super.compareTo(other);
  }

  public long getNum() {
    return num;
  }
}


