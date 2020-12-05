package com.github.0xneffarion.intellijmodulesourcejoinplugin.services

import com.intellij.openapi.project.Project
import com.github.0xneffarion.intellijmodulesourcejoinplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
