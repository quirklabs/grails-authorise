package com.quirklabs.authorise;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
/**
 * Indicates that a method on a Controller or Service should first call a method on the configured authorise Service to determine whether the method invocation is allowed or not.
 * 
 * @author <a href='mailto:craig@quirk.biz'>Craig Raw</a>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorise 
{
  public String value();
}