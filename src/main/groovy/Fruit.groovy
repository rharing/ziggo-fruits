/**
 * Created on 6/13/22.
 */
class Fruit {
    String type
    String color
    String productstate

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Fruit fruit = (Fruit) o

        if (color != fruit.color) return false
        if (type != fruit.type) return false

        return true
    }

    int hashCode() {
        int result
        result = (type != null ? type.hashCode() : 0)
        result = 31 * result + (color != null ? color.hashCode() : 0)
        return result
    }
}
