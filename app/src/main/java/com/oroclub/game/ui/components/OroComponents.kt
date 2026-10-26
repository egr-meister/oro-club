package com.oroclub.game.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.oroclub.game.R
import com.oroclub.game.audio.SoundManager
import com.oroclub.game.ui.Assets
import com.oroclub.game.ui.theme.DeepRoyal
import com.oroclub.game.ui.theme.Gem
import com.oroclub.game.ui.theme.Gold
import com.oroclub.game.ui.theme.GoldDark
import com.oroclub.game.ui.theme.GoldSoft
import com.oroclub.game.ui.theme.Ink
import com.oroclub.game.ui.theme.NearBlack
import com.oroclub.game.ui.theme.Panel
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.theme.TextPrimary

/** Minimum interactive size mandated by the accessibility guidelines. */
val MinTouchTarget: Dp = 48.dp

/**
 * Invokes [onLeaveForeground] when the hosting screen stops. Timed screens use
 * it so a phone call or a trip to the launcher pauses the round instead of
 * running the clock down unattended.
 */
@Composable
fun PauseWhenBackgrounded(onLeaveForeground: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentCallback by rememberUpdatedState(onLeaveForeground)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) currentCallback()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

/** Full-bleed themed background: the artwork plus a legibility scrim. */
@Composable
fun OroBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(Ink)) {
        Image(
            painter = painterResource(Assets.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x8801081A),
                            Color(0x4401081A),
                            Color(0xCC01081A)
                        )
                    )
                )
        )
        content()
    }
}

/**
 * Standard screen frame: background, safe-area insets, an optional gold title
 * and optional back / pause controls.
 */
@Composable
fun OroScreen(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    OroBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            if (onBack != null || onPause != null || title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconPlateButton(
                            iconRes = Assets.iconBack,
                            contentDescription = stringResource(R.string.a11y_back),
                            onClick = onBack
                        )
                    } else {
                        Box(Modifier.size(MinTouchTarget))
                    }
                    Box(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (title != null) {
                            GoldTitle(text = title, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                    if (onPause != null) {
                        IconPlateButton(
                            iconRes = Assets.iconPause,
                            contentDescription = stringResource(R.string.a11y_pause),
                            onClick = onPause
                        )
                    } else {
                        Box(Modifier.size(MinTouchTarget))
                    }
                }
            }
            content()
        }
    }
}

/**
 * Content that is vertically centred while it fits and scrolls once it does
 * not — the behaviour every modal and results page needs so that a large system
 * font scale can never clip a button off the screen.
 *
 * `heightIn(min = viewport)` inside the scroll container is what makes both
 * halves of that work.
 */
@Composable
fun CenteredScrollContent(
    modifier: Modifier = Modifier,
    applyInsets: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val frame = if (applyInsets) {
        modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
    } else {
        modifier.fillMaxSize()
    }
    BoxWithConstraints(modifier = frame) {
        val viewport = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewport),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(14.dp))
            content()
            Spacer(Modifier.height(14.dp))
        }
    }
}

/** Gold gradient title text. */
@Composable
fun GoldTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    maxLines: Int = 2
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(brush = Brush.verticalGradient(listOf(GoldSoft, Gold, GoldDark))),
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * A button drawn as an image plate with a WHITE BOLD label composed on top.
 *
 * @param plateRes [Assets.menuPlate] for the ornate main-menu plate,
 *   [Assets.buttonPlate] for the simple in-panel plate.
 * @param aspect width / height of that plate.
 */
@Composable
fun PlateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes plateRes: Int = Assets.buttonPlate,
    aspect: Float = Assets.PLATE_ASPECT,
    enabled: Boolean = true,
    labelScale: Float = 0.082f,
    contentDescription: String? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val safeAspect = if (aspect.isFinite() && aspect > 0f) aspect else 3f
    val label = contentDescription ?: text
    BoxWithConstraints(
        modifier = modifier
            .sizeIn(minWidth = MinTouchTarget, minHeight = MinTouchTarget)
            .aspectRatio(safeAspect)
            .clip(RoundedCornerShape(percent = 16))
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Gold),
                enabled = enabled,
                role = Role.Button
            ) {
                SoundManager.click()
                onClick()
            }
            .semantics { this.contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        val fontSize = (maxWidth.value * labelScale).coerceIn(13f, 23f).sp
        Image(
            painter = painterResource(plateRes),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize().alpha(if (enabled) 1f else 0.45f)
        )
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextMuted,
            fontSize = fontSize,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(0.74f).clearAndSetSemantics { }
        )
    }
}

/** The ornate main-menu plate; callers constrain its width (~0.7 of screen). */
@Composable
fun MenuPlateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    PlateButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        plateRes = Assets.menuPlate,
        aspect = Assets.MENU_PLATE_ASPECT,
        enabled = enabled,
        labelScale = 0.076f
    )
}

/** Round icon button (back / pause) built on the supplied round icon art. */
@Composable
fun IconPlateButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(if (size < MinTouchTarget) MinTouchTarget else size)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = false, color = Gold),
                role = Role.Button
            ) {
                SoundManager.click()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Modal panel DRAWN IN COMPOSE — rounded rectangle, gradient fill, gold border.
 * Decorative frame art is never stretched to fit variable content.
 */
@Composable
fun OroPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(DeepRoyal, Panel, DeepRoyal)))
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(listOf(GoldSoft, Gold, GoldDark)),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

/** Thin gold divider used inside panels. */
@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .height(1.dp)
            .background(Brush.horizontalGradient(listOf(Color.Transparent, Gold, Color.Transparent)))
    )
}

/** Progress bar with a gold fill. A numeric label always accompanies it. */
@Composable
fun OroProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fill: Brush = Brush.horizontalGradient(listOf(GoldDark, Gold, GoldSoft))
) {
    val clamped = if (progress.isFinite()) progress.coerceIn(0f, 1f) else 0f
    Box(
        modifier = modifier
            .height(12.dp)
            .clip(CircleShape)
            .background(NearBlack)
            .border(1.dp, GoldDark, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped)
                .clip(CircleShape)
                .background(fill)
        )
    }
}

/**
 * Indeterminate loading bar: a gold segment sweeping the track.
 *
 * The startup gate can sit on the splash for several seconds while attribution and the
 * routing probe resolve. Without a moving element that reads as a frozen app, and people
 * kill it before it ever gets to a screen.
 */
@Composable
fun LoadingSweep(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "loading")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    BoxWithConstraints(
        modifier = modifier
            .height(6.dp)
            .clip(CircleShape)
            .background(NearBlack)
            .border(1.dp, GoldDark, CircleShape)
    ) {
        val segment = maxWidth * 0.34f
        // Starts fully off the left edge and ends fully off the right one, so the segment
        // never appears or vanishes mid-track.
        val travel = maxWidth + segment
        Box(
            modifier = Modifier
                .offset(x = -segment + travel * progress)
                .width(segment)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldDark, Gold, GoldSoft, Gold, GoldDark, Color.Transparent)
                    )
                )
        )
    }
}

/** Countdown bar tinted with the gem accent. */
@Composable
fun TimerBar(progress: Float, modifier: Modifier = Modifier) {
    OroProgressBar(
        progress = progress,
        modifier = modifier,
        fill = Brush.horizontalGradient(listOf(Gem, GoldSoft, Gold))
    )
}

/** Round bubble built on `plate_round`, with content composed on top. */
@Composable
fun RoundPlate(
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val interaction = remember { MutableInteractionSource() }
    var shaped: Modifier = modifier
        .sizeIn(minWidth = MinTouchTarget, minHeight = MinTouchTarget)
        .clip(CircleShape)
    if (onClick != null) {
        shaped = shaped.clickable(
            interactionSource = interaction,
            indication = ripple(color = Gold),
            role = Role.Button
        ) {
            SoundManager.click()
            onClick()
        }
    }
    if (contentDescription != null) {
        val label = contentDescription
        // clearAndSetSemantics: the bubble announces itself once, instead of the
        // caller's description plus every glyph drawn inside it.
        shaped = shaped.clearAndSetSemantics { this.contentDescription = label }
    }
    Box(modifier = shaped, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Assets.roundPlate),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().alpha(if (dimmed) 0.42f else 1f)
        )
        content()
    }
}

/** Body copy in the muted text colour. */
@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextMuted,
    align: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = align
    )
}

/** Bold white copy, e.g. the score line on a results panel. */
@Composable
fun StrongText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = TextPrimary
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = TextAlign.Center
    )
}

/**
 * Fallback shown when a destination receives arguments it cannot honour
 * (deleted progress, a bad deep link, a level that no longer exists).
 */
@Composable
fun FallbackScreen(
    message: String,
    onBack: () -> Unit
) {
    OroScreen(title = stringResource(R.string.app_name), onBack = onBack) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            OroPanel(modifier = Modifier.fillMaxWidth(0.92f)) {
                GoldTitle(text = stringResource(R.string.fallback_nothing), style = MaterialTheme.typography.headlineSmall)
                GoldDivider()
                BodyText(text = message)
                PlateButton(
                    text = stringResource(R.string.btn_back),
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(0.72f)
                )
            }
        }
    }
}
