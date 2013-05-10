package com.korotovsky.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @link http://stackoverflow.com/a/6702643/1563234
 */
public class Callback {
    private String methodName;
    private Object scope;

    public Callback(Object scope, String methodName) {
        this.methodName = methodName;
        this.scope = scope;
    }

    public Object invoke(Object... parameters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = scope.getClass().getMethod(methodName, getParameterClasses(parameters));
        return method.invoke(scope, parameters);
    }

    private Class[] getParameterClasses(Object... parameters) {
        Class[] classes = new Class[parameters.length];
        for (int i=0; i < classes.length; i++) {
            classes[i] = parameters[i].getClass();
        }
        return classes;
    }
}