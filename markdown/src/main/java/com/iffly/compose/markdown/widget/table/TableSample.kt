/*
 * Copyright (c) 2025.
 * Microsoft Corporation. All rights reserved.
 */

package com.iffly.compose.markdown.widget.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 表格组件的示例用法，展示DSL语法和Modifier支持
 */
@Composable
fun TableSample() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(state = rememberScrollState())
    ) {
        val twoCellModifier = Modifier.fillMaxSize()
        Text(
            text = "基础表格示例",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 使用统一内边距的DSL示例
        Table(
            modifier = Modifier.fillMaxWidth(),
            border = TableBorder.solid(
                mode = TableBorderMode.ALL,
                color = Color.Gray,
                width = 1.dp
            ),
            widthWeights = listOf(1f, 2f),
            cellAlignment = Alignment.TopStart
        ) {
            header {
                cell(
                    modifier = twoCellModifier.background(Color.Red),
                    cellBackground = Modifier.background(Color.Green)
                ) { Text("名称", fontWeight = FontWeight.Bold) }
                cell(modifier = twoCellModifier) { Text("数量", fontWeight = FontWeight.Bold) }
            }
            body {
                row {
                    cell(modifier = twoCellModifier) { Text("项目1ljkhjkhjkjkhjknkjnjkhkjhjkhj") }
                    cell(modifier = twoCellModifier) { Text("10") }
                }
                row {
                    cell(modifier = twoCellModifier) { Text("项目2") }
                    cell(modifier = twoCellModifier) { Text("20") }
                }
            }
        }

        Text(
            text = "带样式的表格示例",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        val cellModifier = Modifier
            .wrapContentSize()
            .widthIn(max = 170.dp)

        // 使用Modifier自定义样式的示例
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Table(
                cellPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
                cellAlignment = Alignment.Center,
                border = TableBorder.solid(
                    mode = TableBorderMode.ALL,
                    color = Color.Gray,
                    width = 1.dp
                )
            ) {
                header(
                    modifier = Modifier.background(Color.LightGray)
                ) {
                    cell(
                        modifier = cellModifier
                            .padding(12.dp)
                    ) {
                        Text(
                            "产品",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    cell(
                        modifier = cellModifier
                            .padding(12.dp)
                    ) {
                        Text(
                            "价格",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    cell(
                        modifier = cellModifier
                            .padding(12.dp)
                    ) {
                        Text(
                            "状态",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                body {
                    row {
                        cell(
                            modifier = cellModifier
                                .padding(12.dp)
                        ) {
                            Text("MacBook Pro")
                        }
                        cell(
                            modifier = cellModifier
                                .padding(12.dp)
                        ) {
                            Text("¥18,999", color = Color.Blue)
                        }
                        cell(
                            modifier = cellModifier
                                .padding(12.dp)
                                .background(Color.Green.copy(alpha = 0.2f))
                        ) {
                            Text("有库存", color = Color.Green)
                        }
                    }
                    row(
                        modifier = Modifier.background(Color.Gray.copy(alpha = 0.1f))
                    ) {
                        cell(
                            modifier = cellModifier
                                .padding(12.dp)
                        ) {
                            Text("iPhone 15fdsfdsfdsfdsfdsfdsfdsfsdfdsfsdsdfdsfdsfdsfdsfdsfsd")
                        }
                        cell(
                            modifier = cellModifier
                                .padding(12.dp)
                        ) {
                            Text(
                                "¥7,999dasdasdasdasdasdasdasdasdasdasdasdasdasddasdasdasdasdsadasda",
                                color = Color.Blue
                            )
                        }
                        cell(
                            modifier = cellModifier
                                .padding(12.dp)
                                .background(Color.Green.copy(alpha = 0.2f))
                        ) {
                            Text("数量有限", color = Color.Green)
                        }
                    }
                }
            }
        }


        Text(
            text = "简洁边框样式表格",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        // 简洁样式的表格
        Table(
            cellPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            header {
                cell(
                    modifier = Modifier.border(
                        width = 0.dp,
                        color = Color.Transparent
                    )
                ) {
                    Text("任务", fontWeight = FontWeight.Bold)
                }
                cell { Text("优先级", fontWeight = FontWeight.Bold) }
                cell { Text("截止日期", fontWeight = FontWeight.Bold) }
            }
            body {
                row {
                    cell { Text("设计评审") }
                    cell(
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = 0.1f))
                            .padding(4.dp)
                    ) {
                        Text("高", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    cell { Text("今天") }
                }
                row {
                    cell { Text("代码文档") }
                    cell(
                        modifier = Modifier
                            .background(Color.Yellow.copy(alpha = 0.2f))
                            .padding(4.dp)
                    ) {
                        Text("中", color = Color.Green)
                    }
                    cell { Text("明天") }
                }
                row {
                    cell { Text("单元测试") }
                    cell(
                        modifier = Modifier
                            .padding(4.dp),
                        cellBackground = Modifier
                            .background(Color.Green.copy(alpha = 0.1f))
                    ) {
                        Text("低", color = Color.Green)
                    }
                    cell { Text("下周") }
                }
            }
        }
        Text(
            text = "简洁边框样式表格",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )
        // 展示更多对齐方式
        Table(
            cellPadding = PaddingValues(16.dp),
            modifier = Modifier.border(1.dp, Color.Gray)
        ) {
            header {
                cell(
                    alignment = Alignment.TopStart,
                    modifier = Modifier
                        .background(Color.Blue.copy(alpha = 0.1f))

                ) {
                    Text("左上对齐", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                cell(
                    alignment = Alignment.TopCenter,
                    modifier = Modifier
                        .background(Color.Blue.copy(alpha = 0.1f))

                ) {
                    Text("顶部居中", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                cell(
                    alignment = Alignment.TopEnd,
                    modifier = Modifier
                        .background(Color.Blue.copy(alpha = 0.1f))

                ) {
                    Text("右上对齐", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            body {
                row {
                    cell(
                        alignment = Alignment.CenterStart,
                        modifier = Modifier
                    ) {
                        Text("左侧居中")
                    }
                    cell(
                        alignment = Alignment.Center,
                        modifier = Modifier
                    ) {
                        Text("完全居中")
                    }
                    cell(
                        alignment = Alignment.CenterEnd,
                        modifier = Modifier
                    ) {
                        Text("右侧居中")
                    }
                }
                row {
                    cell(
                        alignment = Alignment.BottomStart,
                        modifier = Modifier
                    ) {
                        Text("左下对齐", fontSize = 12.sp, color = Color.Gray)
                    }
                    cell(
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier
                    ) {
                        Text("底部居中", fontSize = 12.sp, color = Color.Gray)
                    }
                    cell(
                        alignment = Alignment.BottomEnd,
                        modifier = Modifier
                    ) {
                        Text("右下对齐", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TableSamplePreview() {
    MaterialTheme {
        TableSample()
    }
}
