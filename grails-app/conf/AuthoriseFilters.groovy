import za.co.quirklabs.authorise.*
import java.lang.reflect.Method
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

class AuthoriseFilters {
    def authoriseService
    
    def filters = {
        authorise(controller: '*', action: '*') {
            before = {
                String condition = AuthoriseAnnotationHelper.getAuthoriseCondition(controllerName, actionName)
                if(condition) {
                    try {
                        Method method = null
                        List paramsList = []
                    
                        def matcher = condition =~ /(\w+)\((.*)\)/
                        if( matcher.matches() ) {
                            String name = matcher.group( 1 )
                            String vars = matcher.group( 2 )
                            if( vars ) {
                                List varList = vars.split(",")*.trim()
                                def stringPattern = ~/["'](.*)["']/
                                paramsList = varList.collect { 
                                    def stringMatcher = stringPattern.matcher(it)
                                    if( stringMatcher.matches() ) {
                                        return stringMatcher.group( 1 )
                                    }
                                    else {
                                        throw new IllegalArgumentException("Can only no-arg methods or methods with String parameters from an Authorise annotation on a controller") 
                                    }
                                }
                                
                                Class[] typesArray = paramsList*.class as Class[]
                                method = authoriseService.class.getMethod(name, typesArray)
                            }
                            else {
                                method = authoriseService.class.getMethod(name)
                            }
                        }
                        else {
                            method = authoriseService.class.getMethod(condition)
                        }
                    
                        if(method.invoke(authoriseService, paramsList as String[])) {
                            return true
                        }

                        throw new AuthorisationDeniedException("Authorisation for action " + actionName + " denied by " + method)
                    }
                    catch(NoSuchMethodException e) {
                        throw new IllegalArgumentException("No method ${condition} on ${ProxyUtils.unproxy(authoriseService.class)}")
                    }
                }
                
                return true
            }
        
            after = { model ->
                if (model) {
                    model?.authoriseService = authoriseService
                }
            }
        }
    }
}
        
            