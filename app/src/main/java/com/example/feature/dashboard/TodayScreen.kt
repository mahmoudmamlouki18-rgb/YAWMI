package com.example.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.preferences.AppLanguage
import com.example.core.time.GreetingPeriod
import com.example.core.time.formatLocalizedHeader
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalYawmiSemanticColors
import java.time.LocalDate

@Composable
fun TodayScreen(
  currentDate: LocalDate,
  greetingPeriod: GreetingPeriod,
  language: AppLanguage,
  onQuickAddClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val greetingText =
    when (greetingPeriod) {
      GreetingPeriod.MORNING -> stringResource(R.string.greeting_morning)
      GreetingPeriod.AFTERNOON -> stringResource(R.string.greeting_afternoon)
      GreetingPeriod.EVENING -> stringResource(R.string.greeting_evening)
    }

  val semantic = LocalYawmiSemanticColors.current

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      item {
        Column(
          modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small,
          ) {
            Text(
              text = stringResource(R.string.foundation_badge),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = currentDate.formatLocalizedHeader(language),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("today_date_label"),
          )
          Text(
            text = greetingText,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("today_greeting_label"),
          )
          Text(
            text = stringResource(R.string.today_summary_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      item {
        DailyProgressCard(
          completionRatio = 0f,
          modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp),
        )
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MetricChipCard(
            label = stringResource(R.string.metric_total),
            value = "0",
            accentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
          )
          MetricChipCard(
            label = stringResource(R.string.metric_completed),
            value = "0",
            accentColor = semantic.success,
            modifier = Modifier.weight(1f),
          )
          MetricChipCard(
            label = stringResource(R.string.metric_remaining),
            value = "0",
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
          )
          MetricChipCard(
            label = stringResource(R.string.metric_overdue),
            value = "0",
            accentColor = semantic.overdue,
            modifier = Modifier.weight(1f),
          )
        }
      }

      item {
        EmptySectionCard(
          title = stringResource(R.string.section_priorities),
          description = stringResource(R.string.section_priorities_empty),
          icon = Icons.Outlined.Flag,
          modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp).testTag("today_priorities_card"),
        )
      }

      item {
        EmptySectionCard(
          title = stringResource(R.string.section_upcoming),
          description = stringResource(R.string.section_upcoming_empty),
          icon = Icons.Outlined.EventAvailable,
          modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp).testTag("today_upcoming_card"),
        )
      }
    }

    FloatingActionButton(
      onClick = onQuickAddClick,
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier =
        Modifier.align(Alignment.BottomEnd)
          .padding(20.dp)
          .testTag("fab_quick_add"),
    ) {
      Icon(
        imageVector = Icons.Rounded.Add,
        contentDescription = stringResource(R.string.fab_quick_add_desc),
      )
    }
  }
}

@Composable
private fun DailyProgressCard(completionRatio: Float, modifier: Modifier = Modifier) {
  val semantic = LocalYawmiSemanticColors.current
  val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
  val activeColor = MaterialTheme.colorScheme.primary

  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, semantic.borderSubtle),
    shape = MaterialTheme.shapes.large,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = stringResource(R.string.today_progress_title),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = stringResource(R.string.today_progress_empty),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val strokeWidth = 8.dp.toPx()
          drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
          )
          if (completionRatio > 0f) {
            drawArc(
              color = activeColor,
              startAngle = -90f,
              sweepAngle = 360f * completionRatio.coerceIn(0f, 1f),
              useCenter = false,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
          }
        }
        Text(
          text = "${(completionRatio * 100).toInt()}%",
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

@Composable
private fun MetricChipCard(
  label: String,
  value: String,
  accentColor: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier,
) {
  val semantic = LocalYawmiSemanticColors.current
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, semantic.borderSubtle),
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = value,
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = accentColor,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun EmptySectionCard(
  title: String,
  description: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
) {
  val semantic = LocalYawmiSemanticColors.current
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, semantic.borderSubtle),
    shape = MaterialTheme.shapes.large,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
