package com.conreo.couchytv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.semantics
import com.conreo.couchytv.R
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.conreo.couchytv.data.NetStatus

@Composable
fun StatusBar(
    net: NetStatus,
    time: String,
    date: String,
    showVpn: Boolean,
    glass: Boolean,
    racing: Boolean = false,
    onVpnClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    // Racing mode floats over the wallpaper scrim; the classic glass option
    // remains available when the personal style is switched off.
    val cluster = if (glass && !racing) {
        Modifier
            .clip(SmoothCornerShape(LocalCornerRadius.current))
            .background(Color(0xB3121418))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    } else Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        modifier = cluster,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Order: VPN · network · settings · date · clock (clock far right)
        if (showVpn) {
            StatusIcon(
                icon = AppIcons.Vpn,
                active = net.vpn,
                contentDescription = stringResource(R.string.cd_vpn),
                onClick = onVpnClick,
                racing = racing,
            )
        }
        StatusIcon(
            icon = when {
                net.ethernet -> AppIcons.Ethernet
                net.wifi -> AppIcons.Wifi
                else -> AppIcons.WifiOff
            },
            active = net.connected,
            contentDescription = stringResource(R.string.cd_network),
            onClick = onNetworkClick,
            racing = racing,
        )
        StatusIcon(
            icon = AppIcons.Gear,
            active = true,
            contentDescription = stringResource(R.string.cd_settings),
            onClick = onSettingsClick,
            racing = racing,
        )
        if (racing) {
            RacingClock(time = time, date = date)
        } else {
        if (date.isNotEmpty()) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Text(
            // Tabular figures: digits keep a fixed width so the clock never jitters.
            text = time,
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            color = Color.White,
            modifier = Modifier.padding(start = 10.dp),
        )
        }
      }
    }
}

// Bundled OFL font: no network request and no synthetic slant on the clock.
private val RacingClockFont = FontFamily(
    Font(R.font.barlow_condensed_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

@Composable
private fun RacingClock(time: String, date: String) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 4.dp)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 48.sp, lineHeight = 50.sp,
                fontFamily = RacingClockFont,
                fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic,
                letterSpacing = 0.sp, fontFeatureSettings = "tnum",
                shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, 2f), 8f),
            ),
            color = RacingColors.onSurface,
        )
        Box(Modifier.width(36.dp).height(2.dp).background(RacingColors.secondary))
        if (date.isNotEmpty()) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.5.sp,
                ),
                color = RacingColors.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun StatusIcon(
    icon: ImageVector,
    active: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    racing: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = if (racing) RacingColors.primary else Color.White.copy(alpha = 0.22f),
            contentColor = Color.White,
            focusedContentColor = if (racing) RacingColors.surface else Color.White,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.18f),
    ) {
        Box(Modifier.size(if (racing) 48.dp else 40.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = androidx.tv.material3.LocalContentColor.current.copy(
                    alpha = if (active) 0.95f else 0.35f,
                ),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
