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

package com.wezerl.bfreader.reddit.url;

import android.net.Uri;

public class UnknownPostListURL extends PostListingURL {

	private final Uri uri;

	UnknownPostListURL(final Uri uri) {
		this.uri = uri;
	}

	@Override
	public PostListingURL after(final String after) {
		return new UnknownPostListURL(uri.buildUpon()
				.appendQueryParameter("after", after)
				.build());
	}

	@Override
	public PostListingURL limit(final Integer limit) {
		return new UnknownPostListURL(uri.buildUpon()
				.appendQueryParameter(
						"limit",
						String.valueOf(limit))
				.build());
	}

	// TODO handle this better
	@Override
	public Uri generateJsonUri() {
		if(uri.getPath().endsWith(".json")) {
			return uri;
		} else {
			return uri.buildUpon().appendEncodedPath(".json").build();
		}
	}

	@Override
	public @RedditURLParser.PathType
	int pathType() {
		return RedditURLParser.UNKNOWN_POST_LISTING_URL;
	}
}
