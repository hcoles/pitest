package org.pitest.sequence;

public class Result<T> {
    private final boolean result;
    private final Context context;

    Result(boolean result, Context context) {
        this.result = result;
        this.context = context;
    }

    public static Result result(boolean result, Context context) {
        return new Result(result, context);
    }

    public boolean result() {
        return result;
    }

    public Context context() {
        return context;
    }

}
