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

package com.wezerl.bfreader.reddit.api;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.wezerl.bfreader.account.RedditAccount;
import com.wezerl.bfreader.cache.CacheManager;
import com.wezerl.bfreader.cache.CacheRequest;
import com.wezerl.bfreader.cache.CacheRequestJSONParser;
import com.wezerl.bfreader.cache.downloadstrategy.DownloadStrategyAlways;
import com.wezerl.bfreader.common.Constants;
import com.wezerl.bfreader.common.Optional;
import com.wezerl.bfreader.common.Priority;
import com.wezerl.bfreader.common.RRTime;
import com.wezerl.bfreader.common.TimestampBound;
import com.wezerl.bfreader.http.FailedRequestBody;
import com.wezerl.bfreader.io.CacheDataSource;
import com.wezerl.bfreader.io.RequestResponseHandler;
import com.wezerl.bfreader.io.WritableHashSet;
import com.wezerl.bfreader.jsonwrap.JsonArray;
import com.wezerl.bfreader.jsonwrap.JsonValue;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class RedditAPIMultiredditListRequester implements CacheDataSource<
		RedditAPIMultiredditListRequester.Key, WritableHashSet, SubredditRequestFailure> {

	public static class Key {
		public static final Key INSTANCE = new Key();

		private Key() {
		}
	}

	private final Context context;
	private final RedditAccount user;

	public RedditAPIMultiredditListRequester(final Context context, final RedditAccount user) {
		this.context = context;
		this.user = user;
	}

	@Override
	public void performRequest(
			final Key key,
			final TimestampBound timestampBound,
			final RequestResponseHandler<WritableHashSet, SubredditRequestFailure> handler) {

		if(user.isAnonymous()) {

			final long now = RRTime.utcCurrentTimeMillis();

			handler.onRequestSuccess(
					new WritableHashSet(
							new HashSet<String>(),
							now,
							user.getCanonicalUsername()),
					now);

		} else {
			doRequest(handler);
		}
	}

	private void doRequest(
			final RequestResponseHandler<WritableHashSet, SubredditRequestFailure> handler) {

		final URI uri = Constants.Reddit.getUri(Constants.Reddit.PATH_MULTIREDDITS_MINE);

		final CacheRequest request = new CacheRequest(
				uri,
				user,
				null,
				new Priority(Constants.Priority.API_SUBREDDIT_LIST),
				DownloadStrategyAlways.INSTANCE,
				Constants.FileType.MULTIREDDIT_LIST,
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
							final HashSet<String> output = new HashSet<>();

							final JsonArray multiredditList = result.asArray();

							for(final JsonValue multireddit : multiredditList) {
								final String name = multireddit.asObject()
										.getObject("data")
										.getString("name");
								output.add(name);
							}

							handler.onRequestSuccess(new WritableHashSet(
									output,
									timestamp,
									user.getCanonicalUsername()), timestamp);

						} catch(final Exception e) {
							handler.onRequestFailed(
									new SubredditRequestFailure(
											CacheRequest.REQUEST_FAILURE_PARSE,
											e,
											null,
											"Parse error",
											uri.toString(),
											Optional.of(new FailedRequestBody(result))));
						}
					}

					@Override
					public void onFailure(
							final int type,
							@Nullable final Throwable t,
							@Nullable final Integer httpStatus,
							@Nullable final String readableMessage,
							@NonNull final Optional<FailedRequestBody> body) {

						handler.onRequestFailed(new SubredditRequestFailure(
								type,
								t,
								httpStatus,
								readableMessage,
								uri.toString(),
								body));
					}
				}));

		CacheManager.getInstance(context).makeRequest(request);
	}

	@Override
	public void performRequest(
			final Collection<Key> keys, final TimestampBound timestampBound,
			final RequestResponseHandler<HashMap<Key, WritableHashSet>,
					SubredditRequestFailure> handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void performWrite(final WritableHashSet value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void performWrite(final Collection<WritableHashSet> values) {
		throw new UnsupportedOperationException();
	}
}
