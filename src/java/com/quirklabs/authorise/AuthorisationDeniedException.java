package com.quirklabs.authorise;

/**
 * Thrown if authorisation is denied for a method.
 *
 * @author <a href='mailto:craig@quirk.biz'>Craig Raw</a>
 */
public class AuthorisationDeniedException extends RuntimeException 
{
  public AuthorisationDeniedException( String msg ) 
  {
    super( msg );
  }

  public AuthorisationDeniedException( String msg, Throwable t ) 
  {
    super( msg, t );
  }
}