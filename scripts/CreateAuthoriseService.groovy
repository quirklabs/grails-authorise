includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsCreateArtifacts")

target ('default': "Creates the authorisation service and configures it for use with the Authorise plugin") {
    depends(checkVersion, parseArguments)

    ant.mkdir(dir:"${basedir}/grails-app/services")

    def type = "Service"
    promptForName(type: type)

    for (name in argsMap["params"]) {
        name = purgeRedundantArtifactSuffix(name, type)
        createArtifact(name: name, suffix: type, type: type, path: "grails-app/services")
        createUnitTest(name: name, suffix: type)
    }
    
    def configFile = new File("${basedir}/grails-app/conf/Config.groovy")
	if (configFile.exists()) {
		configFile.withWriterAppend {
			it.writeLine '\n// Added by the Authorise plugin:'
			for (name in argsMap["params"]) {
			    it.writeLine "grails.plugins.authorise.authoriseService = ${name}${type}"
			}
		}
	}
}
