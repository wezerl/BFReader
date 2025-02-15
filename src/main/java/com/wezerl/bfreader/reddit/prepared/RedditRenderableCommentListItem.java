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

package com.wezerl.bfreader.reddit.prepared;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import com.wezerl.bfreader.activities.BaseActivity;
import com.wezerl.bfreader.common.Optional;
import com.wezerl.bfreader.common.RRThemeAttributes;

public interface RedditRenderableCommentListItem {

	CharSequence getHeader(
			final RRThemeAttributes theme,
			final RedditChangeDataManager changeDataManager,
			final Context context,
			final int commentAgeUnits,
			final long postCreated,
			final long parentCommentCreated);

	String getAccessibilityHeader(
			final RRThemeAttributes theme,
			final RedditChangeDataManager changeDataManager,
			final Context context,
			final int commentAgeUnits,
			final long postCreated,
			final long parentCommentCreated,
			final boolean collapsed,
			@NonNull final Optional<Integer> indentLevel);

	View getBody(
			final BaseActivity activity,
			final Integer textColor,
			final Float textSize,
			final boolean showLinkButtons);
}
