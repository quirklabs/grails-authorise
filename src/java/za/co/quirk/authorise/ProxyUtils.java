package za.co.quirk.authorise;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Copied from Grails plugin spring-security-acl 
 *
 * Utility methods for unproxying transactional services.
 * 
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class ProxyUtils
{

  private ProxyUtils()
  {
    // static only
  }

  /**
   * Finds the unproxied superclass if proxied.
   * 
   * @param clazz the potentially proxied class
   * @return the unproxied class
   */
  public static Class<?> unproxy( final Class<?> clazz )
  {
    Class<?> current = clazz;
    
    while( ClassUtils.isCglibProxyClass( current ) )
    {
      current = current.getSuperclass();
    }
    return current;
  }

  /**
   * Finds the method in the unproxied superclass if proxied.
   * 
   * @param method the method
   * @return the method in the unproxied class
   */
  public static Method unproxy( final Method method )
  {
    Class<?> clazz = method.getDeclaringClass();

    if( !ClassUtils.isCglibProxyClass( clazz ) )
    {
      return method;
    }

    return ReflectionUtils.findMethod( unproxy( clazz ), method.getName(), method.getParameterTypes() );
  }

  /**
   * Finds the constructor in the unproxied superclass if proxied.
   * 
   * @param constructor the constructor
   * @return the constructor in the unproxied class
   */
  public static Constructor<?> unproxy( final Constructor<?> constructor )
  {
    Class<?> clazz = constructor.getDeclaringClass();

    if( !ClassUtils.isCglibProxyClass( clazz ) )
    {
      return constructor;
    }

    Class<?> searchType = unproxy( clazz );
    while( searchType != null )
    {
      for( Constructor<?> c : searchType.getConstructors() )
      {
        if( constructor.getName().equals( c.getName() )
            && ( constructor.getParameterTypes() == null || Arrays.equals( constructor.getParameterTypes(), c.getParameterTypes() ) ) )
        {
          return c;
        }
      }
      searchType = searchType.getSuperclass();
    }

    return null;
  }
}