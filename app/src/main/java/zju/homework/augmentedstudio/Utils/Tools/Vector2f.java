package zju.homework.augmentedstudio.Utils.Tools;

/**
 * Created by stardust on 2017/1/2.
 */

public class Vector2f {

    public float x;
    public float y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float magnitude() {
        double sum = Math.pow(this.x, 2)
                + Math.pow(this.y, 2);

        return (float) Math.sqrt(sum);
    }

    public void normalize() {
        float mag = this.magnitude();
        this.x = this.x/mag;
        this.y = this.y/mag;
    }

    public Vector2f sub(Vector2f v) {
        float dx = this.x - v.x;
        float dy = this.y - v.y;

        return new Vector2f(dx, dy);
    }

    public Vector2f add(Vector2f v) {
        float dx = this.x + v.x;
        float dy = this.y + v.y;

        return new Vector2f(dx, dy);
    }
}