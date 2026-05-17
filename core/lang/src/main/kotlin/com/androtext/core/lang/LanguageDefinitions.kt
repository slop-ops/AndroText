package com.androtext.core.lang

import android.content.Context
import android.content.res.AssetManager

data class GrammarDefinition(
    val name: String,
    val scopeName: String,
    val grammarPath: String,
    val extensions: Set<String>,
    val languageConfigurationPath: String? = null,
)

object LanguageDefinitions {

    private val definitions = listOf(
        GrammarDefinition(
            name = "json",
            scopeName = "source.json",
            grammarPath = "textmate/grammars/json.tmLanguage.json",
            extensions = setOf("json"),
        ),
        GrammarDefinition(
            name = "html",
            scopeName = "text.html.basic",
            grammarPath = "textmate/grammars/html.tmLanguage.json",
            extensions = setOf("html", "htm", "svg"),
        ),
        GrammarDefinition(
            name = "css",
            scopeName = "source.css",
            grammarPath = "textmate/grammars/css.tmLanguage.json",
            extensions = setOf("css"),
        ),
        GrammarDefinition(
            name = "javascript",
            scopeName = "source.js",
            grammarPath = "textmate/grammars/javascript.tmLanguage.json",
            extensions = setOf("js", "mjs", "cjs"),
        ),
        GrammarDefinition(
            name = "typescript",
            scopeName = "source.ts",
            grammarPath = "textmate/grammars/typescript.tmLanguage.json",
            extensions = setOf("ts", "tsx"),
        ),
        GrammarDefinition(
            name = "java",
            scopeName = "source.java",
            grammarPath = "textmate/grammars/java.tmLanguage.json",
            extensions = setOf("java"),
        ),
        GrammarDefinition(
            name = "kotlin",
            scopeName = "source.kotlin",
            grammarPath = "textmate/grammars/kotlin.tmLanguage",
            extensions = setOf("kt", "kts"),
        ),
        GrammarDefinition(
            name = "c",
            scopeName = "source.c",
            grammarPath = "textmate/grammars/c.tmLanguage.json",
            extensions = setOf("c", "h"),
        ),
        GrammarDefinition(
            name = "cpp",
            scopeName = "source.cpp",
            grammarPath = "textmate/grammars/cpp.tmLanguage.json",
            extensions = setOf("cpp", "hpp", "cc", "cxx", "hxx"),
        ),
        GrammarDefinition(
            name = "python",
            scopeName = "source.python",
            grammarPath = "textmate/grammars/python.tmLanguage.json",
            extensions = setOf("py", "pyw"),
        ),
        GrammarDefinition(
            name = "go",
            scopeName = "source.go",
            grammarPath = "textmate/grammars/go.tmLanguage.json",
            extensions = setOf("go"),
        ),
        GrammarDefinition(
            name = "rust",
            scopeName = "source.rust",
            grammarPath = "textmate/grammars/rust.tmLanguage.json",
            extensions = setOf("rs"),
        ),
        GrammarDefinition(
            name = "php",
            scopeName = "source.php",
            grammarPath = "textmate/grammars/php.tmLanguage.json",
            extensions = setOf("php", "phtml"),
        ),
        GrammarDefinition(
            name = "ruby",
            scopeName = "source.ruby",
            grammarPath = "textmate/grammars/ruby.tmLanguage",
            extensions = setOf("rb", "erb", "rake", "gemspec"),
        ),
        GrammarDefinition(
            name = "swift",
            scopeName = "source.swift",
            grammarPath = "textmate/grammars/swift.tmLanguage",
            extensions = setOf("swift"),
        ),
        GrammarDefinition(
            name = "csharp",
            scopeName = "source.cs",
            grammarPath = "textmate/grammars/csharp.tmLanguage",
            extensions = setOf("cs"),
        ),
        GrammarDefinition(
            name = "dart",
            scopeName = "source.dart",
            grammarPath = "textmate/grammars/dart.tmLanguage.json",
            extensions = setOf("dart"),
        ),
        GrammarDefinition(
            name = "shellscript",
            scopeName = "source.shell",
            grammarPath = "textmate/grammars/shellscript.tmLanguage.json",
            extensions = setOf("sh", "bash", "zsh", "fish"),
        ),
        GrammarDefinition(
            name = "sql",
            scopeName = "source.sql",
            grammarPath = "textmate/grammars/sql.tmLanguage",
            extensions = setOf("sql"),
        ),
        GrammarDefinition(
            name = "xml",
            scopeName = "text.xml",
            grammarPath = "textmate/grammars/xml.tmLanguage.json",
            extensions = setOf("xml", "xsd", "xsl", "xslt", "svg"),
        ),
        GrammarDefinition(
            name = "yaml",
            scopeName = "source.yaml",
            grammarPath = "textmate/grammars/yaml.tmLanguage.json",
            extensions = setOf("yaml", "yml"),
        ),
        GrammarDefinition(
            name = "toml",
            scopeName = "source.toml",
            grammarPath = "textmate/grammars/toml.tmLanguage",
            extensions = setOf("toml"),
        ),
        GrammarDefinition(
            name = "markdown",
            scopeName = "text.html.markdown",
            grammarPath = "textmate/grammars/markdown.tmLanguage.json",
            extensions = setOf("md", "markdown", "mdown", "mkd"),
        ),
        GrammarDefinition(
            name = "diff",
            scopeName = "source.diff",
            grammarPath = "textmate/grammars/diff.tmLanguage.json",
            extensions = setOf("diff", "patch"),
        ),
        GrammarDefinition(
            name = "lua",
            scopeName = "source.lua",
            grammarPath = "textmate/grammars/lua.tmLanguage",
            extensions = setOf("lua"),
        ),
        GrammarDefinition(
            name = "r",
            scopeName = "source.r",
            grammarPath = "textmate/grammars/r.tmLanguage",
            extensions = setOf("r", "R"),
        ),
        GrammarDefinition(
            name = "groovy",
            scopeName = "source.groovy",
            grammarPath = "textmate/grammars/groovy.tmLanguage",
            extensions = setOf("groovy", "gradle"),
        ),
        GrammarDefinition(
            name = "latex",
            scopeName = "text.tex.latex",
            grammarPath = "textmate/grammars/latex.tmLanguage",
            extensions = setOf("tex", "latex", "lhs"),
        ),
    )

    fun getAll(): List<GrammarDefinition> = definitions
}
