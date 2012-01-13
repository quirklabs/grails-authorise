package za.co.quirklabs.authorise;

import java.util.Arrays;
import java.util.Collection;

import org.codehaus.groovy.grails.compiler.GrailsClassLoader;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;

/**
 * Borrowed from https://github.com/alkemist/grails-aop-reloading-fix/blob/master/src/groovy/grails/plugin/aopreloadingfix/ClassLoaderPerProxyGroovyAwareAspectJAwareAdvisorAutoProxyCreator.groovy
 *
 * @author Luke Daley
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class ClassLoaderPerProxyBeanNameAutoProxyCreator extends BeanNameAutoProxyCreator
{

  private static final long serialVersionUID = 1;

  private ClassLoader baseLoader;
  private Collection<String> beanNames;

  @Override
  public void setBeanClassLoader( ClassLoader classLoader )
  {
    super.setBeanClassLoader( classLoader );
    baseLoader = classLoader;
  }

  @Override
  protected Object createProxy( Class<?> beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource )
  {
    setProxyClassLoader( new GrailsClassLoader( baseLoader, null, null ) );
    Object proxy = super.createProxy( beanClass, beanName, specificInterceptors, targetSource );
    setProxyClassLoader( baseLoader );
    return proxy;
  }

  @Override
  protected Object getCacheKey( Class<?> beanClass, String beanName )
  {
    return beanClass.hashCode() + "_" + beanName;
  }

  @Override
  protected boolean shouldProxyTargetClass( Class<?> beanClass, String beanName )
  {
    return beanNames.contains( beanName );
  }

  @Override
  public void setBeanNames( String[] names )
  {
    super.setBeanNames( names );
    beanNames = Arrays.asList( names );
  }
}