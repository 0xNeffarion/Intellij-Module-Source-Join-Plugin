package com.github.xneffarion.intellijmodulesourcejoinplugin.services

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class JoinModuleSourcesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.project
        if (project == null) {
            Messages.showErrorDialog("No project found.", "Error")
            return
        }

        val baseDir = project.projectFile?.parent?.parent!!;

        ApplicationManager.getApplication().runWriteAction {

            var joinedDir = baseDir.findChild("joined");
            if (joinedDir?.exists()!!){
                Files.walk(joinedDir.toNioPath())
                        .sorted(Comparator.reverseOrder())
                        .forEach { path ->
                            Files.delete(path)
                        }
            }else{
                joinedDir = baseDir.findOrCreateDirectory("joined");
            }

            for (module in ModuleManager.getInstance(project).modules) {
                val moduleDir = module.guessModuleDir()!!
                val src = moduleDir.findChild("src")!!
                copyFolder(src, joinedDir)
            }

            LocalFileSystem.getInstance().refresh(false)
        }

        Messages.showInfoMessage("Module sources joined.", "Success")
    }

    private fun copyFolder(source: VirtualFile, destination: VirtualFile) {
        try {
            Files.walk(source.toNioPath()).use { stream ->
                stream.forEach { sourcePath ->
                    val sourcePathIo = source.toNioPath();
                    val destinationPathIo = destination.toNioPath();

                    val relativePath = sourcePathIo.relativize(sourcePath)
                    val destinationPath = destinationPathIo.resolve(relativePath)

                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(destinationPath)
                    } else {
                        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        } catch (e: IOException) {
            println("An error occurred: ${e.message}")
        }
    }
}