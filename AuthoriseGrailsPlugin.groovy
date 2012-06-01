import com.quirklabs.authorise.*
import grails.util.GrailsNameUtils
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator

/**
 * @author <a href='mailto:craig@quirk.biz'>Craig Raw</a>
 */
class AuthoriseGrailsPlugin {
    def version = "1.2"
    def grailsVersion = "2.0 > *"
    def dependsOn = [:]
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]
    def title = "Authorise Plugin"
    def author = "Craig Raw"
    def authorEmail = "craig@quirk.biz"
    def description = "This plugin provides a framework for authorising actions in Controllers, methods in Services and visible elements in GSPs. No authentication or domain models are provided so you are free to use your own or another plugins'."
    def documentation = "https://github.com/quirklabs/grails-authorise/blob/master/README.md"
    def license = "APACHE"
    def organization = [ name: "Quirk Labs", url: "http://www.quirklabs.co.za" ]
    def issueManagement = [ system: "github", url: "https://github.com/quirklabs/grails-authorise/issues" ]
    def scm = [ url: "https://github.com/quirklabs/grails-authorise" ]

    def watchedResources = ["file:./grails-app/controllers/**/*Controller.groovy","file:./grails-app/services/**/*Service.groovy"]

    def doWithSpring = {
        def authoriseServiceClass = application.config.grails.plugins.authorise.authoriseService
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
        AuthoriseAnnotationHelper.init(application)
    }
    
    def onChange = { event ->
        AuthoriseAnnotationHelper.reset(application)
        
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
