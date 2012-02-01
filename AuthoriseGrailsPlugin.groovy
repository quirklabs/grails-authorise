import za.co.quirklabs.authorise.*
import grails.util.GrailsNameUtils
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

/**
 * @author <a href='mailto:craig@quirk.biz'>Craig Raw</a>
 */
class AuthoriseGrailsPlugin {
    // the plugin version
    def version = "1.11"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Authorise Plugin" // Headline display name of the plugin
    def author = "Craig Raw"
    def authorEmail = "craig@quirk.biz"
    def description = '''\
This plugin provides a framework for authorising actions in Controllers, methods in Services and visible elements in GSPs. 
No authentication or domain models are provided so you are free to use your own or another plugins'. 

The only configuration necessary is to specify the service (authoriseService) that will contain the methods used to authorise actions, by specifying in Config.groovy
grails.plugins.authorise.authoriseService = 'xyz.AuthoriseService'

You can create and configure this Service automatically by runing the CreateAuthoriseService script e.g. 
> grails create-authorise-service xyz.Authorise

Authorisation is done by annotating each action in controllers or each method in services with the @Authorise('authoriseMethod()') annotation. Strings can be passed in too e.g.

@Authorise("hasRole('ADMIN')")
def showAdmin() {
    ...
}

will call on the authoriseService class, e.g.
def hasRole(String role) {
    return adminRoles.contains(role)
}

Note the authorise method on the authoriseService is called before the annotated method, and should return true or false.
Note that on Service methods, the arguments passed to the method can be used in the annotation e.g.

@Authorise("canEdit(project)")
def editProject(Project project) {
    ...
}

If the authorise method as annotated on a controller or service fails, an exception of type za.co.quirklabs.authorise.AuthorisationDeniedException will be thrown.
A good way to handle this is to create an ErrorsController with actions mapped in UrlMappings.groovy to send this exception and other errors to an appropriate GSP page e.g.

import za.co.quirklabs.authorise.AuthorisationDeniedException

class UrlMappings {
	static mappings = {
        .....

        "/"(view:"/index")
        "403"(controller: "errors", action: "error403")
        "404"(controller: "errors", action: "error404")
        "500"(controller: "errors", action: "error500")
        "500"(controller: "errors", action: "error403", exception: AuthorisationDeniedException)
	}
}

In GSPs, the authoriseService is automatically added to the model and can be accessed with other available variables in the page scope e.g.
<g:if test="${authoriseService.canEdit(project)}">
	Edit project here
</g:if>

'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/authorise"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Quirk", url: "http://www.quirk.biz/" ]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.grails-plugins.codehaus.org/browse/grails-plugins/" ]

    def watchedResources = ["file:./grails-app/controllers/**/*Controller.groovy","file:./grails-app/services/**/*Service.groovy"]

    def doWithSpring = {
        def authoriseServiceClass = AH.application.config.grails.plugins.authorise.authoriseService
        if (!authoriseServiceClass) {
            throw new RuntimeException("grails.plugins.authorise.authoriseService must be configured to the fully qualified name of the service that will contain the authorisation methods")
        }
        
        def serviceClassBeanName = GrailsNameUtils.getPropertyNameRepresentation(authoriseServiceClass.name)
        
        def serviceNames = []
        for (serviceClass in application.serviceClasses) {
        	if (serviceIsAnnotated(serviceClass.clazz, Authorise)) {
        		serviceNames << GrailsNameUtils.getPropertyNameRepresentation(serviceClass.clazz.name)
        	}
        }
        
        parameterNameDiscoverer(ProxyAwareParameterNameDiscoverer)
        
        if (serviceNames) {
            authorisedServicesInterceptor(ClassLoaderPerProxyBeanNameAutoProxyCreator) {
        		proxyTargetClass = true
        		beanNames = serviceNames
        		interceptorNames = ['methodAuthoriseInterceptor']
        	}
        	
        	methodAuthoriseInterceptor(MethodAuthoriseInterceptor) { 
        	    authoriseService = ref(serviceClassBeanName)
        	    parameterNameDiscoverer = ref('parameterNameDiscoverer')
        	}
        }
        
        springConfig.addAlias('authoriseService', serviceClassBeanName) 
    }
    
    def doWithApplicationContext = { appCtx ->
        AuthoriseAnnotationHelper.init()
    }
    
    def onChange = { event ->
        AuthoriseAnnotationHelper.reset()
        
        ProxyAwareParameterNameDiscoverer nameDiscoverer = event.ctx.getBean("parameterNameDiscoverer")
        nameDiscoverer.clearCaches()
    }

    private boolean serviceIsAnnotated(Class clazz, Class annotation) {
    	if (clazz.isAnnotationPresent(annotation)) {
    		return true
    	}

    	for (java.lang.reflect.Method method in clazz.methods) {
    		if (method.isAnnotationPresent(annotation)) {
    			return true
    		}
    	}

    	false
    }
}
