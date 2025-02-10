package ru.lashnev.forwarderbackend.utils

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import org.springframework.stereotype.Component

@Component
class TextUtils {
    fun removeMarkdown(text: String): String {
        val options = MutableDataSet()
        val parser = Parser.builder(options).build()
        val renderer = HtmlRenderer.builder(options).build()

        val document: Node = parser.parse(text)

        return renderer.render(document).replace(Regex("<.*?>"), "")
    }
}