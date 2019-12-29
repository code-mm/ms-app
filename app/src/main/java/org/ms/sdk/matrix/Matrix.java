package org.ms.sdk.matrix;

public class Matrix {
    private static final Matrix instance = new Matrix();

    private Matrix() {
    }

    public static Matrix getInstance() {
        return instance;
    }


    public void setBaseUrl(final String url) {

    }

    public void login(final String username, final String password, MatrixCallBack callBack) {

    }
}
