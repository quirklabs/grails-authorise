package za.co.quirk.authorise

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

/**
 * Helper class to inspect and cache Authorise annotated methods and classes
 *
 * @author <a href='mailto:craig@quirk.biz'>Craig Raw</a>
 */
class AuthoriseAnnotationHelper {
 
  private static Map<String, Map<String, String>> _actionMap = [:]
  private static Map<String, String> _controllerAnnotationMap = [:]
 
  /**
   * Find controller annotation information. Called by BootStrap.init().
   */
  static void init() {
    AH.application.controllerClasses.each { controllerClass ->
      def clazz = controllerClass.clazz
      String controllerName = uncapitalize(controllerClass.name)
      mapClassAnnotation clazz, controllerName
 
      Map<String, String> annotatedMethods = findAnnotatedMethods(clazz, Authorise)
      if (annotatedMethods) {
        _actionMap[controllerName] = annotatedMethods
      }
    }
  }
 
  // for testing
  static void reset() {
    _actionMap.clear()
    _controllerAnnotationMap.clear()
    
    init()
  }
 
  // for testing
  static Map<String, Map<String, List<Class>>> getActionMap() {
    return _actionMap
  }
 
  // for testing
  static Map<String, Class> getControllerAnnotationMap() {
    return _controllerAnnotationMap
  }
 
  private static void mapClassAnnotation(clazz, controllerName) {
    Authorise authorise = clazz.getAnnotation(Authorise)
    if (authorise) {
      _controllerAnnotationMap[controllerName] = authorise.value()
    }
  }
 
  /**
   * Check if the specified controller action requires Ajax.
   * @param controllerName  the controller name
   * @param actionName  the action name (closure name)
   */
  static String getAuthoriseCondition(String controllerName, String actionName) {
      // see if the controller has the annotation
      def condition = _controllerAnnotationMap[controllerName]
      if (condition) {
        return condition
      }

      // otherwise check the action
      Map<String, String> controllerMethodAnnotations = _actionMap[controllerName] ?: [:]
      if(controllerMethodAnnotations && controllerMethodAnnotations[actionName]) {
        return controllerMethodAnnotations[actionName]
      }
  }
 
  private static Map<String, List<Class>> findAnnotatedMethods(Class clazz, Class... annotationClasses) {
    def map = [:]
    for (method in clazz.methods) {
        Authorise authorise = method.getAnnotation(Authorise)
        if(authorise) {
            map[method.name] = authorise.value()
        }
    }
 
    return map
  }
  
  private static String uncapitalize(String str) {
      return str.substring(0,1).toLowerCase() + str.substring(1)
  }
}