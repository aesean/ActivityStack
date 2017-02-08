/*
 *    Copyright (C) 2017.  Aesean
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.aesean.activitystack.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ReflectUtils
 * 一个简单的反射工具类。可以帮助你像写脚本语言一样调用处理java类。
 *
 * @author xl
 * @version V0.1.1
 * @since 09/01/2017
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ReflectUtils {
    private static Object getField(Object target, String arg) throws Exception {
        if (arg.contains(".")) {
            throw new IllegalArgumentException("属性名称不能包含：.");
        }
        Class<?> clazz = target.getClass();
        Field field = clazz.getDeclaredField(arg);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        return field.get(target);
    }

    public static Method reflectMethod(Class<?> clazz, String methodName
            , Class<?>... parameterTypes) throws Exception {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass == null) {
                throw e;
            }
            return reflectMethod(superclass, methodName);
        }
    }

    public static Field reflectField(Class<?> clazz, String fieldName) throws Exception {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass == null) {
                throw e;
            }
            return reflectField(superclass, fieldName);
        }
    }

    private static boolean isMethod(String block) {
        return block.contains("(") && block.endsWith(")");
    }


    /**
     * 辅助工具，用来更简单的通过反射，像写脚本一样更清晰容易的调用和处理Java类。
     * 处理方式为，把script用.分隔成一组block，然后按从左到右的顺序一个个处理，直到结束或者某个block异常退出，
     * 没有回滚机制，不支持嵌套（例如不支持：setName(getName())），支持调用静态方法，
     * 但注意静态方法调用需要用$符号分割目标类，参考示例写法。
     * <p>
     * 调用示例：
     * <p>
     * ReflectUtils.reflect(application, "mLoadedApk.mActivityThread.mActivities");
     * <p>
     * ReflectUtils.reflect(application, "mModel.getName().toString()");
     * <p>
     * ReflectUtils.reflect(null, "android.app.ActivityThread$currentApplication()");
     * <p>
     *
     * @param target 需要反射的对象
     * @param script 脚本
     * @return 最终结果。注意，如果脚本最后一个block是属性，无论是否需要更新新属性，这里都会返回反射到的对象。
     * 如果最后一个block是方法，这里会返回方法的处理结果。
     * @throws Exception 反射有可能发生任何未知的异常，所以这里强制要求处理异常。
     */
    public static Object reflect(Object target, String script) throws Exception {
        return reflect(target, script, null);
    }

    /**
     * 辅助工具，用来更简单的通过反射，像写脚本一样更清晰容易的调用和处理Java类。
     * 处理方式为，把script用.分隔成一组block，然后按从左到右的顺序一个个处理，直到结束或者某个block异常退出，
     * 没有回滚机制，不支持嵌套（例如不支持：setName(getName())），支持调用静态方法，
     * 但注意静态方法调用需要用$符号分割目标类，参考示例写法。
     * <p>
     * 调用示例：
     * <p>
     * ReflectUtils.reflect(application, "mLoadedApk.mActivityThread.mActivities");
     * <p>
     * ReflectUtils.reflect(application, "mModel.getName().toString()");
     * <p>
     * ReflectUtils.reflect(application, "mModel.setName(%1)", new Object[]{"new_name"});
     * <p>
     * ReflectUtils.reflect(null, "android.app.ActivityThread$currentApplication()");
     * <p>
     *
     * @param target 需要反射的对象
     * @param script 脚本
     * @param args   实际的参数对象，注意这里会根据args来自动生成参数对象的类类型。
     *               但是一定注意这里的参数类型必须跟实际反射的方法的参数类型对应，不能是参数类型的子类。
     *               例如有一个方法为：setContext(Context context)，args这里如果传了一个Context子类，
     *               比如Activity这里就会出错，因为实际需要反射setContext(Context context)方法，
     *               但这里会被错误识别为反射setContext(Activity activity)方法。这时候请调用
     *               {@link #reflect(Object, String, Object[], Class[])}显示指定参数的真实类型。
     * @return 最终结果。注意，如果脚本最后一个block是属性，无论是否需要更新新属性，这里都会返回反射到的对象。
     * 如果最后一个block是方法，这里会返回方法的处理结果。
     * @throws Exception 反射有可能发生任何未知的异常，所以这里强制要求处理异常。
     */
    public static Object reflect(Object target, String script, Object[] args) throws Exception {
        Class[] classes = null;
        if (args != null) {
            classes = new Class[args.length];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = args[i].getClass();
            }
        }
        return reflect(target, script, args, classes);
    }

    /**
     * 辅助工具，用来更简单的通过反射，像写脚本一样更清晰容易的调用和处理Java类。
     * 处理方式为，把script用.分隔成一组block，然后按从左到右的顺序一个个处理，直到结束或者某个block异常退出，
     * 没有回滚机制，不支持嵌套（例如不支持：setName(getName())），支持调用静态方法，
     * 但注意静态方法调用需要用$符号分割目标类，参考示例写法。
     * <p>
     * 调用示例：
     * <p>
     * ReflectUtils.reflect(application, "mModel.setName(%1).value, new Object[]{"new_name"}, newValue);
     * <p>
     *
     * @param target   需要反射的对象
     * @param script   脚本
     * @param args     实际的参数对象，注意这里会根据args来自动生成参数对象的类类型。
     *                 但是一定注意这里的参数类型必须跟实际反射的方法的参数类型对应，不能是参数类型的子类。
     *                 例如有一个方法为：setContext(Context context)，args这里如果传了一个Context子类，
     *                 比如Activity这里就会出错，因为实际需要反射setContext(Context context)方法，
     *                 但这里会被错误识别为反射setContext(Activity activity)方法。这时候请调用
     *                 {@link #reflect(Object, String, Object[], Class[], Object)}显示指定参数的真实类型。
     * @param newValue 如果脚本最后一个block是属性，则会尝试把这个对象赋给反射到的对象
     * @return 最终结果。注意，如果脚本最后一个block是属性，无论是否需要更新新属性，这里都会返回反射到的对象。
     * 如果最后一个block是方法，这里会返回方法的处理结果。
     * @throws Exception 反射有可能发生任何未知的异常，所以这里强制要求处理异常。
     */
    public static Object reflect(Object target, String script, Object[] args
            , Object newValue) throws Exception {
        script = script.trim();
        Class[] classes = null;
        if (args != null) {
            classes = new Class[args.length];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = args[i].getClass();
            }
        }
        return reflect(target, script, args, classes, newValue, true);
    }

    private static String getMethodName(String block) {
        // 当block是method的时候block可能是以下几种类型
        // method()
        // method(%1)
        // method(%1,%2)
        return block.substring(0, block.indexOf('('));
    }

    /**
     * 辅助工具，用来更简单的通过反射，像写脚本一样更清晰容易的调用和处理Java类。
     * 处理方式为，把script用.分隔成一组block，然后按从左到右的顺序一个个处理，直到结束或者某个block异常退出，
     * 没有回滚机制，不支持嵌套（例如不支持：setName(getName())），支持调用静态方法，
     * 但注意静态方法调用需要用$符号分割目标类，参考示例写法。
     * <p>
     * 调用示例：
     * <p>
     * ReflectUtils.reflect(application, "mModel.setName(%1), new Object[]{"new_name"}, new Class[]{String.class});
     * <p>
     *
     * @param target  需要反射的对象
     * @param script  脚本
     * @param args    实际的参数对象
     * @param classes 参数对象的类类型
     * @return 最终结果。注意，如果脚本最后一个block是属性，无论是否需要更新新属性，这里都会返回反射到的对象。
     * 如果最后一个block是方法，这里会返回方法的处理结果。
     * @throws Exception 反射有可能发生任何未知的异常，所以这里强制要求处理异常。
     */
    public static Object reflect(Object target, String script, final Object[] args
            , final Class[] classes) throws Exception {
        return reflect(target, script, args, classes, null, false);
    }

    /**
     * 辅助工具，用来更简单的通过反射，像写脚本一样更清晰容易的调用和处理Java类。
     * 处理方式为，把script用.分隔成一组block，然后按从左到右的顺序一个个处理，直到结束或者某个block异常退出，
     * 没有回滚机制，不支持嵌套（例如不支持：setName(getName())），支持调用静态方法，
     * 但注意静态方法调用需要用$符号分割目标类，参考示例写法。
     * <p>
     * 调用示例：
     * <p>
     * ReflectUtils.reflect(application, "mModel.setName(%1).value, new Object[]{"new_name"}, new Class[]{String.class}, newValue);
     * <p>
     *
     * @param target   需要反射的对象
     * @param script   脚本
     * @param args     实际的参数对象
     * @param classes  参数对象的类类型
     * @param newValue 如果脚本最后一个block是属性，则会尝试把这个对象赋给反射到的对象，否则会忽略这个参数
     * @return 最终结果。注意，如果脚本最后一个block是属性，无论是否需要更新新属性，这里都会返回反射到的对象。
     * 如果最后一个block是方法，这里会返回方法的处理结果。
     * @throws Exception 反射有可能发生任何未知的异常，所以这里强制要求处理异常。
     */
    public static Object reflect(Object target, String script, final Object[] args
            , final Class[] classes, final Object newValue) throws Exception {
        return reflect(target, script, args, classes, newValue, true);
    }

    /**
     * 辅助工具，用来更简单的通过反射，像写脚本一样更清晰容易的调用和处理Java类。
     * 处理方式为，把script用.分隔成一组block，然后按从左到右的顺序一个个处理，直到结束或者某个block异常退出，
     * 没有回滚机制，不支持嵌套（例如不支持：setName(getName())），支持调用静态方法，
     * 但注意静态方法调用需要用$符号分割目标类，参考示例写法。
     * <p>
     * 调用示例：
     * <p>
     * ReflectUtils.reflect(application, "mLoadedApk.mActivityThread.mActivities");
     * <p>
     * ReflectUtils.reflect(application, "mModel.getName().toString()");
     * <p>
     * ReflectUtils.reflect(application, "mModel.setName(%1)");
     * <p>
     * ReflectUtils.reflect(null, "android.app.ActivityThread$currentApplication()");
     * <p>
     * ReflectUtils.reflect(application, "mModel.setName(%1).value, new Object[]{"new_name"}, new Class[]{String.class}, newValue, true);
     * <p>
     *
     * @param target      需要反射的对象
     * @param script      脚本
     * @param args        实际的参数对象
     * @param classes     参数对象的类类型
     * @param newValue    如果脚本最后一个block是属性，则会尝试把这个对象赋给反射到的对象
     * @param setNewValue 是否需要设置新对象，这里不能通过newValue是否为null判断是否需要设置新数值，
     *                    因为可能用户就是需要把目标设置为null
     * @return 最终结果。注意，如果脚本最后一个block是属性，无论是否需要更新新属性，这里都会返回反射到的对象。
     * 如果最后一个block是方法，这里会返回方法的处理结果。
     * @throws Exception 反射有可能发生任何未知的异常，所以这里强制要求处理异常。
     */
    public static Object reflect(Object target, String script, final Object[] args
            , final Class[] classes, final Object newValue, boolean setNewValue) throws Exception {
        if (script.startsWith(".")) {
            script = script.substring(1, script.length());
        }

        final int firstPointIndex;
        final String block;
        // 生成新的script
        final String newScript;
        final Class<?> targetClass;
        if (target == null) {
            int classIndex = script.indexOf('$');
            targetClass = Class.forName(script.substring(0, classIndex));
            script = script.substring(classIndex + 1, script.length());
            firstPointIndex = script.indexOf('.');
            newScript = script.substring(firstPointIndex + 1, script.length());
        } else {
            firstPointIndex = script.indexOf('.');
            targetClass = target.getClass();
            newScript = script.substring(firstPointIndex + 1, script.length());
        }

        if (firstPointIndex == -1) {
            // 不包含点，则block=字符串本身
            block = script;
        } else {
            // 拿到第一个需要处理的block
            block = script.substring(0, firstPointIndex);
        }

        if (isMethod(block)) {
            String methodName = getMethodName(block);
            final Class<?>[] parameterTypes;
            final Object[] arguments;

            int leftIndex = block.indexOf('(') + 1;
            int rightIndex = block.indexOf(')');
            if (leftIndex == rightIndex) {
                // 没有参数
                parameterTypes = null;
                arguments = null;
            } else {
                String paramStr = block.substring(leftIndex, rightIndex);
                String[] split = paramStr.split(",");
                parameterTypes = new Class[split.length];
                arguments = new Object[split.length];
                for (int i = 0; i < split.length; i++) {
                    int indexOf = split[i].indexOf('%');
                    String s = "(";
                    for (int j = 0; j < split.length; j++) {
                        if (j != 0) {
                            s += ",%" + (j + 1);
                        } else {
                            s += "%" + (j + 1);
                        }
                    }
                    s += ")";
                    if (indexOf == -1) {
                        throw new IllegalArgumentException("参数必须用%定义，参考写法：" + methodName
                                + s + "。请检查脚本中的：" + script);
                    }
                    String substring = split[i].substring(indexOf + 1
                            , split[i].length());
                    int index;
                    try {
                        index = Integer.parseInt(substring) - 1;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("参数解析错误，" + substring
                                + "无法转换为数字，参考写法：" + methodName
                                + s + "。请检查脚本中的：" + script);
                    }
                    parameterTypes[i] = classes[index];
                    arguments[i] = args[index];
                }
            }

            try {
                Method method = reflectMethod(targetClass, methodName, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                Object invoke = method.invoke(target, arguments);
                if (firstPointIndex == -1) {
                    return invoke;
                }
                return reflect(invoke, newScript, args, classes, newValue);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法在类：" + targetClass
                        + "及其父类，找到方法：" + methodName
                        + "。请检查脚本中的：" + script);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("在类：" + targetClass
                        + "执行方法：" + methodName
                        + "发生异常。请检查脚本中的：" + script);
            }
        } else {
            Field field;
            try {
                field = reflectField(targetClass, block);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("无法在类：" + targetClass
                        + "及其父类，找到属性：" + block
                        + "。请检查脚本中的：" + script);
            }
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            Object o = field.get(target);
            if (firstPointIndex == -1) {
                if (setNewValue) {
                    field.set(target, newValue);
                }
                return o;
            }
            return reflect(o, newScript, args, classes);
        }
    }

    public static Object get(Object target, String arg) throws Exception {
        int firstPointIndex = arg.indexOf('.');
        final String fieldName;
        if (firstPointIndex != -1) {
            fieldName = arg.substring(0, firstPointIndex);
        } else {
            fieldName = arg;
            return getField(target, fieldName);
        }
        Class<?> clazz = target.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object result = field.get(target);
        return get(result, arg.substring(firstPointIndex + 1, arg.length()));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("属性名称不能包含：.");
        }
        Class<?> clazz = target.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        field.set(target, value);
    }

    public static void set(Object target, String arg, Object value) throws Exception {
        int firstIndexOf = arg.indexOf('.');
        if (firstIndexOf != -1) {
            String substring = arg.substring(0, firstIndexOf);
            Class<?> clazz = target.getClass();
            Field field = clazz.getDeclaredField(substring);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            Object result = field.get(target);
            set(result, arg.substring(firstIndexOf + 1, arg.length()), value);
        } else {
            setField(target, arg, value);
        }
    }
//
//    public static class ReflectException extends Exception {
//
//        private static final long serialVersionUID = -2504804697196582566L;
//
//        /**
//         * Constructs a <code>ReflectException</code> without a detail message.
//         */
//        public ReflectException() {
//            super();
//        }
//
//        /**
//         * Constructs a <code>ReflectException</code> with a detail message.
//         *
//         * @param s the detail message.
//         */
//        public ReflectException(String s) {
//            super(s);
//        }
//    }
}