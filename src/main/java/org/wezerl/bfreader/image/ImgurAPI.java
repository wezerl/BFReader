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

import java.util.UUID;

public final class ImgurAPI {

	public static void getAlbumInfo(
			final Context context,
			final String albumUrl,
			final String albumId,
			@NonNull final Priority priority,
			final GetAlbumInfoListener listener) {

		final String apiUrl = "https://api.imgur.com/2/album/" + albumId + ".json";

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
				context,
				new CacheRequestJSONParser(
						context,
				new CacheRequestJSONParser.Listener() {

					@Override
					public void onJsonParsed(
							@NonNull final JsonValue result,
							final long timestamp,
							@NonNull final UUID session,
							final boolean fromCache) {

						try {
							final JsonObject outer = result.asObject().getObject("album");
							listener.onSuccess(AlbumInfo.parseImgur(albumUrl, outer));

						} catch(final Throwable t) {
							listener.onFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									t,
									null,
									"Imgur data parse failed",
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

	public static void getImageInfo(
			final Context context,
			final String imageId,
			@NonNull final Priority priority,
			final GetImageInfoListener listener) {

		final String apiUrl = "https://api.imgur.com/2/image/" + imageId + ".json";

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
				context,
				new CacheRequestJSONParser(context, new CacheRequestJSONParser.Listener() {
					@Override
					public void onJsonParsed(
							@NonNull final JsonValue result,
							final long timestamp,
							@NonNull final UUID session,
							final boolean fromCache) {

						try {
							final JsonObject outer = result.asObject().getObject("image");
							listener.onSuccess(ImageInfo.parseImgur(outer));

						} catch(final Throwable t) {
							listener.onFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									t,
									null,
									"Imgur data parse failed",
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
