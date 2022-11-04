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

package org.wezerl.bfreader.image;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.wezerl.bfreader.account.RedditAccountManager;
import org.wezerl.bfreader.cache.CacheManager;
import org.wezerl.bfreader.cache.CacheRequest;
import org.wezerl.bfreader.cache.CacheRequestJSONParser;
import org.wezerl.bfreader.cache.downloadstrategy.DownloadStrategyIfNotCached;
import org.wezerl.bfreader.common.Constants;
import org.wezerl.bfreader.common.General;
import org.wezerl.bfreader.common.Optional;
import org.wezerl.bfreader.common.Priority;
import org.wezerl.bfreader.http.FailedRequestBody;
import org.wezerl.bfreader.jsonwrap.JsonObject;
import org.wezerl.bfreader.jsonwrap.JsonValue;
import org.wezerl.bfreader.reddit.url.PostCommentListingURL;

import java.util.UUID;

public final class RedditGalleryAPI {

	public static void getAlbumInfo(
			final Context context,
			final String albumUrl,
			final String albumId,
			@NonNull final Priority priority,
			final GetAlbumInfoListener listener) {

		final Uri apiUrl = new PostCommentListingURL(
				null,
				albumId,
				null,
				null,
				null,
				null).generateJsonUri();

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl.toString()),
				RedditAccountManager.getInstance(context).getDefaultAccount(),
				null,
				priority,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				CacheRequest.DOWNLOAD_QUEUE_REDDIT_API,
				context,
				new CacheRequestJSONParser(context, new CacheRequestJSONParser.Listener() {
					@Override
					public void onJsonParsed(
							@NonNull final JsonValue result,
							final long timestamp,
							@NonNull final UUID session,
							final boolean fromCache) {

						try {
							@SuppressWarnings("ConstantConditions") final JsonObject
									redditPostData = result.asArray()
									.getObject(0)
									.getObject("data")
									.getArray("children")
									.getObject(0)
									.getObject("data");

							final AlbumInfo album
									= AlbumInfo.parseRedditGallery(albumUrl, redditPostData);

							if(album == null) {

								if(redditPostData.getString("removed_by_category") != null) {
									listener.onGalleryRemoved();
								} else {
									listener.onGalleryDataNotPresent();
								}

							} else {
								listener.onSuccess(album);
							}

						} catch(final Exception e) {
							listener.onFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									e,
									null,
									"Reddit gallery data parse failed",
									Optional.of(new FailedRequestBody(result)));
						}
					}

					@Override
					public void onFailure(
							final int type,
							@Nullable final Throwable t,
							@Nullable final Integer httpStatus,
							@Nullable final String readableMessage,
							@NonNull final Optional<FailedRequestBody> body) {

						listener.onFailure(
								type,
								t,
								httpStatus,
								readableMessage,
								body);
					}
				})));
	}
}
