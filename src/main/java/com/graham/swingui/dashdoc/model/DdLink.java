package com.graham.swingui.dashdoc.model;

import com.graham.swingui.dashdoc.DdCommon;

public class DdLink implements DdPart {
	public final String linkText;
	public final int nestedLevel;
	DdLink parentLink;

	public DdLink(String l, int lvl) {
		linkText = l.trim();
		nestedLevel = lvl;
	}

	@Override
	public boolean addPart(DdPart p) {
		return false;
	}

	@Override
	public void postLoadCleanup(DdContainer parent) {
		if (nestedLevel == 0) {
			return;
		}
		int thisIndex = parent.parts.indexOf(this);
		for (int i = thisIndex - 1; i >= 0; i--) {
			if (parent.parts.get(i) instanceof DdLink && ((DdLink)parent.parts.get(i)).nestedLevel == nestedLevel - 1) {
				parentLink = (DdLink)parent.parts.get(i);
				return;
			}
		}
	}

	@Override
	public boolean supportsAdd(DdPart part) {
		return false;
	}

	@Override
	public String toString() {
		return linkText;
	}

	public String getLinkToTitle() {
		String l2 = linkText;
		DdLink nextParent = parentLink;
		while (nextParent != null) {
			l2 = nextParent.linkText + DdCommon.Title_Delim_Format + l2;
			nextParent = nextParent.parentLink;
		}
		return l2;
	}

}
