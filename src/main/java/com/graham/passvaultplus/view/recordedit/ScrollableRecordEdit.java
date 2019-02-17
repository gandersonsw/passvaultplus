package com.graham.passvaultplus.view.recordedit;

import javax.swing.*;
import java.awt.*;

public class ScrollableRecordEdit extends JPanel implements Scrollable {
		public ScrollableRecordEdit(LayoutManager layout) {
				super(layout, true);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
				return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 5;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 5;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
				return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
				return false;
		}
}
