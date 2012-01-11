package za.co.quirk.authorise;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

/**
 * Copied from Grails plugin spring-security-acl
 *
 * CGLIB proxies confuse parameter name discovery since the classes aren't compiled with
 * debug, so find the corresponding method or constructor in the target and use that.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class ProxyAwareParameterNameDiscoverer extends ProxyAwareLocalVariableTableParameterNameDiscoverer 
{
	/**
	 * {@inheritDoc}
	 * @see org.springframework.core.LocalVariableTableParameterNameDiscoverer#getParameterNames(
	 * 	java.lang.reflect.Method)
	 */
	@Override
	public String[] getParameterNames( final Method method ) 
	{
		return super.getParameterNames( ProxyUtils.unproxy( method ) );
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.core.LocalVariableTableParameterNameDiscoverer#getParameterNames(
	 * 	java.lang.reflect.Constructor)
	 */
	@Override
	public String[] getParameterNames( @SuppressWarnings("rawtypes") final Constructor constructor ) 
	{
		return super.getParameterNames( ProxyUtils.unproxy( constructor ) );
	}
}