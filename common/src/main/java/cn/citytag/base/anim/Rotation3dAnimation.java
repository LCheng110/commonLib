package cn.citytag.base.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by liucheng on 2017/9/11.
 */

public class Rotation3dAnimation extends Animation {
    public static final int ROTATE_X = 0;//沿着x轴旋转
    public static final int ROTATE_Y = 1;//沿着y轴旋转
    private float mCenterX;
    private float mCenterY;
    private float mDepthZ;
    private int mDirection;
    private Camera mCamera;

    public Rotation3dAnimation(float centerX, float centerY, int direction) {
        mCenterX = centerX;
        mCenterY = centerY;
        mDirection = direction;
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float degrees = 360 * interpolatedTime;

        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;

        final Matrix matrix = t.getMatrix();

        camera.save();

        if (centerX != 0) {
            if (interpolatedTime < 0.5) {
                camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
            } else {
                camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
            }
        }

        switch (mDirection) {
            case ROTATE_X:
                camera.rotateX(degrees);
                break;
            case ROTATE_Y:
                camera.rotateY(degrees);
                break;
        }

        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }

    public void setmDepthZ(float mDepthZ) {
        this.mDepthZ = mDepthZ;
    }
}