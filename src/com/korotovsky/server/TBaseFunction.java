package com.korotovsky.server;

interface TFunction<T1, R> {
    public R invoke(T1 t);
}

abstract class TFunction1<T1, R> implements TFunction<T1, R> {
    abstract public R invoke(T1 t);
}

abstract class TFunction2<T1, T2, R> implements TFunction<T1, TFunction<T2, R>> {
    abstract protected R invoke2(T1 arg1, T2 arg);

    @Override
    public TFunction<T2, R> invoke(T1 arg) {
        final T1 arg1 = arg;
        return new TFunction<T2, R>() {
            @Override
            public R invoke(T2 arg2) {
                return invoke2(arg1, arg2);
            }
        };
    }
}

/*
public abstract class TBaseFunction implements IBaseFunction {
    abstract public void invoke1(Object arg);
    abstract public void invoke2(Object arg1, Object arg2);
}

abstract class TFunction1 extends TBaseFunction {
    @Override abstract public void invoke1(Object arg);
    @Override private void invoke2(Object arg1, Object arg2) {}
    public void invoke(Object arg) {
        invoke1(arg);
    }
}

abstract class TFunction2 extends TBaseFunction {
    @Override abstract public void invoke1(Object arg1);
    @Override abstract public void invoke2(Object arg1, Object arg2);
    public void invoke(Object arg1, Object arg2) {
        invoke2(arg1, arg2);
    }
}
*/