/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.wezerl.bfreader.views.glview.displaylist;

import com.wezerl.bfreader.views.glview.program.RRGLMatrixStack;

import java.util.ArrayList;

public class RRGLRenderableGroup extends RRGLRenderable {

	private final ArrayList<RRGLRenderable> mChildren = new ArrayList<>(16);

	public final void add(final RRGLRenderable child) {
		mChildren.add(child);
		if(isAdded()) {
			child.onAdded();
		}
	}

	public final void remove(final RRGLRenderable child) {
		if(isAdded()) {
			child.onRemoved();
		}
		mChildren.remove(child);
	}

	@Override
	public void onAdded() {

		if(!isAdded()) {
			for(final RRGLRenderable entity : mChildren) {
				entity.onAdded();
			}
		}

		super.onAdded();
	}

	@Override
	protected void renderInternal(final RRGLMatrixStack matrixStack, final long time) {
		for(int i = 0; i < mChildren.size(); i++) {
			final RRGLRenderable entity = mChildren.get(i);
			entity.startRender(matrixStack, time);
		}
	}

	@Override
	public void onRemoved() {

		super.onRemoved();

		if(!isAdded()) {
			for(final RRGLRenderable entity : mChildren) {
				entity.onRemoved();
			}
		}
	}

	@Override
	public boolean isAnimating() {
		for(int i = 0; i < mChildren.size(); i++) {
			final RRGLRenderable entity = mChildren.get(i);
			if(entity.isAnimating()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setOverallAlpha(final float alpha) {
		for(int i = 0; i < mChildren.size(); i++) {
			final RRGLRenderable entity = mChildren.get(i);
			entity.setOverallAlpha(alpha);
		}
	}
}
