package com.vanta.codm.vision;

import android.graphics.Bitmap;
import android.graphics.Rect;

public final class Detector {

    public static final class Hit {
        public final float x, y;
        public final float score;
        public Hit(float x, float y, float score) {
            this.x = x; this.y = y; this.score = score;
        }
    }

    // CODM enemy nameplate: red-orange. Tuned by observation.
    private static final int RED_MIN   = 175;
    private static final int GREEN_MAX = 110;
    private static final int BLUE_MAX  = 110;
    private static final int MIN_PIXELS_IN_CLUSTER = 6;

    public Hit findTarget(Bitmap frame, Rect fov, float crosshairX, float crosshairY) {
        int x0 = Math.max(0, fov.left);
        int y0 = Math.max(0, fov.top);
        int x1 = Math.min(frame.getWidth()  - 1, fov.right);
        int y1 = Math.min(frame.getHeight() - 1, fov.bottom);
        if (x1 <= x0 || y1 <= y0) return null;

        int w = x1 - x0 + 1;
        int h = y1 - y0 + 1;
        int[] px = new int[w * h];
        frame.getPixels(px, 0, w, x0, y0, w, h);

        byte[] mask = new byte[w * h];
        for (int i = 0; i < px.length; ++i) {
            int c = px[i];
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8)  & 0xFF;
            int b =  c        & 0xFF;
            if (r >= RED_MIN && g <= GREEN_MAX && b <= BLUE_MAX) mask[i] = 1;
        }

        boolean[] visited = new boolean[w * h];
        int[] stack = new int[w * h];
        float bestScore = 0f, bestX = 0, bestY = 0;

        for (int i = 0; i < px.length; ++i) {
            if (mask[i] == 0 || visited[i]) continue;
            int sp = 0;
            stack[sp++] = i;
            visited[i] = true;
            int count = 0;
            long sumX = 0, sumY = 0;

            while (sp > 0) {
                int j = stack[--sp];
                int jx = j % w;
                int jy = j / w;
                count++;
                sumX += jx;
                sumY += jy;

                int[] nb = { j - 1, j + 1, j - w, j + w };
                for (int k = 0; k < 4; ++k) {
                    int n = nb[k];
                    if (n < 0 || n >= mask.length) continue;
                    int nx = n % w;
                    if (Math.abs(nx - jx) > 1) continue;
                    if (mask[n] == 1 && !visited[n]) {
                        visited[n] = true;
                        stack[sp++] = n;
                    }
                }
            }

            if (count < MIN_PIXELS_IN_CLUSTER) continue;

            float cx = x0 + (float) sumX / count;
            float cy = y0 + (float) sumY / count;
            float dx = cx - crosshairX;
            float dy = cy - crosshairY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float distScore = 1f - Math.min(1f, dist / Math.max(w, h));
            float sizeScore = Math.min(1f, count / 200f);
            float score = 0.6f * distScore + 0.4f * sizeScore;

            if (score > bestScore) {
                bestScore = score;
                bestX = cx;
                bestY = cy;
            }
        }

        if (bestScore <= 0f) return null;
        return new Hit(bestX, bestY, bestScore);
    }
}
