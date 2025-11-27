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
import kotlinx.collections.immutable.persistentListOf

/**
 * Sample usage of table component, demonstrating DSL syntax and Modifier support
 */
@Composable
fun TableSample(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(state = rememberScrollState()),
    ) {
        val twoCellModifier = Modifier.fillMaxSize()
        Text(
            text = "Basic Table Example",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // DSL example with unified padding
        Table(
            modifier = Modifier.fillMaxWidth(),
            border =
                TableBorder.solid(
                    mode = TableBorderMode.ALL,
                    color = Color.Gray,
                    width = 1.dp,
                ),
            widthWeights = persistentListOf(1f, 2f),
            cellAlignment = Alignment.TopStart,
        ) {
            header {
                cell(
                    modifier = twoCellModifier.background(Color.Red),
                    cellBackground = Modifier.background(Color.Green),
                ) { Text("Name", fontWeight = FontWeight.Bold) }
                cell(modifier = twoCellModifier) { Text("Quantity", fontWeight = FontWeight.Bold) }
            }
            body {
                row {
                    cell(modifier = twoCellModifier) { Text("Item 1ljkhjkhjkjkhjknkjnjkhkjhjkhj") }
                    cell(modifier = twoCellModifier) { Text("10") }
                }
                row {
                    cell(modifier = twoCellModifier) { Text("Item 2") }
                    cell(modifier = twoCellModifier) { Text("20") }
                }
            }
        }

        Text(
            text = "Styled Table Example",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
        )

        val cellModifier =
            Modifier
                .wrapContentSize()
                .widthIn(max = 170.dp)

        // Example using Modifier to customize styles
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
        ) {
            Table(
                cellPadding = PaddingValues(0.dp),
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
                cellAlignment = Alignment.Center,
                border =
                    TableBorder.solid(
                        mode = TableBorderMode.ALL,
                        color = Color.Gray,
                        width = 1.dp,
                    ),
            ) {
                header(
                    modifier = Modifier.background(Color.LightGray),
                ) {
                    cell(
                        modifier =
                            cellModifier
                                .padding(12.dp),
                    ) {
                        Text(
                            "Product",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                    cell(
                        modifier =
                            cellModifier
                                .padding(12.dp),
                    ) {
                        Text(
                            "Price",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                    cell(
                        modifier =
                            cellModifier
                                .padding(12.dp),
                    ) {
                        Text(
                            "Status",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                body {
                    row {
                        cell(
                            modifier =
                                cellModifier
                                    .padding(12.dp),
                        ) {
                            Text("MacBook Pro")
                        }
                        cell(
                            modifier =
                                cellModifier
                                    .padding(12.dp),
                        ) {
                            Text("¥18,999", color = Color.Blue)
                        }
                        cell(
                            modifier =
                                cellModifier
                                    .padding(12.dp)
                                    .background(Color.Green.copy(alpha = 0.2f)),
                        ) {
                            Text("In Stock", color = Color.Green)
                        }
                    }
                    row(
                        modifier = Modifier.background(Color.Gray.copy(alpha = 0.1f)),
                    ) {
                        cell(
                            modifier =
                                cellModifier
                                    .padding(12.dp),
                        ) {
                            Text("iPhone 15fdsfdsfdsfdsfdsfdsfdsfsdfdsfsdsdfdsfdsfdsfdsfdsfsd")
                        }
                        cell(
                            modifier =
                                cellModifier
                                    .padding(12.dp),
                        ) {
                            Text(
                                "¥7,999dasdasdasdasdasdasdasdasdasdasdasdasdasddasdasdasdasdsadasda",
                                color = Color.Blue,
                            )
                        }
                        cell(
                            modifier =
                                cellModifier
                                    .padding(12.dp)
                                    .background(Color.Green.copy(alpha = 0.2f)),
                        ) {
                            Text("Limited Stock", color = Color.Green)
                        }
                    }
                }
            }
        }

        Text(
            text = "Clean Border Style Table",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
        )

        // Clean style table
        Table(
            cellPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            header {
                cell(
                    modifier =
                        Modifier.border(
                            width = 0.dp,
                            color = Color.Transparent,
                        ),
                ) {
                    Text("Task", fontWeight = FontWeight.Bold)
                }
                cell { Text("Priority", fontWeight = FontWeight.Bold) }
                cell { Text("Due Date", fontWeight = FontWeight.Bold) }
            }
            body {
                row {
                    cell { Text("Design Review") }
                    cell(
                        modifier =
                            Modifier
                                .background(Color.Red.copy(alpha = 0.1f))
                                .padding(4.dp),
                    ) {
                        Text("High", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    cell { Text("Today") }
                }
                row {
                    cell { Text("Code Documentation") }
                    cell(
                        modifier =
                            Modifier
                                .background(Color.Yellow.copy(alpha = 0.2f))
                                .padding(4.dp),
                    ) {
                        Text("Medium", color = Color.Green)
                    }
                    cell { Text("Tomorrow") }
                }
                row {
                    cell { Text("Unit Testing") }
                    cell(
                        modifier =
                            Modifier
                                .padding(4.dp),
                        cellBackground =
                            Modifier
                                .background(Color.Green.copy(alpha = 0.1f)),
                    ) {
                        Text("Low", color = Color.Green)
                    }
                    cell { Text("Next Week") }
                }
            }
        }
        Text(
            text = "Clean Border Style Table",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
        )
        // Showcase more alignment options
        Table(
            cellPadding = PaddingValues(16.dp),
            modifier = Modifier.border(1.dp, Color.Gray),
        ) {
            header {
                cell(
                    alignment = Alignment.TopStart,
                    modifier =
                        Modifier
                            .background(Color.Blue.copy(alpha = 0.1f)),
                ) {
                    Text("Top Left", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                cell(
                    alignment = Alignment.TopCenter,
                    modifier =
                        Modifier
                            .background(Color.Blue.copy(alpha = 0.1f)),
                ) {
                    Text("Top Center", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                cell(
                    alignment = Alignment.TopEnd,
                    modifier =
                        Modifier
                            .background(Color.Blue.copy(alpha = 0.1f)),
                ) {
                    Text("Top Right", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            body {
                row {
                    cell(
                        alignment = Alignment.CenterStart,
                        modifier = Modifier,
                    ) {
                        Text("Center Left")
                    }
                    cell(
                        alignment = Alignment.Center,
                        modifier = Modifier,
                    ) {
                        Text("Center")
                    }
                    cell(
                        alignment = Alignment.CenterEnd,
                        modifier = Modifier,
                    ) {
                        Text("Center Right")
                    }
                }
                row {
                    cell(
                        alignment = Alignment.BottomStart,
                        modifier = Modifier,
                    ) {
                        Text("Bottom Left", fontSize = 12.sp, color = Color.Gray)
                    }
                    cell(
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier,
                    ) {
                        Text("Bottom Center", fontSize = 12.sp, color = Color.Gray)
                    }
                    cell(
                        alignment = Alignment.BottomEnd,
                        modifier = Modifier,
                    ) {
                        Text("Bottom Right", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TableSamplePreview() {
    MaterialTheme {
        TableSample()
    }
}
