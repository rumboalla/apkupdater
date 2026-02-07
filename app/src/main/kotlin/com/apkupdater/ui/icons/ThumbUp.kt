package com.apkupdater.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ThumbUpFilled: ImageVector
    get() {
        if (_ThumbUpFilled != null) return _ThumbUpFilled!!
        _ThumbUpFilled = ImageVector.Builder(
            name = "ThumbUpFilled",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(1f, 21f)
                horizontalLineToRelative(4f)
                verticalLineTo(9f)
                horizontalLineTo(1f)
                verticalLineToRelative(12f)
                close()
                moveTo(23f, 10f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-6.31f)
                lineToRelative(0.95f, -4.57f)
                lineToRelative(0.03f, -0.32f)
                curveToRelative(0f, -0.41f, -0.17f, -0.79f, -0.44f, -1.06f)
                lineTo(14.17f, 1f)
                lineTo(7.59f, 7.59f)
                curveTo(7.22f, 7.95f, 7f, 8.45f, 7f, 9f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(9f)
                curveToRelative(0.83f, 0f, 1.54f, -0.5f, 1.84f, -1.22f)
                lineToRelative(3.02f, -7.05f)
                curveToRelative(0.09f, -0.23f, 0.14f, -0.47f, 0.14f, -0.73f)
                verticalLineToRelative(-1.91f)
                lineToRelative(-0.01f, -0.01f)
                lineTo(23f, 10f)
                close()
            }
        }.build()
        return _ThumbUpFilled!!
    }

private var _ThumbUpFilled: ImageVector? = null

val ThumbUpOutlined: ImageVector
    get() {
        if (_ThumbUpOutlined != null) return _ThumbUpOutlined!!
        _ThumbUpOutlined = ImageVector.Builder(
            name = "ThumbUpOutlined",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        ).apply {
             path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
             ) {
                 moveTo(9f, 21f)
                 horizontalLineToRelative(9f)
                 curveToRelative(0.83f, 0f, 1.54f, -0.5f, 1.84f, -1.22f)
                 lineToRelative(3.02f, -7.05f)
                 curveToRelative(0.09f, -0.23f, 0.14f, -0.47f, 0.14f, -0.73f)
                 verticalLineToRelative(-2f)
                 curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                 horizontalLineToRelative(-6.31f)
                 lineToRelative(0.95f, -4.57f)
                 lineToRelative(0.03f, -0.32f)
                 curveToRelative(0f, -0.41f, -0.17f, -0.79f, -0.44f, -1.06f)
                 lineTo(14.17f, 1f)
                 lineTo(7.58f, 7.59f)
                 curveTo(7.22f, 7.95f, 7f, 8.45f, 7f, 9f)
                 verticalLineToRelative(10f)
                 curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                 close()
                 moveTo(9f, 9f)
                 lineToRelative(4.17f, -4.17f)
                 lineTo(11.96f, 11f)
                 lineToRelative(-0.5f, 2.5f)
                 horizontalLineToRelative(8.99f)
                 verticalLineToRelative(2.17f)
                 lineToRelative(-3.2f, 7.49f)
                 lineToRelative(-0.16f, 0.22f)
                 lineTo(9f, 23.38f)
                 verticalLineTo(9f)
                 close()
                 moveTo(5f, 21f)
                 verticalLineTo(9f)
                 horizontalLineTo(1f)
                 verticalLineToRelative(12f)
                 horizontalLineToRelative(4f)
                 close()
             }
        }.build()
        return _ThumbUpOutlined!!
    }

private var _ThumbUpOutlined: ImageVector? = null
