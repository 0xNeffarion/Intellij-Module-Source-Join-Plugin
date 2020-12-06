package com.github.xneffarion.intellijmodulesourcejoinplugin.services

import com.intellij.ide.actions.SaveAllAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.CopyOption
import java.nio.file.Files


class JoinModuleSourcesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        SaveAllAction().actionPerformed(e)
        val project: Project = e.project!!
        val folderName = "joined"
        val projectDir = project.guessProjectDir() ?: return

        val joinDir = projectDir.path + "/" + folderName
        val joinDirFile = File(joinDir)
        if (!joinDirFile.exists()) {
            Files.createDirectory(joinDirFile.toPath())
        } else {
            deleteDir(joinDirFile)
            Files.createDirectory(joinDirFile.toPath())
        }

        for (module in ModuleManager.getInstance(project).modules) {
            val moduleFile = module.moduleFilePath
            val index = moduleFile.indexOfLast { c -> c == '/' }
            val dir = moduleFile.substring(0, index + 1) + "src"
            copyFolder(File(dir), joinDirFile)
        }

        LocalFileSystem.getInstance().refresh(true)
    }

    @Throws(IOException::class)
    fun copyFileOrFolder(source: File, dest: File, vararg options: CopyOption?) {
        if (source.isDirectory) {
            copyFolder(source, dest, *options)
        } else {
            ensureParentFolder(dest)
            copyFile(source, dest, *options)
        }
    }

    @Throws(IOException::class)
    private fun copyFolder(source: File, dest: File, vararg options: CopyOption?) {
        if (!dest.exists()) dest.mkdirs()
        val contents = source.listFiles()
        if (contents != null) {
            for (f in contents) {
                val newFile = File(dest.absolutePath + File.separator + f.name)
                if (f.isDirectory) copyFolder(f, newFile, *options) else copyFile(f, newFile, *options)
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFile(source: File, dest: File, vararg options: CopyOption?) {
        Files.copy(source.toPath(), dest.toPath(), *options)
    }

    private fun ensureParentFolder(file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) parent.mkdirs()
    }

    @Throws(IOException::class)
    fun deleteDir(f: File) {
        if (f.isDirectory) {
            for (c in f.listFiles()) deleteDir(c)
        }
        if (!f.delete()) throw FileNotFoundException("Failed to delete file: $f")
    }

}