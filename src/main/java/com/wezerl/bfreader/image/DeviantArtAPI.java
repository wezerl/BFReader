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

package com.wezerl.bfreader.image;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.wezerl.bfreader.account.RedditAccountManager;
import com.wezerl.bfreader.cache.CacheManager;
import com.wezerl.bfreader.cache.CacheRequest;
import com.wezerl.bfreader.cache.CacheRequestJSONParser;
import com.wezerl.bfreader.cache.downloadstrategy.DownloadStrategyIfNotCached;
import com.wezerl.bfreader.common.Constants;
import com.wezerl.bfreader.common.General;
import com.wezerl.bfreader.common.Optional;
import com.wezerl.bfreader.common.Priority;
import com.wezerl.bfreader.http.FailedRequestBody;
import com.wezerl.bfreader.jsonwrap.JsonObject;
import com.wezerl.bfreader.jsonwrap.JsonValue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public final class DeviantArtAPI {

	public static void getImageInfo(
			final Context context,
			final String url,
			@NonNull final Priority priority,
			final GetImageInfoListener listener) {

		final String apiUrl;
		try {
			apiUrl = "https://backend.deviantart.com/oembed?url="
					+ URLEncoder.encode(url, "UTF-8");

		} catch(final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

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
							@NonNull final UUID session, final boolean fromCache) {

						try {
							final JsonObject outer = result.asObject();
							listener.onSuccess(ImageInfo.parseDeviantArt(outer));

						} catch(final Throwable t) {
							listener.onFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									t,
									null,
									"DeviantArt data parse failed",
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
