package com.ncfgroup.dbstat.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.log4j.Logger;


public final class ReflectionUtil
{
  private static Logger logger = Logger.getLogger(ReflectionUtil.class);
  
  public static Object getInstance(String classname){
    try {
      Class<?> clazz = Class.forName(classname);
      return clazz.newInstance();
    } catch (Exception e) {
      logger.error("根据类名:【"+classname+"】获取对象实例出错", e);
    }
    return null;
  }

  /**
   * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数.
   */
  public static Object getFieldValue(final Object object, final String fieldName)
  {
    Field field = getDeclaredField(object, fieldName);

    if (field == null)
      throw new IllegalArgumentException("Could not find field [" + fieldName
          + "] on target [" + object + "]");

    makeAccessible(field);

    Object result = null;
    try {
      result = field.get(object);
    } catch (IllegalAccessException e) {
      logger.error("不可能抛出的异常{}", e);
    }
    return result;
  }

  /**
   * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
   */
  public static void setFieldValue(final Object object, final String fieldName,
      final Object value)
  {
    Field field = getDeclaredField(object, fieldName);

    if (field == null)
      throw new IllegalArgumentException("Could not find field [" + fieldName
          + "] on target [" + object + "]");

    makeAccessible(field);

    try {
      field.set(object, value);
    } catch (IllegalAccessException e) {
      logger.error("不可能抛出的异常:{}", e);
    }
  }

  /**
   * 直接调用对象方法,无视private/protected修饰符.
   */
  public static Object invokeMethod(final Object object,
      final String methodName, final Class<?>[] parameterTypes,
      final Object[] parameters) throws InvocationTargetException
  {
    Method method = getDeclaredMethod(object, methodName, parameterTypes);
    if (method == null)
      throw new IllegalArgumentException("Could not find method [" + methodName
          + "] on target [" + object + "]");

    method.setAccessible(true);

    try {
      return method.invoke(object, parameters);
    } catch (IllegalAccessException e) {
      logger.error("不可能抛出的异常:{}", e);
    }

    return null;
  }

  /**
   * 循环向上转型,获取对象的DeclaredField.
   */
  protected static Field getDeclaredField(final Object object,
      final String fieldName)
  {
    for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
        .getSuperclass()) {
      try {
        return superClass.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        // Field不在当前类定义,继续向上转型
      }
    }
    return null;
  }

  /**
   * 循环向上转型,获取对象的DeclaredField.
   */
  protected static void makeAccessible(final Field field)
  {
    if (!Modifier.isPublic(field.getModifiers())
        || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
      field.setAccessible(true);
    }
  }

  /**
   * 循环向上转型,获取对象的DeclaredMethod.
   */
  protected static Method getDeclaredMethod(Object object, String methodName,
      Class<?>[] parameterTypes)
  {
    for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
        .getSuperclass()) {
      try {
        return superClass.getDeclaredMethod(methodName, parameterTypes);
      } catch (NoSuchMethodException e) {
        // Method不在当前类定义,继续向上转型
      }
    }
    return null;
  }

  /**
   * 通过反射,获得Class定义中声明的父类的泛型参数的类型. eg. public UserDao extends HibernateDao<User>
   * 
   * @param clazz
   *          - The class to introspect
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getSuperClassGenricType(final Class<?> clazz)
  {
    return getSuperClassGenricType(clazz, 0);
  }

  /**
   * 通过反射,获得定义Class时声明的父类的泛型参数的类型.
   * 
   * 如public UserDao extends HibernateDao<User,Long>
   * 
   * @param clazz
   *          - the class to introspect
   * @param index
   *          - the Index of the generic ddeclaration,start from 0.
   */
  public static Class getSuperClassGenricType(final Class<?> clazz, final int index)
  {

    Type genType = clazz.getGenericSuperclass();

    if (!(genType instanceof ParameterizedType)) {
      logger
          .warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
      return Object.class;
    }

    Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

    if (index >= params.length || index < 0) {
      logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName()
          + "'s Parameterized Type: " + params.length);
      return Object.class;
    }
    if (!(params[index] instanceof Class)) {
      logger.warn(clazz.getSimpleName()
          + " not set the actual class on superclass generic parameter");
      return Object.class;
    }
    return (Class) params[index];
  }

  /**
   * 将反射时的checked exception转换为unchecked exception。
   */
  public static IllegalArgumentException convertToUncheckedException(Exception e)
      throws IllegalArgumentException
  {
    if (e instanceof IllegalAccessException
        || e instanceof IllegalArgumentException
        || e instanceof NoSuchMethodException)
      throw new IllegalArgumentException("Refelction Exception.", e);
    else
      throw new IllegalArgumentException(e);
  }
}
