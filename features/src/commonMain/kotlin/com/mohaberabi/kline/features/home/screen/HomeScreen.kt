package com.mohaberabi.kline.features.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohaberabi.kline.features.home.viewmodel.HomeActions
import com.mohaberabi.kline.features.home.viewmodel.HomeState
import com.mohaberabi.kline.features.home.viewmodel.HomeViewModel
import com.mohaberabi.kline.features.home.viewmodel.PrintJobContent
import com.mohaberabi.kline.features.info.ui.InfoSheet
import com.mohaberabi.kline.server.DEFAULT_PORT
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreenBody(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun HomeScreenBody(
    state: HomeState,
    onAction: (HomeActions) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.receivedContent) {
        if (state.receivedContent.isNotEmpty()) {
            listState.animateScrollToItem(state.receivedContent.lastIndex)
        }
    }
    Scaffold(
        containerColor = Color.LightGray.copy(alpha = .5f),
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                modifier = Modifier.shadow(12.dp)
            ) {

                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Spacer(Modifier.width(8.dp))
                    PrinterStatusBox(
                        onClick = { onAction(HomeActions.ToggleInfoSheet) },
                        icon = Icons.Default.Info,
                        color = Color.Black
                    )
                    Spacer(Modifier.width(8.dp))
                    PrinterStatusBox(
                        onClick = { onAction(HomeActions.OnToggleServer) },
                        color = if (state.serverRunning) Color.Green else Color.Red,
                        icon = if (state.serverRunning) Icons.Default.Start else Icons.Default.Stop
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .heightIn(min = 50.dp)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        onClick = {
                            onAction(HomeActions.OnTestPrint)
                        }
                    ) {
                        Text("Print Test", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(36.dp)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                items(state.receivedContent) { content ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxHeight()
                            .width(500.dp)
                            .padding(top = 12.dp)
                            .padding(horizontal = 8.dp)
                            .border(1.dp, Color.Gray)
                    ) {
                        when (content) {
                            PrintJobContent.None -> Unit
                            is PrintJobContent.PLainText -> Text(
                                content.text,
                                modifier = Modifier.align(Alignment.Center).padding(4.dp)
                            )

                            is PrintJobContent.RasterImage -> Image(
                                content.bitmap,
                                "",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.padding(4.dp)
                            )
                        }

                    }

                }

            }
        }
        if (state.showInfoSheet) {
            InfoSheet(
                ipAddress = state.ipAddress,
                port = DEFAULT_PORT,
                onDismiss = { onAction(HomeActions.ToggleInfoSheet) }
            )
        }
    }

}