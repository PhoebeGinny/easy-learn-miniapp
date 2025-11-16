package com.example.demo.astrology;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Map;

/**
 * 自定义视图：根据计算得到的行星黄经（0-360°）在圆盘上绘制行星符号与连线（角度关系）
 *
 * 说明：
 * - 我使用简单视觉风格：外圈黄道带（12星座分割），中心绘制行星位置（角度映射到圆周）
 * - 宫位采用等分宫：12等份，从上升点（Ascendant）开始顺时针
 */
public class ChartView extends View {

    private PlanetCalculator.CalcResult result;
    private Paint paintLine;
    private Paint paintText;
    private Paint paintCircle;

    public ChartView(Context context) {
        super(context);
        init();
    }
    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setStrokeWidth(2);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTextSize(28);

        paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(3);
    }

    public void setResult(PlanetCalculator.CalcResult r) {
        this.result = r;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 背景留白
        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h) - 20;
        int cx = w / 2;
        int cy = h / 2;
        int radius = size / 2;

        // 外圈
        paintCircle.setColor(0xFF444444);
        canvas.drawCircle(cx, cy, radius, paintCircle);

        // 绘制12星座分割线
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90); // 0°在正右，减90让0在正上
            float x2 = (float) (cx + Math.cos(angle) * radius);
            float y2 = (float) (cy + Math.sin(angle) * radius);
            paintLine.setColor(0xFFBBBBBB);
            canvas.drawLine(cx, cy, x2, y2, paintLine);

            // 写星座短名（用 ZodiacUtils）
            String name = ZodiacUtils.SYMBOLS[i]; // 简短符号
            float tx = (float) (cx + Math.cos(angle + 0.13) * (radius - 30));
            float ty = (float) (cy + Math.sin(angle + 0.13) * (radius - 30));
            paintText.setColor(0xFF666666);
            canvas.drawText(name, tx - 12, ty, paintText);
        }

        if (result == null) return;

        // 绘制每个行星：将黄经映射到圆周
        Paint pPlanet = new Paint(Paint.ANTI_ALIAS_FLAG);
        pPlanet.setTextSize(28);
        int idx = 0;
        for (Map.Entry<String, Double> e : result.planetLongitudes.entrySet()) {
            String planet = e.getKey();
            double lon = e.getValue(); // 0-360
            double angle = Math.toRadians(lon - 90); // 0 at top
            float x = (float) (cx + Math.cos(angle) * (radius - 60));
            float y = (float) (cy + Math.sin(angle) * (radius - 60));

            // 小圆点
            pPlanet.setColor(0xFFAA3333);
            canvas.drawCircle(x, y, 10, pPlanet);

            // 名称
            pPlanet.setColor(0xFF222222);
            canvas.drawText(planet, x + 12, y + 8, pPlanet);

            idx++;
        }

        // 绘制宫位分隔（等分宫），并在上方标注宫号
        if (result.ascendant != null) {
            double asc = result.ascendant; // 上升点黄经
            // 从 asc 开始，顺时针每 30° 为一宫
            for (int i = 0; i < 12; i++) {
                double ang = asc + i * 30;
                double rad = Math.toRadians(ang - 90);
                float x2 = (float) (cx + Math.cos(rad) * radius);
                float y2 = (float) (cy + Math.sin(rad) * radius);
                paintLine.setColor(0xFF5555AA);
                canvas.drawLine(cx, cy, x2, y2, paintLine);

                // 宫号文本：写在外圈
                float tx = (float) (cx + Math.cos(rad) * (radius + 10));
                float ty = (float) (cy + Math.sin(rad) * (radius + 10));
                paintText.setColor(0xFF333399);
                canvas.drawText(String.valueOf((i + 1)), tx - 8, ty, paintText);
            }
        }
    }
}

