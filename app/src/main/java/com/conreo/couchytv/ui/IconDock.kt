package com.conreo.couchytv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.conreo.couchytv.R
import com.conreo.couchytv.data.AppEntry
import com.conreo.couchytv.data.LauncherConfig

/** Six favorite icons and an explicit app drawer, operated entirely by D-pad. */
@Composable
fun IconDockArea(
    modifier: Modifier,
    apps: List<AppEntry>,
    allApps: List<AppEntry>,
    config: LauncherConfig,
    accent: Color,
    movePkg: String?,
    moveFocus: FocusRequester,
    cardWidth: Dp,
    gap: Dp,
    intro: Boolean,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onLaunch: (AppEntry) -> Unit,
    onMenu: (AppEntry) -> Unit,
) {
    BoxWithConstraints(modifier) {
        // Reserve space for the drawer button even with large UI scaling.
        val iconSize = (cardWidth * 0.44f).coerceIn(48.dp, 64.dp)
        val iconGap = 14.dp
        val favoriteCount = (((maxWidth - 120.dp) / (iconSize + iconGap)).toInt() - 1)
            .coerceIn(0, 6)
        val favorites = apps.take(favoriteCount)
        val cols = fitRow(maxWidth - 108.dp, cardWidth, gap).first.coerceAtMost(6)
        val rows = allApps.chunked(cols)
        val rowWidth = cardWidth * cols + gap * (cols - 1)
        val dockRequests = remember(favorites.map { it.pkg }) {
            List(favorites.size + 1) { FocusRequester() }
        }
        val gridFocus = remember { FocusRequester() }
        var returnIndex by remember { mutableIntStateOf(0) }
        var gridStart by remember { mutableIntStateOf(0) }
        var focusedDock by remember { mutableIntStateOf(-1) }
        val allAppsLabel = stringResource(R.string.cat_all_apps)

        fun openDrawer(index: Int) {
            returnIndex = index
            gridStart = if (index < favorites.size) index.coerceAtMost(cols - 1) else 0
            onExpandChange(true)
        }

        BackHandler(expanded) { onExpandChange(false) }
        LaunchedEffect(expanded, intro, favorites.map { it.pkg }, allApps.isEmpty()) {
            if (!intro || movePkg != null) return@LaunchedEffect
            // Request after layout, retrying a few frames while entrance nodes mount.
            repeat(4) {
                withFrameNanos { }
                val target = if (expanded && allApps.isNotEmpty()) gridFocus
                    else dockRequests[returnIndex.coerceIn(dockRequests.indices)]
                runCatching { target.requestFocus() }
            }
        }

        AnimatedVisibility(
            visible = intro && !expanded,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp),
        ) {
            Row(
                modifier = Modifier
                    .background(RacingColors.surface.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(iconGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                favorites.forEachIndexed { index, app ->
                    Box(Modifier.size(iconSize)) {
                        if (focusedDock == index && config.showAppLabels) DockTooltip(app.label)
                        AppCard(
                            app = app, isMoving = app.pkg == movePkg,
                            isHidden = app.pkg in config.hidden, accent = accent,
                            cardWidth = iconSize, showLabel = false, compactIcon = true,
                            modifier = Modifier
                                .focusRequester(if (app.pkg == movePkg) moveFocus else dockRequests[index])
                                .onFocusChanged {
                                    if (it.isFocused) { focusedDock = index; returnIndex = index }
                                    else if (focusedDock == index) focusedDock = -1
                                }
                                .onPreviewKeyEvent {
                                    if (movePkg == null && it.type == KeyEventType.KeyDown && it.key == Key.DirectionDown) {
                                        openDrawer(index); true
                                    } else false
                                },
                            onLaunch = { onLaunch(app) },
                            onLongPress = { onMenu(app) },
                        )
                    }
                }
                if (favorites.isNotEmpty()) {
                    Box(Modifier.width(1.dp).height(iconSize * 0.65f)
                        .background(Color.White.copy(alpha = 0.2f)))
                }
                Box(Modifier.size(iconSize)) {
                    if (focusedDock == favorites.size) DockTooltip(allAppsLabel)
                    Surface(
                        onClick = { if (movePkg == null) openDrawer(favorites.size) },
                        modifier = Modifier.size(iconSize)
                            .focusRequester(dockRequests.last())
                            .onFocusChanged {
                                if (it.isFocused) { focusedDock = favorites.size; returnIndex = favorites.size }
                                else if (focusedDock == favorites.size) focusedDock = -1
                            },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = RacingColors.onSurface,
                            focusedContentColor = RacingColors.onSurface,
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(BorderStroke(1.25.dp, accent), shape = RoundedCornerShape(12.dp)),
                        ),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(AppIcons.Apps, contentDescription = allAppsLabel, modifier = Modifier.size(iconSize * 0.6f))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically { it / 5 },
            exit = fadeOut() + slideOutVertically { it / 5 },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 40.dp)) {
                Text(allAppsLabel, style = MaterialTheme.typography.headlineSmall,
                    color = RacingColors.onSurface, modifier = Modifier.padding(start = 12.dp, bottom = 24.dp))
                LazyColumn(
                    contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(rows.size, key = { rows[it].first().pkg }) { row ->
                        Row(
                            Modifier.width(rowWidth),
                            horizontalArrangement = Arrangement.spacedBy(gap),
                        ) {
                            rows[row].forEachIndexed { column, app ->
                                AppCard(
                                    app = app, isMoving = app.pkg == movePkg,
                                    isHidden = app.pkg in config.hidden, accent = accent,
                                    cardWidth = cardWidth, showLabel = config.showAppLabels,
                                    modifier = when {
                                        app.pkg == movePkg -> Modifier.focusRequester(moveFocus)
                                        row == 0 && column == gridStart -> Modifier.focusRequester(gridFocus)
                                        else -> Modifier
                                    },
                                    onLaunch = {
                                        if (movePkg == null) {
                                            onExpandChange(false)
                                            onLaunch(app)
                                        }
                                    }, onLongPress = { onMenu(app) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockTooltip(label: String) {
    Box(Modifier.fillMaxWidth().wrapContentSize(unbounded = true).requiredWidth(150.dp).offset(y = (-42).dp), contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = RacingColors.onSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.background(RacingColors.surface.copy(alpha = 0.92f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp))
    }
}
