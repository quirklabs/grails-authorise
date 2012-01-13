package za.co.quirklabs.authorise;

import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.core.ParameterNameDiscoverer;

/**
 * Provides authorisation interception of AOP Alliance based method invocations. 
 *
 * @author <a href='mailto:craig@quirk.biz'>Craig Raw</a>
 */
public class MethodAuthoriseInterceptor implements Advice, Interceptor, MethodInterceptor
{
  private static Log logger = LogFactory.getLog( MethodAuthoriseInterceptor.class );
  
  private Object authoriseService;
  private ParameterNameDiscoverer parameterNameDiscoverer;
  
  public Object invoke( MethodInvocation invocation ) throws Throwable
  {
    Object result = null;
    Object beforeResult = before( invocation );

    try 
    {
      result = invocation.proceed();
    } 
    finally 
    {
      result = after( beforeResult, result );
    }

    return result;
  }
  
  protected Object before( MethodInvocation invocation ) throws Throwable
  {
    Method method = invocation.getMethod();
    Authorise annotation = method.getAnnotation( Authorise.class );
    
    System.out.println( method );
    System.out.println( annotation );
    System.out.println( Arrays.asList( invocation.getArguments() ) );
    
    if( annotation != null )
    {
      String expr = annotation.value();
      
      Method serviceMethod = null;
      List<Object> paramValues = new ArrayList<Object>();
      
      Pattern pattern = Pattern.compile( "(\\w+)\\((.*)\\)" );
      Matcher matcher = pattern.matcher( expr );
      if( matcher.matches() )
      {
        String targetName = matcher.group( 1 );
        
        List<Class> paramClasses = new ArrayList<Class>();
        
        String variables = matcher.group( 2 ).trim();
        if( variables.length() > 0 )
        {
          String[] vars = variables.split( "," );
          String[] names = getParameterNameDiscoverer().getParameterNames( method );
          Class[] types = method.getParameterTypes();
          Object[] values = invocation.getArguments();
          
          for( int i = 0; i < vars.length; i++ )
          {
            String var = vars[i].trim();
            for( int j = 0; j < names.length; j++ )
            {
               if( var.equals( names[j] ) )
               {
                 paramClasses.add( types[j] );
                 paramValues.add( values[j] );
               }
            }
          }
          
          if( vars.length != paramClasses.size() )
          {
            throw new IllegalArgumentException( "Parameters in " + expr + " do not match provided parameters in method " + method );
          }
        }
        
        Class[] targetParameters = (Class[])paramClasses.toArray( new Class[paramClasses.size()] );
        serviceMethod = authoriseService.getClass().getMethod( targetName, targetParameters );
      }
      else
      {
        serviceMethod = authoriseService.getClass().getMethod( expr );
      }
      
      if( serviceMethod == null )
      {
        throw new NoSuchMethodException( "No method " + expr + " on " + authoriseService.getClass() );
      }
            
      Object result = serviceMethod.invoke( authoriseService, paramValues.toArray() );
      logger.debug( "AOP interception of " + method.getName() + " for authorisation method " + serviceMethod + " returned " + result );
      
      if( !(result instanceof Boolean) || !((Boolean)result) )
      {
        throw new AuthorisationDeniedException( "Authorisation for method " + method.getName() + " denied by " + serviceMethod );
      }
      
      return result;
    }
    
    return null;
  }
  
  protected Object after( Object beforeResult, Object result )
  {
    return result;
  }
  
  public Object getAuthoriseService()
  {
    return authoriseService;
  }
  
  public void setAuthoriseService( Object authoriseService )
  {
    this.authoriseService = authoriseService;
  }
  
  public ParameterNameDiscoverer getParameterNameDiscoverer()
  {
    return parameterNameDiscoverer;
  }
  
  public void setParameterNameDiscoverer( ParameterNameDiscoverer parameterNameDiscoverer )
  {
    this.parameterNameDiscoverer = parameterNameDiscoverer;
  }
}