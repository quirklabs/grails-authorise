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

Authorisation is done by annotating each method with the @Authorise('authoriseMethod()') annotation. Strings can be passed in too e.g.

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

In GSPs, the authoriseService is automatically added to the model and can be accessed with other available variables e.g.

```html
<g:if test="${authoriseService.canEdit(project)}">
    Edit project here
</g:if>
```
