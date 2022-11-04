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
import org.wezerl.bfreader.cache.downloadstrategy.DownloadStrategyIfTimestampOutsideBounds;
import org.wezerl.bfreader.common.Constants;
import org.wezerl.bfreader.common.General;
import org.wezerl.bfreader.common.Optional;
import org.wezerl.bfreader.common.Priority;
import org.wezerl.bfreader.common.RRTime;
import org.wezerl.bfreader.common.TimestampBound;
import org.wezerl.bfreader.http.FailedRequestBody;
import org.wezerl.bfreader.jsonwrap.JsonObject;
import org.wezerl.bfreader.jsonwrap.JsonValue;

import java.util.UUID;

public final class RedgifsAPI {

	public static void getImageInfo(
			final Context context,
			final String imageId,
			@NonNull final Priority priority,
			final GetImageInfoListener listener) {

		final String apiUrl = "https://api.redgifs.com/v1/gfycats/" + imageId;

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				// RedGifs links expire after an undocumented period of time
				new DownloadStrategyIfTimestampOutsideBounds(
						TimestampBound.notOlderThan(RRTime.minsToMs(10))),
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
							final JsonObject outer = result.asObject().getObject("gfyItem");
							listener.onSuccess(ImageInfo.parseGfycat(outer));

						} catch(final Throwable t) {
							listener.onFailure(
									CacheRequest.REQUEST_FAILURE_PARSE,
									t,
									null,
									"Redgifs data parse failed",
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
