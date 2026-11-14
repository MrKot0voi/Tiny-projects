package smsk.smoothscroll;

/**
 * Геометрия сетки предметов креативного экрана (локальные координаты экрана).
 * Вынесено из миксина: в Mixin-классах нельзя объявлять не-private статические поля.
 */
public final class CreativeGrid {
    /** Прямоугольник сетки: 9 колонок x 5 рядов, тайлы 18 px. */
    public static final int X0 = 8;
    public static final int Y0 = 17;
    public static final int X1 = 8 + 9 * 18;
    public static final int Y1 = 17 + 5 * 18;
    /** Слоты сетки имеют y < 108; слоты хотбара ниже. */
    public static final int SLOT_Y_LIMIT = 108;

    private CreativeGrid() {
    }
}
