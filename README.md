Authorise Plugin for Grails
===========================

This plugin provides a framework for authorising actions in Controllers, methods in Services and visible elements in GSPs. 
No authentication or domain models are provided so you are free to use your own or another plugins'.

The only configuration necessary is to specify the service (authoriseService) that will contain the methods used to authorise actions, by specifying in Config.groovy

```
grails.plugins.authorise.authoriseService = 'xyz.AuthoriseService'
```

You can create and configure this Service automatically by runing the CreateAuthoriseService script e.g. 
```
> grails create-authorise-service xyz.Authorise
```

Authorisation is done by annotating an action in a controller or a method in a service with the @Authorise('authoriseMethod()') annotation. Strings can be passed in too e.g.

```groovy
@Authorise("hasRole('ADMIN')")
def showAdmin() {
    ...
}
```

will call on the authoriseService class, e.g.

```groovy
def hasRole(String role) {
    return adminRoles.contains(role)
}
```

Note the authorise method on the authoriseService is called before the annotated method, and should return true or false.
Note that on Service methods, the arguments passed to the method can be used in the annotation e.g.

```groovy
@Authorise("canEdit(project)")
def editProject(Project project) {
    ...
}
```

If the authorise method as annotated on a controller or service fails, an exception of type com.quirklabs.authorise.AuthorisationDeniedException will be thrown.
A good way to handle this is to create an ErrorsController with actions mapped in UrlMappings.groovy to send this exception and other errors to an appropriate GSP page e.g.

```groovy
import com.quirklabs.authorise.AuthorisationDeniedException

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
```

In GSPs, the authoriseService artifact is automatically added to the model and can be accessed with other available variables in the page scope e.g.

```html
<g:if test="${authoriseService.canEdit(project)}">
    Edit project here
</g:if>
```
