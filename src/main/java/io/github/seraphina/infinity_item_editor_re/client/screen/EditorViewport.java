package io.github.seraphina.infinity_item_editor_re.client.screen;

/** A uniform, shrink-only transform from editor layout coordinates to GUI coordinates. */
record EditorViewport(int width, int height, double scale) {
    static EditorViewport fit(int availableWidth, int availableHeight, int minimumWidth, int minimumHeight) {
        int availableX = Math.max(1, availableWidth);
        int availableY = Math.max(1, availableHeight);
        double scale = Math.min(1.0D, Math.min((double) availableX / minimumWidth,
                (double) availableY / minimumHeight));
        int width = Math.max(minimumWidth, (int) Math.round(availableX / scale));
        int height = Math.max(minimumHeight, (int) Math.round(availableY / scale));
        // Rounding the layout to integer coordinates must not push an edge off-screen.
        scale = Math.min(scale, Math.min((double) availableX / width, (double) availableY / height));
        return new EditorViewport(width, height, scale);
    }

    double toLayout(double coordinate) {
        return coordinate / this.scale;
    }
}
