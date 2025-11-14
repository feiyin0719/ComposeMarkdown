package com.iffly.compose.markdown.samples

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.ActionHandler
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.vladsch.flexmark.util.ast.Node

@Composable
fun ImageAndMediaExample(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        MarkdownView(
            content = """
                # Image and Media Example
                
                This example demonstrates image rendering functionality.
                
                ## Network Images
                
                ![Compose Logo](https://raw.githubusercontent.com/feiyin0719/AFreeSvg/dev/test.jpg)
                
                ## Image Description
                
                Above is the official Android robot image, showcasing network image loading capabilities.
                
                
                ## Images Mixed with Text
                
                Text content can be nicely mixed with images. ![Small Icon](https://qcloud.dpfile.com/pc/w7BUcqbwgbqmYoDIbJcYkS-4p5gNsX7g5bXVyqeC386xoJR2wB3zvXKeaGZtgX19.jpg) Such small icons can be embedded within text.
                
                ## Multiple Images
                
                ![Image 1](https://photo.tuchong.com/24937277/f/725032695.jpg)
                
                ![Image 2](https://qcloud.dpfile.com/pc/w7BUcqbwgbqmYoDIbJcYkS-4p5gNsX7g5bXVyqeC386xoJR2wB3zvXKeaGZtgX19.jpg)
                
                
                The image above is clickable and will navigate to the Android developer website.
            """.trimIndent(),
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            modifier = Modifier.padding(16.dp),
            actionHandler = object : ActionHandler {
                override fun handleUrlClick(url: String, node: Node) {
                    Log.d("BasicSyntax", "Clicked link: $url")

                }
            }
        )
    }
}
