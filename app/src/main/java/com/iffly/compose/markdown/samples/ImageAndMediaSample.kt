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
import com.iffly.compose.markdown.MarkdownView
import com.iffly.compose.markdown.config.MarkdownRenderConfig

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
                
                ![Compose Logo](https://developer.android.com/images/brand/Android_Robot.png)
                
                ## Image Description
                
                Above is the official Android robot image, showcasing network image loading capabilities.
                
                ## Local Image Reference
                
                ![Local Image](file:///android_asset/sample_image.png)
                
                ## Images Mixed with Text
                
                Text content can be nicely mixed with images. ![Small Icon](https://developer.android.com/images/brand/Android_Robot.png) Such small icons can be embedded within text.
                
                ## Multiple Images
                
                ![Image 1](https://developer.android.com/images/brand/Android_Robot.png)
                
                ![Image 2](https://developer.android.com/images/brand/Android_Robot.png)
                
                ## Linked Images
                
                [![Clickable Image](https://developer.android.com/images/brand/Android_Robot.png)](https://developer.android.com)
                
                The image above is clickable and will navigate to the Android developer website.
            """.trimIndent(),
            markdownRenderConfig = MarkdownRenderConfig.Builder().build(),
            modifier = Modifier.padding(16.dp),
            linkInteractionListener = { linkAnnotation ->
                Log.d("ImageAndMedia", "Clicked image link: $linkAnnotation")
            }
        )
    }
}
