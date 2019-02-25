/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import com.graham.passvaultplus.PvpContext;

import java.awt.*;

public class FlowLayout2 extends FlowLayout {

		private PvpContext context;

		public FlowLayout2(int align, PvpContext c) {
				super(align);
				context = c;
		}

		public Dimension preferredLayoutSize(Container target) {
				if (context.ui.getFrame() == null) {
						return super.preferredLayoutSize(target);
				}
				Insets insets = target.getInsets();
				int maxwidth = context.ui.getFrame().getWidth() - (insets.left + insets.right + this.getHgap()*2) - 21;
				int nmembers = target.getComponentCount();
				int x = 0, y = insets.top + this.getVgap();
				int rowh = 0;

				for (int i = 0 ; i < nmembers ; i++) {
						Component m = target.getComponent(i);
						if (m.isVisible()) {
								Dimension d = m.getPreferredSize();

								if ((x == 0) || ((x + d.width) <= maxwidth)) {
										if (x > 0) {
												x += this.getHgap();
										}
										x += d.width;
										rowh = Math.max(rowh, d.height);
								} else {
										x = d.width;
										y += this.getVgap() + rowh;
										rowh = d.height;
								}
						}
				}

				return new Dimension(maxwidth, y + rowh);
		}
}
