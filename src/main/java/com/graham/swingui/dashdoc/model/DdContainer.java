package com.graham.swingui.dashdoc.model;

import java.util.ArrayList;
import java.util.List;

public abstract class DdContainer implements DdPart {
	public List<DdPart> parts = new ArrayList<>();

	DdPart getPartForAction(DdPart partToAdd) {
		if (parts == null || parts.size() == 0) {
			return this.supportsAdd(partToAdd) ? this : null;
		} else {
			DdPart part = parts.get(parts.size() - 1);
			if (part instanceof DdContainer) {
				DdPart subPart = ((DdContainer) part).getPartForAction(partToAdd);
				if (subPart != null) {
					return subPart;
				}
			}
			if (part.supportsAdd(partToAdd)) {
				return part;
			}
		}
		return null;
	}

	@Override
	public boolean addPart(DdPart p) {
		DdPart opPart = getPartForAction(p);
		if (opPart instanceof DdContainer) {
			((DdContainer)opPart).addPartToThis(p);
			return true;
		} else if (opPart != null) {
			return opPart.addPart(p);
		}
		return false;
	}

	void addPartToThis(DdPart p) {
		parts.add(p);
	}

	@Override
	public void postLoadCleanup(DdContainer parent) {
		for (DdPart s : parts) {
			s.postLoadCleanup(this);
		}
	}

	public String getTitle() {
		return null;
	}
}

