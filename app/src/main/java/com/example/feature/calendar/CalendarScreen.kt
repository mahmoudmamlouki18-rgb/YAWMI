package com.example.feature.calendar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.feature.common.FoundationDestinationPlaceholder

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
  FoundationDestinationPlaceholder(
    title = stringResource(R.string.calendar_title),
    description = stringResource(R.string.calendar_foundation_desc),
    icon = Icons.Outlined.CalendarMonth,
    modifier = modifier.testTag("screen_calendar"),
  )
}
