package ai;

public class Pair<K, V> {

  public final K key;
  public final V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pair<?, ?> pair = (Pair<?, ?>) o;

    if (!key.equals(pair.key)) return false;
    return value.equals(pair.value);

  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return key + " --> " + value;
  }
}
